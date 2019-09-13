package link.infra.demagnetize;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import link.infra.demagnetize.blocks.DemagnetizerEventHandler;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import java.nio.file.Path;

@Mod.EventBusSubscriber
public class ConfigHandler {
	private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

	static ForgeConfigSpec COMMON_CONFIG;

	private static final String CATEGORY_GENERAL = "general";
	public static final ForgeConfigSpec.IntValue DEMAGNETIZER_RANGE;
	public static final ForgeConfigSpec.IntValue DEMAGNETIZER_ADVANCED_RANGE;
	public static final ForgeConfigSpec.IntValue DEMAGNETIZER_FILTER_SLOTS;
	public static final ForgeConfigSpec.IntValue DEMAGNETIZER_ADVANCED_FILTER_SLOTS;

	static {
		COMMON_BUILDER.comment("General settings").push(CATEGORY_GENERAL);
		DEMAGNETIZER_RANGE = COMMON_BUILDER.comment("Demagnetizer Range")
				.defineInRange("demagnetizerRange", 4, 1, Integer.MAX_VALUE);
		DEMAGNETIZER_ADVANCED_RANGE = COMMON_BUILDER.comment("Advanced Demagnetizer Range")
				.defineInRange("demagnetizerAdvancedRange", 16, 1, Integer.MAX_VALUE);
		DEMAGNETIZER_FILTER_SLOTS = COMMON_BUILDER.comment("Demagnetizer Filter Size")
				.comment("The number of filter slots the Demagnetizer has, ensure this is changed on all clients and the server.")
				.defineInRange("demagnetizerFilterSlots", 4, 1, 9);
		DEMAGNETIZER_ADVANCED_FILTER_SLOTS = COMMON_BUILDER.comment("Advanced Demagnetizer Filter Size")
				.comment("The number of filter slots the Advanced Demagnetizer has, ensure this is changed on all clients and the server.")
				.defineInRange("demagnetizerAdvancedFilterSlots", 9, 1, 9);
		COMMON_BUILDER.pop();

		COMMON_CONFIG = COMMON_BUILDER.build();
	}

	static void loadConfig(ForgeConfigSpec spec, Path path) {
		final CommentedFileConfig configData = CommentedFileConfig.builder(path)
				.sync()
				.autosave()
				.writingMode(WritingMode.REPLACE)
				.build();

		configData.load();
		spec.setConfig(configData);
	}

	@SubscribeEvent
	public static void onReload(final ModConfig.ConfigReloading configEvent) {
		DemagnetizerEventHandler.updateBoundingBoxes();
	}

}
