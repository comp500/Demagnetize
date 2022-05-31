package link.infra.demagnetize;

import link.infra.demagnetize.blocks.*;
import link.infra.demagnetize.items.BlockItemClearConfiguration;
import link.infra.demagnetize.items.ModItems;
import link.infra.demagnetize.network.PacketHandler;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
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
		MenuScreens.register(ModBlocks.DEMAGNETIZER_CONTAINER, DemagnetizerGui::new);
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
			Item.Properties properties = new Item.Properties().tab(ModItems.tab);
			event.getRegistry().register(new BlockItemClearConfiguration(ModBlocks.DEMAGNETIZER, properties).setRegistryName("demagnetizer"));
			event.getRegistry().register(new BlockItemClearConfiguration(ModBlocks.DEMAGNETIZER_ADVANCED, properties).setRegistryName("demagnetizer_advanced"));
		}

		@SubscribeEvent
		public static void onTileEntityRegistry(final RegistryEvent.Register<BlockEntityType<?>> event) {
			// For some reason the parameter to build() is marked as @Nonnull
			//noinspection ConstantConditions
			event.getRegistry().register(BlockEntityType.Builder.of((blockPos, blockState) -> new DemagnetizerTileEntity(false, blockPos, blockState), ModBlocks.DEMAGNETIZER).build(null).setRegistryName("demagnetizer"));
			//noinspection ConstantConditions
			event.getRegistry().register(BlockEntityType.Builder.of((blockPos, blockState) -> new DemagnetizerTileEntity(true, blockPos, blockState), ModBlocks.DEMAGNETIZER_ADVANCED).build(null).setRegistryName("demagnetizer_advanced"));
		}

		@SubscribeEvent
		public static void onContainerRegistry(final RegistryEvent.Register<MenuType<?>> event) {
			event.getRegistry().register(IForgeMenuType.create((windowId, inv, data) -> {
				BlockPos pos = data.readBlockPos();
				return new DemagnetizerContainer(windowId, inv.player.level, pos, inv);
			}).setRegistryName("demagnetizer"));
		}
	}
}
