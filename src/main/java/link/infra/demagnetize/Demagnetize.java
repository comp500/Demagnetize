package link.infra.demagnetize;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Demagnetize.MODID, name = Demagnetize.MODNAME, version = Demagnetize.VERSION, useMetadata = true)
public class Demagnetize {

	public static final String MODID = "demagnetize";
	public static final String MODNAME = "Demagnetize";
	public static final String VERSION = "1.0.0.0";

	@Mod.Instance
	public static Demagnetize instance;
	public static Logger logger;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent e) {
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent e) {
	}
}
