package link.infra.demagnetize.blocks;

import java.util.List;

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

	public DemagnetizerTileEntity() {
		super();
		range = getMaxRange();

		updateBoundingBox();
		DemagnetizerEventHandler.addTileEntity(this);
	}

	// TODO: replace with configuration
	public int getMaxRange() {
		return 4;
	}

	private void updateBoundingBox() {
		int negRange = range * -1;
		scanArea = new AxisAlignedBB(getPos().add(negRange, negRange, negRange), getPos().add(range, range, range));
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
		if (compound.hasKey("items")) {
			itemStackHandler.deserializeNBT((NBTTagCompound) compound.getTag("items"));
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
			}
		}
		if (compound.hasKey("whitelist")) {
			filtersWhitelist = compound.getBoolean("whitelist");
		}
		if (compound.hasKey("redstonePowered")) {
			isPowered = compound.getBoolean("redstonePowered");
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setTag("items", itemStackHandler.serializeNBT());
		compound.setString("redstone", redstoneSetting.name());
		compound.setInteger("range", range);
		compound.setBoolean("whitelist", filtersWhitelist);
		compound.setBoolean("redstonePowered", isPowered);
		return compound;
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

		if (!redstoneCheck()) {
			// Reset tick time when redstone disabled
			currTick = tickTime;
			return;
		}

		if (currTick == tickTime) {
			List<EntityItem> list = world.getEntitiesWithinAABB(EntityItem.class, scanArea);
			for (EntityItem item : list) {
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
			return scanArea.intersects(entityBox);
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
		}
	}

	public void updateRedstone(boolean redstoneStatus) {
		isPowered = redstoneStatus;
		markDirty();
		if (world != null) {
			IBlockState state = world.getBlockState(getPos());
			world.notifyBlockUpdate(getPos(), state, state, 3);
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

	// TODO: replace with configuration
	public int getFilterSize() {
		return 4;
	}

	public boolean canInteractWith(EntityPlayer playerIn) {
		// If we are too far away from this tile entity you cannot use it
		return !isInvalid() && playerIn.getDistanceSq(pos.add(0.5D, 0.5D, 0.5D)) <= 64D;
	}

}
