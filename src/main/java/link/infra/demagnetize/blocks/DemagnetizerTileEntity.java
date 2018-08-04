package link.infra.demagnetize.blocks;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DemagnetizerTileEntity extends TileEntity implements ITickable {
	
	AxisAlignedBB scanArea;

	public DemagnetizerTileEntity() {
		super();
		MinecraftForge.EVENT_BUS.register(this);
		scanArea = new AxisAlignedBB(getPos().add(-2, -2, -2), getPos().add(2, 2, 2));
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
	
	@SubscribeEvent
	public void itemSpawned(EntityJoinWorldEvent event) {
		if (isInvalid()) {
			MinecraftForge.EVENT_BUS.unregister(this);
			return;
		}
		
		Entity ent = event.getEntity();
		if (ent instanceof EntityItem) {
			//System.out.println("detected item");
			AxisAlignedBB entityBox = ent.getEntityBoundingBox();
			if (scanArea != null && entityBox != null && scanArea.intersects(entityBox)) {
				NBTTagCompound data = ((EntityItem) ent).getEntityData();
				System.out.println("hello " + ((EntityItem)ent).getDisplayName().getUnformattedText());
				if (data != null) {
					if (!data.getBoolean("PreventRemoteMovement")) {
						data.setBoolean("PreventRemoteMovement", true);
					}
				}
			}
		}
	}

}
