package link.infra.demagnetize.items;

import link.infra.demagnetize.Demagnetize;
import link.infra.demagnetize.blocks.ModBlocks;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class ModItems {
	public static final CreativeModeTab tab = new CreativeModeTab(Demagnetize.MODID) {
		@Override
		@Nonnull
		public ItemStack makeIcon() {
			return new ItemStack(ModBlocks.DEMAGNETIZER);
		}
	};

}
