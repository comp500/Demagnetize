package link.infra.demagnetize;

import link.infra.demagnetize.blocks.DemagnetizerEventHandler;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Demagnetize.MODID)
public class ConfigHandler {
	
	@Name("Demagnetizer Range")
	@RangeInt(min = 1)
	public static int demagnetizerRange = 4;
	
	@Name("Advanced Demagnetizer Range")
	@RangeInt(min = 1)
	public static int demagnetizerAdvancedRange = 16;

	@Mod.EventBusSubscriber
	private static class EventHandler {

		// Sync configuration to gui and update demagnetizers
		@SubscribeEvent
		public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
			if (event.getModID().equals(Demagnetize.MODID)) {
				ConfigManager.sync(Demagnetize.MODID, Config.Type.INSTANCE);
				DemagnetizerEventHandler.updateBoundingBoxes();
			}
		}
	}

}
