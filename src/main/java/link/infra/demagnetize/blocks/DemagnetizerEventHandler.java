package link.infra.demagnetize.blocks;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Mod.EventBusSubscriber
public class DemagnetizerEventHandler {
	
	private static final List<WeakReference<DemagnetizerTileEntity>> teList = new ArrayList<>();
	
	static void addTileEntity(DemagnetizerTileEntity te) {
		synchronized (teList) {
			teList.add(new WeakReference<>(te));
		}
	}
	
	static void removeTileEntity(DemagnetizerTileEntity te) {
		synchronized (teList) {
			for (Iterator<WeakReference<DemagnetizerTileEntity>> iterator = teList.iterator(); iterator.hasNext();) {
				WeakReference<DemagnetizerTileEntity> weakRef = iterator.next();
				
				if (weakRef == null || weakRef.get() == null) {
					iterator.remove();
					continue;
				}
				
				DemagnetizerTileEntity ent = weakRef.get();

				if (ent == null || ent.isRemoved() || ent.equals(te)) {
					iterator.remove();
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void itemSpawned(EntityJoinWorldEvent event) {
		Entity ent = event.getEntity();
		if (!(ent instanceof ItemEntity)) {
			return;
		}

		ItemEntity item = (ItemEntity) ent;
		
		synchronized (teList) {
			for (Iterator<WeakReference<DemagnetizerTileEntity>> iterator = teList.iterator(); iterator.hasNext();) {
				WeakReference<DemagnetizerTileEntity> weakRef = iterator.next();
				
				// Remove te if it has been GC'd
				if (weakRef == null || weakRef.get() == null) {
					iterator.remove();
					continue;
				}
				
				DemagnetizerTileEntity te = weakRef.get();
				
				// Remove te if it has been destroyed
				if (te == null || te.isRemoved()) {
					iterator.remove();
					continue;
				}
				
				// Must be in the same world
				if (!Objects.equals(te.getWorld(), item.getEntityWorld())) {
					continue;
				}

				// If on the client side (empty item), queue processing of this item until the next entity tick
				if (item.getItem().isEmpty()) {
					// Returns true if this TE is valid for this item
					if (te.queueItemClient(item)) {
						return;
					} else {
						continue;
					}
				}
				
				if (te.checkItem(item)) {
					te.demagnetizeItem(item);
					return;
				}
			}
		}
	}
	
	public static void updateBoundingBoxes() {
		synchronized (teList) {
			for (Iterator<WeakReference<DemagnetizerTileEntity>> iterator = teList.iterator(); iterator.hasNext();) {
				WeakReference<DemagnetizerTileEntity> weakRef = iterator.next();
				
				if (weakRef == null || weakRef.get() == null) {
					iterator.remove();
					continue;
				}
				
				DemagnetizerTileEntity ent = weakRef.get();

				if (ent == null || ent.isRemoved()) {
					iterator.remove();
					continue;
				}
				// Force it to recalculate bounding boxes and maximum sizes
				ent.setRange(ent.getRange());
			}
		}
	}

}
