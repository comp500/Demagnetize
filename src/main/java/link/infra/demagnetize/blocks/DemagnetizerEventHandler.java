package link.infra.demagnetize.blocks;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class DemagnetizerEventHandler {
	
	private static List<WeakReference<DemagnetizerTileEntity>> teList = new ArrayList<WeakReference<DemagnetizerTileEntity>>();
	
	public static void addTileEntity(DemagnetizerTileEntity te) {
		teList.add(new WeakReference<DemagnetizerTileEntity>(te));
	}
	
	public static void removeTileEntity(DemagnetizerTileEntity te) {
		for (Iterator<WeakReference<DemagnetizerTileEntity>> iterator = teList.iterator(); iterator.hasNext();) {
			WeakReference<DemagnetizerTileEntity> weakRef = iterator.next();
			
			if (weakRef.get() == null) {
				iterator.remove();
				continue;
			}
			
			DemagnetizerTileEntity ent = weakRef.get();
			
			if (ent.isInvalid() || ent.equals(te)) {
				iterator.remove();
			}
		}
	}
	
	@SubscribeEvent
	public static void itemSpawned(EntityJoinWorldEvent event) {
		Entity ent = event.getEntity();
		if (!(ent instanceof EntityItem)) {
			return;
		}
		
		EntityItem item = (EntityItem) ent;
		
		for (Iterator<WeakReference<DemagnetizerTileEntity>> iterator = teList.iterator(); iterator.hasNext();) {
			WeakReference<DemagnetizerTileEntity> weakRef = iterator.next();
			
			// Remove te if it has been GC'd
			if (weakRef.get() == null) {
				iterator.remove();
				continue;
			}
			
			DemagnetizerTileEntity te = weakRef.get();
			
			// Remove te if it has been destroyed
			if (te.isInvalid()) {
				iterator.remove();
				continue;
			}
			
			// Must be in the same world
			if (!te.getWorld().equals(item.getEntityWorld())) {
				continue;
			}
			
			if (te.checkItem(item)) {
				te.demagnetizeItem(item);
				return;
			}
		}
	}

}
