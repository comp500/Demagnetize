package link.infra.demagnetize.blocks;

import link.infra.demagnetize.ConfigHandler;
import link.infra.demagnetize.network.PacketDemagnetizerSettings;
import link.infra.demagnetize.network.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class DemagnetizerTileEntity extends BlockEntity implements MenuProvider {

	public enum RedstoneStatus {
		REDSTONE_DISABLED(0), POWERED(1), UNPOWERED(2);

		private final int num;

		RedstoneStatus(int num) {
			this.num = num;
		}

		public int getNum() {
			return num;
		}

		public static RedstoneStatus parse(int num) {
			for (RedstoneStatus s : RedstoneStatus.values()) {
				if (s.getNum() == num) {
					return s;
				}
			}
			return null;
		}
	}

	private AABB scanArea;
	private int range;
	private RedstoneStatus redstoneSetting = RedstoneStatus.REDSTONE_DISABLED;
	private boolean filtersWhitelist = false; // Default to using blacklist
	private boolean isPowered = false;

	final boolean advanced;

	public DemagnetizerTileEntity(boolean advanced, BlockPos blockPos, BlockState blockState) {
		super(advanced ? ModBlocks.DEMAGNETIZER_ADVANCED_TILE_ENTITY : ModBlocks.DEMAGNETIZER_TILE_ENTITY, blockPos, blockState);
		this.advanced = advanced;

		range = getMaxRange();

		updateBoundingBox();
		DemagnetizerEventHandler.addTileEntity(this);

		itemStackHandler = new ItemStackHandler(getFilterSize()) {
			@Override
			protected void onContentsChanged(int slot) {
				DemagnetizerTileEntity.this.setChanged();
			}

			@Override
			public int getSlotLimit(int slot) {
				return 1; // Only allow one item
			}

			@Nonnull
			@Override
			public ItemStack extractItem(int slot, int amount, boolean simulate) {
				this.stacks.set(slot, ItemStack.EMPTY);
				return ItemStack.EMPTY; // Extract "failed", no increment of stack size (ghost item)
			}
		};
	}

	int getMaxRange() {
		return advanced ? ConfigHandler.DEMAGNETIZER_ADVANCED_RANGE.get() : ConfigHandler.DEMAGNETIZER_RANGE.get();
	}

	private void updateBoundingBox() {
		scanArea = new AABB(getPos()).inflate(range);
	}

	/*FIXME 1.18
	// Ensure that the new bounding box is updated
	@Override
	public void setPosition(@Nonnull BlockPos pos) {
		super.setPosition(pos);
		updateBoundingBox();
	}

	// Ensure that the new bounding box is updated
	@Override
	public void setLevelAndPosition(@Nonnull Level world, @Nonnull BlockPos pos) {
		super.setLevelAndPosition(world, pos);
		updateBoundingBox();
	}*/

	@Override
	public void deserializeNBT(CompoundTag compound) {
		super.deserializeNBT(compound);
		if (compound.contains("redstone")) {
			try {
				redstoneSetting = RedstoneStatus.valueOf(compound.getString("redstone"));
			} catch (IllegalArgumentException e) {
				// Ignore
			}
		}
		if (compound.contains("range")) {
			int pendingRange = compound.getInt("range");
			if (pendingRange <= getMaxRange() && pendingRange > 0) {
				range = pendingRange;
			}
		}
		if (getFilterSize() == 0) {
			filtersWhitelist = false;
		} else {
			if (compound.contains("whitelist")) {
				filtersWhitelist = compound.getBoolean("whitelist");
			}
			if (compound.contains("items")) {
				CompoundTag itemsTag = compound.getCompound("items");
				// Reset the filter size in NBT, in case config changes
				itemsTag.putInt("Size", getFilterSize());
				itemStackHandler.deserializeNBT(itemsTag);
			}
		}
		if (compound.contains("redstonePowered")) {
			isPowered = compound.getBoolean("redstonePowered");
		}
		// super.read could update TE pos
		updateBoundingBox();
	}

	@Nonnull
	@Override
	public CompoundTag serializeNBT() {
		CompoundTag compound = super.serializeNBT();
		compound.put("items", itemStackHandler.serializeNBT());
		compound.putString("redstone", redstoneSetting.name());
		compound.putInt("range", range);
		compound.putBoolean("whitelist", filtersWhitelist);
		compound.putBoolean("redstonePowered", isPowered);
		return compound;
	}

	@Nonnull
	@Override
	public CompoundTag getUpdateTag() {
		return serializeNBT();
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
		this.deserializeNBT(packet.getTag());
	}

	// Only check for items every 4 ticks
	private final int tickTime = 4;
	private int currTick = tickTime;

	public void tick() {
		// Remove te if it has been destroyed
		if (isRemoved() || !hasLevel()) {
			DemagnetizerEventHandler.removeTileEntity(this);
			return;
		}

		if (redstoneUnpowered()) {
			// Reset tick time when redstone disabled
			currTick = tickTime;
			return;
		}

		if (currTick == tickTime) {
			assert level != null;
			List<ItemEntity> list = level.getEntitiesOfClass(ItemEntity.class, scanArea);
			for (ItemEntity item : list) {
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

	public BlockPos getPos() {
		return getBlockPos();
	}

	boolean checkItem(ItemEntity item) {
		if (redstoneUnpowered()) {
			return false;
		}

		if (scanArea != null && item != null) {
			AABB entityBox = item.getBoundingBox();
			return scanArea.intersects(entityBox) && checkItemFilter(item);
		} else {
			return false;
		}
	}

	/**
	 * Equivalent to checkItem but called on the client when the item is empty (hasn't received metadata yet)
	 */
	boolean checkItemClientPreMetadata(ItemEntity item) {
		if (redstoneUnpowered()) {
			return false;
		}

		if (scanArea != null && item != null) {
			// Can't use checkItemFilter yet, so queue checking for the next TE tick
			return scanArea.intersects(item.getBoundingBox());
		}
		return false;
	}

	void demagnetizeItem(ItemEntity item) {
		CompoundTag data = item.getPersistentData();
		if (!data.getBoolean("PreventRemoteMovement")) {
			data.putBoolean("PreventRemoteMovement", true);
		}
		// Allow machines to remotely move items
		if (!data.getBoolean("AllowMachineRemoteMovement")) {
			data.putBoolean("AllowMachineRemoteMovement", true);
		}
	}

	public void updateBlock() {
		setChanged();
		if (level != null) {
			BlockState state = level.getBlockState(getPos());
			level.sendBlockUpdated(getPos(), state, state, 3);
		}
	}

	void updateRedstone(boolean redstoneStatus) {
		isPowered = redstoneStatus;
		updateBlock();
	}

	private boolean redstoneUnpowered() {
		return switch (redstoneSetting) {
			case POWERED -> !isPowered;
			case UNPOWERED -> isPowered;
			default -> false;
		};
	}

	final ItemStackHandler itemStackHandler;

	int getFilterSize() {
		return advanced ? ConfigHandler.DEMAGNETIZER_ADVANCED_FILTER_SLOTS.get() : ConfigHandler.DEMAGNETIZER_FILTER_SLOTS.get();
	}

	private boolean checkItemFilter(ItemEntity item) {
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

	private boolean checkItemFilterMatches(ItemEntity item) {
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
			if (filterStack.sameItemStackIgnoreDurability(matchingItem)) {
				return true;
			}
		}
		return false;
	}

	int getRange() {
		if (range > getMaxRange()) {
			range = getMaxRange();
		}
		if (range < 1) {
			range = 0;
		}
		return range;
	}

	RedstoneStatus getRedstoneSetting() {
		return redstoneSetting;
	}

	boolean isWhitelist() {
		return filtersWhitelist;
	}

	public void setRange(int range) {
		this.range = range;
		updateBoundingBox();
	}

	public void setRedstoneSetting(RedstoneStatus setting) {
		this.redstoneSetting = setting;
	}

	public void setWhitelist(boolean whitelist) {
		if (getFilterSize() > 0) {
			this.filtersWhitelist = whitelist;
		} else {
			this.filtersWhitelist = false;
		}
	}

	void sendSettingsToServer() {
		PacketHandler.INSTANCE.sendToServer(new PacketDemagnetizerSettings(range, redstoneSetting, filtersWhitelist, getPos()));
	}

	@Nonnull
	@Override
	public Component getDisplayName() {
		if (advanced) {
			return ModBlocks.DEMAGNETIZER_ADVANCED.getName();
		} else {
			return ModBlocks.DEMAGNETIZER.getName();
		}
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int i, @Nonnull Inventory playerInventory, @Nonnull Player playerEntity) {
		assert level != null;
		return new DemagnetizerContainer(i, level, getPos(), playerInventory);
	}

}
