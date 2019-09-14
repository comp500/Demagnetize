package link.infra.demagnetize.jei;

import link.infra.demagnetize.Demagnetize;
import link.infra.demagnetize.blocks.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Arrays;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin {

	@Override
	@Nonnull
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(Demagnetize.MODID, "main");
	}

	@Override
	public void registerRecipes(IRecipeRegistration registry) {
		registry.addIngredientInfo(Arrays.asList(new ItemStack(ModBlocks.DEMAGNETIZER), new ItemStack(ModBlocks.DEMAGNETIZER_ADVANCED)), VanillaTypes.ITEM,
				"description." + Demagnetize.MODID + ".demagnetizer.1",
				"description." + Demagnetize.MODID + ".demagnetizer.2",
				"description." + Demagnetize.MODID + ".demagnetizer.3");
	}

}
