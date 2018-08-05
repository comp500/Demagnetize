package link.infra.demagnetize.blocks;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class DemagnetizerContainer extends Container {

	private DemagnetizerTileEntity te;
	
	public DemagnetizerContainer(IInventory playerInventory, DemagnetizerTileEntity te) {
		this.te = te;
		addOwnSlots();
		addPlayerSlots(playerInventory);
	}

	private void addPlayerSlots(IInventory playerInventory) {
		// Slots for the main inventory
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 9; ++col) {
				int x = 8 + col * 18;
				int y = row * 18 + 84;
				this.addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, x, y));
			}
		}

		// Slots for the hotbar
		for (int row = 0; row < 9; ++row) {
			int x = 8 + row * 18;
			int y = 142;
			this.addSlotToContainer(new Slot(playerInventory, row, x, y));
		}
	}

	private void addOwnSlots() {
		for (int i = 0; i < te.getFilterSize(); i++) {
			int row = i / 4;
			int column = i % 4;
			addSlotToContainer(new SlotItemHandler(te.itemStackHandler, i, 70 + (column * 18), 26 + (row * 18)));
		}
	}

	@Nullable
	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return te.canInteractWith(playerIn);
	}

	@Override
	public void onContainerClosed(EntityPlayer playerIn) {
		super.onContainerClosed(playerIn);
	}
	
	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
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
