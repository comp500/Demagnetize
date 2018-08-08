package link.infra.demagnetize.blocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class DemagnetizerAdvanced extends Demagnetizer {
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new DemagnetizerAdvancedTileEntity();
	}

}
