package link.infra.demagnetize.jei;

import link.infra.demagnetize.Demagnetize;
import link.infra.demagnetize.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import net.minecraft.item.ItemStack;
import java.util.Arrays;

@JEIPlugin
public class JeiPlugin implements IModPlugin {

	@Override
	public void register(IModRegistry registry) {
		registry.addIngredientInfo(Arrays.asList(new ItemStack(ModBlocks.demagnetizer), new ItemStack(ModBlocks.demagnetizerAdvanced)), ItemStack.class, 
				"description." + Demagnetize.MODID + ".demagnetizer.1",
				"description." + Demagnetize.MODID + ".demagnetizer.2",
				"description." + Demagnetize.MODID + ".demagnetizer.3");
	}

}
