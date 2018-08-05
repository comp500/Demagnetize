package link.infra.demagnetize.blocks;

import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class DemagnetizerTileEntity extends TileEntity implements ITickable {
	
	AxisAlignedBB scanArea;
	private int range;
	@SuppressWarnings("unused")
	private final int maxRange;

	public DemagnetizerTileEntity(int maxRange) {
		super();
		this.maxRange = maxRange;
		range = maxRange;
		
		updateBoundingBox();
		DemagnetizerEventHandler.addTileEntity(this);
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
	
	public boolean canInteractWith(EntityPlayer playerIn) {
		// If we are too far away from this tile entity you cannot use it
		return !isInvalid() && playerIn.getDistanceSq(pos.add(0.5D, 0.5D, 0.5D)) <= 64D;
	}
	
	@Override
	public void update() {
		// Remove te if it has been destroyed
		if (isInvalid()) {
			DemagnetizerEventHandler.removeTileEntity(this);
			return;
		}

		List<EntityItem> list = world.getEntitiesWithinAABB(EntityItem.class, scanArea);
		for (EntityItem item : list) {
			demagnetizeItem(item);
		}
	}
	
	public boolean checkItem(EntityItem item) {
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
		
	}

	public boolean isRedstoneEnabled() {
		return true;
	}

}
