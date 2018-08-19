package link.infra.demagnetize;

import link.infra.demagnetize.blocks.DemagnetizerEventHandler;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.RequiresWorldRestart;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Demagnetize.MODID)
public class ConfigHandler {
	
	// TODO: Send settings server -> client on world join
	
	@Name("Demagnetizer Range")
	@RangeInt(min = 1)
	public static int demagnetizerRange = 4;
	
	@Name("Advanced Demagnetizer Range")
	@RangeInt(min = 1)
	public static int demagnetizerAdvancedRange = 16;
	
	@Name("Demagnetizer Filter Size")
	@RangeInt(min = 1, max = 9)
	@Comment({"The number of filter slots the Demagnetizer has.",
		"Ensure this is changed on all clients and the server."})
	@RequiresWorldRestart
	public static int demagnetizerFilterSlots = 4;
	
	@Name("Advanced Demagnetizer Filter Size")
	@RangeInt(min = 1, max = 9)
	@Comment({"The number of filter slots the Advanced Demagnetizer has.",
		"Ensure this is changed on all clients and the server."})
	@RequiresWorldRestart
	public static int demagnetizerAdvancedFilterSlots = 9;
	
	@Name("Enable Botania Compatability")
	@Comment({"Compatability with Botania's Ring of Magnetization.",
	"This ignores the item filter due to API limitations.",
	"If Botania is not installed, this is ignored.",
	"Ensure this is changed on all clients and the server."})
	@RequiresWorldRestart
	public static boolean enableBotaniaCompat = true;

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
