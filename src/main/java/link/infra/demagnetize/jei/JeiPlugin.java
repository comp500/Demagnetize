package link.infra.demagnetize.jei;

import java.util.Arrays;

import link.infra.demagnetize.Demagnetize;
import link.infra.demagnetize.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class JeiPlugin implements IModPlugin {

	@Override
	public void register(IModRegistry registry) {
		registry.addIngredientInfo(Arrays.asList(new ItemStack(ModBlocks.demagnetizer), new ItemStack(ModBlocks.demagnetizerAdvanced)), VanillaTypes.ITEM, 
				"description." + Demagnetize.MODID + ".demagnetizer.1",
				"description." + Demagnetize.MODID + ".demagnetizer.2",
				"description." + Demagnetize.MODID + ".demagnetizer.3");
	}

}
