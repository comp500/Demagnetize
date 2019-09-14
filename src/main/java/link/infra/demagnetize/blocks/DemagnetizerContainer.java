package link.infra.demagnetize.blocks;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import java.util.Objects;

public class DemagnetizerContainer extends Container {
	DemagnetizerTileEntity te;

	public DemagnetizerContainer(int windowId, World world, BlockPos pos, PlayerInventory playerInventory) {
		super(ModBlocks.DEMAGNETIZER_CONTAINER, windowId);
		this.te = (DemagnetizerTileEntity) world.getTileEntity(pos);
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
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canInteractWith(@Nonnull PlayerEntity playerEntity) {
		World world = Objects.requireNonNull(te.getWorld());
		if (te.advanced) {
			return isWithinUsableDistance(IWorldPosCallable.of(world, te.getPos()), playerEntity, ModBlocks.DEMAGNETIZER_ADVANCED);
		} else {
			return isWithinUsableDistance(IWorldPosCallable.of(world, te.getPos()), playerEntity, ModBlocks.DEMAGNETIZER);
		}
	}
	
	@Nonnull
	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
		if (slotId >= 0 && slotId < te.getFilterSize()) {
			Slot slot = this.inventorySlots.get(slotId);
			ItemStack heldStack = player.inventory.getItemStack();
			
			if (heldStack.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				ItemStack single = heldStack.copy();
				single.setCount(1);
				slot.putStack(single);
			}
			return ItemStack.EMPTY;
		}
		return super.slotClick(slotId, dragType, clickTypeIn, player);
	}

}
