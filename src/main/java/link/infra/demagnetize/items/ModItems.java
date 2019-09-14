package link.infra.demagnetize.items;

import link.infra.demagnetize.Demagnetize;
import link.infra.demagnetize.blocks.ModBlocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ModItems {
	public static final ItemGroup tab = new ItemGroup(Demagnetize.MODID) {
		@Override
		@Nonnull
	    public ItemStack createIcon() {
			return new ItemStack(ModBlocks.DEMAGNETIZER);
	    }
	};

}
