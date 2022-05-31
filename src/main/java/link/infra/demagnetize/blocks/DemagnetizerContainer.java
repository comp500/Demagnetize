package link.infra.demagnetize.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import java.util.Objects;

public class DemagnetizerContainer extends AbstractContainerMenu {
	final DemagnetizerTileEntity te;

	public DemagnetizerContainer(int windowId, Level world, BlockPos pos, Inventory playerInventory) {
		super(ModBlocks.DEMAGNETIZER_CONTAINER, windowId);
		this.te = (DemagnetizerTileEntity) world.getBlockEntity(pos);
		addOwnSlots();
		addPlayerSlots(new InvWrapper(playerInventory));
	}

	private void addPlayerSlots(IItemHandler playerInventory) {
		// Slots for the main inventory
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 9; ++col) {
				int x = 8 + col * 18;
				int y = row * 18 + 84;
				addSlot(new SlotItemHandler(playerInventory, col + row * 9 + 9, x, y));
			}
		}

		// Slots for the hotbar
		for (int row = 0; row < 9; ++row) {
			int x = 8 + row * 18;
			int y = 142;
			addSlot(new SlotItemHandler(playerInventory, row, x, y));
		}
	}

	private void addOwnSlots() {
		for (int i = 0; i < te.getFilterSize(); i++) {
			addSlot(new SlotItemHandler(te.itemStackHandler, i, 8 + (i * 18), 53));
		}
	}

	@Nonnull
	@Override
	public ItemStack quickMoveStack(@Nonnull Player playerIn, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(@Nonnull Player playerEntity) {
		Level world = Objects.requireNonNull(te.getLevel());
		if (te.advanced) {
			return stillValid(ContainerLevelAccess.create(world, te.getBlockPos()), playerEntity, ModBlocks.DEMAGNETIZER_ADVANCED);
		} else {
			return stillValid(ContainerLevelAccess.create(world, te.getBlockPos()), playerEntity, ModBlocks.DEMAGNETIZER);
		}
	}

	@Override
	public void clicked(int slotId, int dragType, @Nonnull ClickType clickTypeIn, @Nonnull Player player) {
		if (slotId >= 0 && slotId < te.getFilterSize()) {
			Slot slot = this.slots.get(slotId);
			ItemStack heldStack = getCarried();

			if (heldStack.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				ItemStack single = heldStack.copy();
				single.setCount(1);
				slot.set(single);
			}
		} else super.clicked(slotId, dragType, clickTypeIn, player);
	}
}
