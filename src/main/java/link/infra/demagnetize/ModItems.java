package link.infra.demagnetize;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

class ModItems {
	static final ItemGroup tab = new ItemGroup(Demagnetize.MODID) {
		@Override
		@Nonnull
	    public ItemStack createIcon() {
			return new ItemStack(ModBlocks.DEMAGNETIZER);
	    }
	};

}
