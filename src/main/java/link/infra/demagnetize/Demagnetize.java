package link.infra.demagnetize;

import org.apache.logging.log4j.Logger;

import link.infra.demagnetize.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Demagnetize.MODID, name = Demagnetize.MODNAME, version = Demagnetize.VERSION, useMetadata = true)
public class Demagnetize {

	public static final String MODID = "demagnetize";
	public static final String MODNAME = "Demagnetize";
	public static final String VERSION = "1.12.2-1.0.0.2";

	@Mod.Instance
	public static Demagnetize instance;
	public static Logger logger;

	@SidedProxy(clientSide = "link.infra.demagnetize.proxy.ClientProxy", serverSide = "link.infra.demagnetize.proxy.CommonProxy")
	public static CommonProxy proxy;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		proxy.preInit(event);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent e) {
		proxy.init(e);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent e) {
		proxy.postInit(e);
	}
}
