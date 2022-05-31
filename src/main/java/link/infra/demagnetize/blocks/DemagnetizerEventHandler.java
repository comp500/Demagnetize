package link.infra.demagnetize.blocks;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.lang.ref.WeakReference;
import java.util.*;

@Mod.EventBusSubscriber
public class DemagnetizerEventHandler {

	private static final List<WeakReference<DemagnetizerTileEntity>> teList = new ArrayList<>();
	private static final Deque<WeakReference<ItemEntity>> itemsPendingMetadataPacket = new ArrayDeque<>();

	static void addTileEntity(DemagnetizerTileEntity te) {
		synchronized (teList) {
			teList.add(new WeakReference<>(te));
		}
	}

	static void removeTileEntity(DemagnetizerTileEntity te) {
		synchronized (teList) {
			for (Iterator<WeakReference<DemagnetizerTileEntity>> iterator = teList.iterator(); iterator.hasNext(); ) {
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
		if (ent instanceof ItemEntity) {
			handleItemSpawn((ItemEntity) ent, false);
		}
	}

	private static void handleItemSpawn(ItemEntity item, boolean fromClientTick) {
		synchronized (teList) {
			for (Iterator<WeakReference<DemagnetizerTileEntity>> iterator = teList.iterator(); iterator.hasNext(); ) {
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
				if (!Objects.equals(te.getLevel(), item.getLevel())) {
					continue;
				}

				// If on the client side (empty item), queue processing of this item until the start of the next client tick
				if (item.getItem().isEmpty() && te.getLevel().isClientSide() && !fromClientTick) {
					if (te.checkItemClientPreMetadata(item)) {
						itemsPendingMetadataPacket.push(new WeakReference<>(item));
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

	@SubscribeEvent
	public static void clientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START && event.side == LogicalSide.CLIENT) {
			while (!itemsPendingMetadataPacket.isEmpty()) {
				ItemEntity item = itemsPendingMetadataPacket.pop().get();
				if (item != null) {
					handleItemSpawn(item, true);
				}
			}
		}
	}

	public static void updateBoundingBoxes() {
		synchronized (teList) {
			for (Iterator<WeakReference<DemagnetizerTileEntity>> iterator = teList.iterator(); iterator.hasNext(); ) {
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
