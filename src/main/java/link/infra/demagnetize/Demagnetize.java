package link.infra.demagnetize;

import link.infra.demagnetize.blocks.Demagnetizer;
import link.infra.demagnetize.blocks.DemagnetizerContainer;
import link.infra.demagnetize.blocks.DemagnetizerGui;
import link.infra.demagnetize.blocks.DemagnetizerTileEntity;
import link.infra.demagnetize.network.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(Demagnetize.MODID)
public class Demagnetize {
	public static final String MODID = "demagnetize";

	public Demagnetize() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupCommon);

		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_CONFIG);
		ConfigHandler.loadConfig(ConfigHandler.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve("demagnetize-common.toml"));
	}

	private void setupClient(final FMLClientSetupEvent event) {
		ScreenManager.registerFactory(ModBlocks.DEMAGNETIZER_CONTAINER, DemagnetizerGui::new);
	}

	private void setupCommon(final FMLCommonSetupEvent event) {
		PacketHandler.registerMessages();
	}

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class RegistryEvents {
		@SubscribeEvent
		public static void onBlocksRegistry(final RegistryEvent.Register<Block> event) {
			event.getRegistry().register(new Demagnetizer(false));
			event.getRegistry().register(new Demagnetizer(true));
		}

		@SubscribeEvent
		public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
			Item.Properties properties = new Item.Properties().group(ModItems.tab);
			event.getRegistry().register(new BlockItem(ModBlocks.DEMAGNETIZER, properties).setRegistryName("demagnetizer"));
			event.getRegistry().register(new BlockItem(ModBlocks.DEMAGNETIZER_ADVANCED, properties).setRegistryName("demagnetizer_advanced"));
		}

		@SubscribeEvent
		public static void onTileEntityRegistry(final RegistryEvent.Register<TileEntityType<?>> event) {
			// For some reason the parameter to build() is marked as @Nonnull
			//noinspection ConstantConditions
			event.getRegistry().register(TileEntityType.Builder.create(() -> new DemagnetizerTileEntity(false), ModBlocks.DEMAGNETIZER).build(null).setRegistryName("demagnetizer"));
			//noinspection ConstantConditions
			event.getRegistry().register(TileEntityType.Builder.create(() -> new DemagnetizerTileEntity(true), ModBlocks.DEMAGNETIZER).build(null).setRegistryName("demagnetizer_advanced"));
		}

		@SubscribeEvent
		public static void onContainerRegistry(final RegistryEvent.Register<ContainerType<?>> event) {
			event.getRegistry().register(IForgeContainerType.create((windowId, inv, data) -> {
				BlockPos pos = data.readBlockPos();
				return new DemagnetizerContainer(windowId, Minecraft.getInstance().world, pos, inv);
			}).setRegistryName("demagnetizer"));
		}
	}
}
