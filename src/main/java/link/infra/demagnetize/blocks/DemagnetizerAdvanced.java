package link.infra.demagnetize.blocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class DemagnetizerAdvanced extends Demagnetizer {
	
	@Override
	public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
		return new DemagnetizerAdvancedTileEntity();
	}

}
