package link.infra.demagnetize.proxy;

import link.infra.demagnetize.Demagnetize;
import link.infra.demagnetize.ModBlocks;
import link.infra.demagnetize.ModItems;
import link.infra.demagnetize.blocks.Demagnetizer;
import link.infra.demagnetize.blocks.DemagnetizerAdvanced;
import link.infra.demagnetize.blocks.DemagnetizerAdvancedTileEntity;
import link.infra.demagnetize.blocks.DemagnetizerTileEntity;
import link.infra.demagnetize.network.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber
public class CommonProxy {
	public void preInit(FMLPreInitializationEvent e) {
		PacketHandler.registerMessages("demagnetize");
	}

	public void init(FMLInitializationEvent e) {
		NetworkRegistry.INSTANCE.registerGuiHandler(Demagnetize.instance, new GuiProxy());
	}

	public void postInit(FMLPostInitializationEvent e) {
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		event.getRegistry().register(new Demagnetizer()
				.setTranslationKey(Demagnetize.MODID + ".demagnetizer")
				.setRegistryName("demagnetizer")
				.setCreativeTab(ModItems.tab));
		event.getRegistry().register(new DemagnetizerAdvanced()
				.setTranslationKey(Demagnetize.MODID + ".demagnetizer_advanced")
				.setRegistryName("demagnetizer_advanced")
				.setCreativeTab(ModItems.tab));
		GameRegistry.registerTileEntity(DemagnetizerTileEntity.class,
				new ResourceLocation(Demagnetize.MODID, "demagnetizertile"));
		GameRegistry.registerTileEntity(DemagnetizerAdvancedTileEntity.class,
				new ResourceLocation(Demagnetize.MODID, "demagnetizer_advancedtile"));
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().register(new ItemBlock(ModBlocks.demagnetizer).setRegistryName(ModBlocks.demagnetizer.getRegistryName()));
		event.getRegistry().register(new ItemBlock(ModBlocks.demagnetizerAdvanced).setRegistryName(ModBlocks.demagnetizerAdvanced.getRegistryName()));
	}
}
