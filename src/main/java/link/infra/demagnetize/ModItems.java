package link.infra.demagnetize;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ModItems {
	public static final CreativeTabs tab = new CreativeTabs(Demagnetize.MODID) {
	    @Override 
	    public ItemStack getTabIconItem() {
	        return new ItemStack(Item.getItemFromBlock(ModBlocks.demagnetizer));
	    }
	};

}
