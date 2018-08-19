package link.infra.demagnetize.blocks;

import java.util.List;

import org.apache.logging.log4j.Level;

import link.infra.demagnetize.ConfigHandler;
import link.infra.demagnetize.Demagnetize;
import link.infra.demagnetize.network.PacketDemagnetizerSettings;
import link.infra.demagnetize.network.PacketHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.items.ItemStackHandler;

public class DemagnetizerTileEntity extends TileEntity implements ITickable {

	public enum RedstoneStatus {
		REDSTONE_DISABLED, POWERED, UNPOWERED
	}

	AxisAlignedBB scanArea;
	private int range;
	private RedstoneStatus redstoneSetting = RedstoneStatus.REDSTONE_DISABLED;
	private boolean filtersWhitelist = false; // Default to using blacklist
	private boolean isPowered = false;
	private IDemagnetizerSolegnoliaCompat subtile;

	public DemagnetizerTileEntity() {
		super();
		range = getMaxRange();

		// TODO: add config
		if (Loader.isModLoaded("botania")) {
			// reflection to avoid hard dependency on Botania
			try {
				subtile = Class.forName("link.infra.demagnetize.blocks.DemagnetizerSolegnoliaCompat").asSubclass(IDemagnetizerSolegnoliaCompat.class)
						.newInstance();
				subtile.setRange(getMaxRange());
				subtile.setSupertile(this);
			} catch (Exception e) {
				Demagnetize.logger.catching(Level.ERROR, e);
			}
		} else {
			Demagnetize.logger.debug("hi");
		}
		
		updateBoundingBox();
		DemagnetizerEventHandler.addTileEntity(this);
	}

	public int getMaxRange() {
		return ConfigHandler.demagnetizerRange;
	}

	private void updateBoundingBox() {
		int negRange = range * -1;
		scanArea = new AxisAlignedBB(getPos().add(negRange, negRange, negRange), getPos().add(range, range, range));
		
		if (subtile != null) {
			subtile.setRange(range);
		}
	}

	// Ensure that the new bounding box is updated
	@Override
	public void setPos(BlockPos pos) {
		super.setPos(pos);
		updateBoundingBox();
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		readInternalNBT(compound);
	}
	
	// Read NBT without setting BlockPos
	public void readInternalNBT(NBTTagCompound compound) {
		if (compound.hasKey("items")) {
			NBTTagCompound itemsTag = (NBTTagCompound) compound.getTag("items");
			// Reset the filter size in NBT, in case config changes
			itemsTag.setInteger("Size", getFilterSize());
			itemStackHandler.deserializeNBT(itemsTag);
		}
		if (compound.hasKey("redstone")) {
			try {
				redstoneSetting = RedstoneStatus.valueOf(compound.getString("redstone"));
			} catch (IllegalArgumentException e) {
				// Ignore
			}
		}
		if (compound.hasKey("range")) {
			int pendingRange = compound.getInteger("range");
			if (pendingRange <= getMaxRange() && pendingRange > 0) {
				range = pendingRange;
				updateBoundingBox();
			}
		}
		if (compound.hasKey("whitelist")) {
			filtersWhitelist = compound.getBoolean("whitelist");
		}
		if (compound.hasKey("redstonePowered")) {
			isPowered = compound.getBoolean("redstonePowered");
		}

		if (subtile != null) {
			subtile.setActive(redstoneCheck());
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		writeInternalNBT(compound);
		return compound;
	}
	
	public void writeInternalNBT(NBTTagCompound compound) {
		compound.setTag("items", itemStackHandler.serializeNBT());
		compound.setString("redstone", redstoneSetting.name());
		compound.setInteger("range", range);
		compound.setBoolean("whitelist", filtersWhitelist);
		compound.setBoolean("redstonePowered", isPowered);
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbtTag = new NBTTagCompound();
		this.writeToNBT(nbtTag);
		return new SPacketUpdateTileEntity(getPos(), 1, nbtTag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
		this.readFromNBT(packet.getNbtCompound());
	}

	// Only check for items every 4 ticks
	private final int tickTime = 4;
	private int currTick = tickTime;

	@Override
	public void update() {
		// Remove te if it has been destroyed
		if (isInvalid()) {
			DemagnetizerEventHandler.removeTileEntity(this);
			return;
		}
		
		if (subtile != null) {
			subtile.onUpdate();
		}

		if (!redstoneCheck()) {
			// Reset tick time when redstone disabled
			currTick = tickTime;
			return;
		}

		if (currTick == tickTime) {
			List<EntityItem> list = world.getEntitiesWithinAABB(EntityItem.class, scanArea);
			for (EntityItem item : list) {
				if (!checkItemFilter(item)) {
					continue;
				}
				demagnetizeItem(item);
			}

			currTick = 0;
		} else {
			currTick++;
		}
	}

	public boolean checkItem(EntityItem item) {
		if (!redstoneCheck()) {
			return false;
		}

		if (scanArea != null && item != null) {
			AxisAlignedBB entityBox = item.getEntityBoundingBox();
			if (entityBox == null) {
				return false;
			}
			return scanArea.intersects(entityBox) && checkItemFilter(item);
		} else {
			return false;
		}
	}

	public void demagnetizeItem(EntityItem item) {
		NBTTagCompound data = item.getEntityData();
		if (data != null) {
			if (!data.getBoolean("PreventRemoteMovement")) {
				data.setBoolean("PreventRemoteMovement", true);
			}
			// Allow machines to remotely move items
			if (!data.getBoolean("AllowMachineRemoteMovement")) {
				data.setBoolean("AllowMachineRemoteMovement", true);
			}
		}
	}

	public void updateBlock() {
		markDirty();
		if (world != null) {
			IBlockState state = world.getBlockState(getPos());
			world.notifyBlockUpdate(getPos(), state, state, 3);
		}
	}
	
	public void updateRedstone(boolean redstoneStatus) {
		isPowered = redstoneStatus;
		updateBlock();
		
		if (subtile != null) {
			subtile.setActive(redstoneCheck());
		}
	}

	private boolean redstoneCheck() {
		switch (redstoneSetting) {
		case POWERED:
			return isPowered;
		case UNPOWERED:
			return !isPowered;
		default:
			return true;
		}
	}

	protected ItemStackHandler itemStackHandler = new ItemStackHandler(getFilterSize()) {
		@Override
		protected void onContentsChanged(int slot) {
			DemagnetizerTileEntity.this.markDirty();
		}
		
		@Override
		public int getSlotLimit(int slot) {
			return 1; // Only allow one item
		}
		
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			this.stacks.set(slot, ItemStack.EMPTY);
			return ItemStack.EMPTY; // Extract "failed", no increment of stack size (ghost item)
		}
	};

	public int getFilterSize() {
		return ConfigHandler.demagnetizerFilterSlots;
	}
	
	public boolean checkItemFilter(EntityItem item) {
		// Client event gives empty itemstack, cannot be compared so must ignore
		if (item.getItem().isEmpty()) {
			// If filter is empty and whitelist is disabled, can safely demagnetize
			if (!filtersWhitelist) {
				for (int i = 0; i < itemStackHandler.getSlots(); i++) {
					if (!itemStackHandler.getStackInSlot(i).isEmpty()) {
						return false;
					}
				}
				return true;
			}
			return false;
		}
		
		if (filtersWhitelist) {
			return checkItemFilterMatches(item);
		} else {
			return !checkItemFilterMatches(item);
		}
	}
	
	public boolean checkItemFilterMatches(EntityItem item) {
		ItemStack matchingItem = item.getItem();
		for (int i = 0; i < itemStackHandler.getSlots(); i++) {
			// If the current slot index >= filter size, return and ignore future slots
			if (i >= getFilterSize()) {
				return false;
			}
			
			ItemStack filterStack = itemStackHandler.getStackInSlot(i);
			if (filterStack.isEmpty()) {
				continue;
			}
			if (filterStack.isItemEqualIgnoreDurability(matchingItem)) {
				return true;
			}
		}
		return false;
	}

	public boolean canInteractWith(EntityPlayer playerIn) {
		// If we are too far away from this tile entity you cannot use it
		return !isInvalid() && playerIn.getDistanceSq(pos.add(0.5D, 0.5D, 0.5D)) <= 64D;
	}
	
	public int getRange() {
		if (range > getMaxRange()) {
			range = getMaxRange();
		}
		if (range < 1) {
			range = 0;
		}
		return range;
	}
	
	public RedstoneStatus getRedstoneSetting() {
		return redstoneSetting;
	}
	
	public boolean isWhitelist() {
		return filtersWhitelist;
	}
	
	public void setRange(int range) {
		this.range = range;
		updateBoundingBox();
	}
	
	public void setRedstoneSetting(RedstoneStatus setting) {
		this.redstoneSetting = setting;
		
		if (subtile != null) {
			subtile.setActive(redstoneCheck());
		}
	}
	
	public void setWhitelist(boolean whitelist) {
		this.filtersWhitelist = whitelist;
	}
	
	public void sendSettingsToServer() {
		PacketHandler.INSTANCE.sendToServer(new PacketDemagnetizerSettings(range, redstoneSetting, filtersWhitelist, getPos()));
	}

}
