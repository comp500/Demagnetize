package link.infra.demagnetize.blocks;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class DemagnetizerTileEntity extends TileEntity implements ITickable {
	
	AxisAlignedBB scanArea;

	public DemagnetizerTileEntity() {
		super();
		scanArea = new AxisAlignedBB(getPos().add(-2, -2, -2), getPos().add(2, 2, 2));
		DemagnetizerEventHandler.addTileEntity(this);
	}

	public boolean canInteractWith(EntityPlayer playerIn) {
		// If we are too far away from this tile entity you cannot use it
		return !isInvalid() && playerIn.getDistanceSq(pos.add(0.5D, 0.5D, 0.5D)) <= 64D;
	}
	
	@Override
    public void update() {
		/*List<EntityItem> list = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(getPos().add(-2, -2, -2), getPos().add(2, 2, 2)));
		for (EntityItem item : list) {
			NBTTagCompound data = item.getEntityData();
			if (data != null) {
				if (!data.getBoolean("PreventRemoteMovement")) {
					data.setBoolean("PreventRemoteMovement", true);
				}
			}
		}*/
	}
	
	// Ensure that the new bounding box is updated
	@Override
	public void setPos(BlockPos pos) {
		super.setPos(pos);
		scanArea = new AxisAlignedBB(getPos().add(-2, -2, -2), getPos().add(2, 2, 2));
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

}
