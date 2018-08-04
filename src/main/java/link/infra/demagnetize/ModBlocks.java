package link.infra.demagnetize;

import link.infra.demagnetize.blocks.Demagnetizer;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModBlocks {
	
	@GameRegistry.ObjectHolder("demagnetize:demagnetizer")
	public static Demagnetizer demagnetizer;
	
	@SideOnly(Side.CLIENT)
    public static void initModels() {
		demagnetizer.initModel();
	}

}
