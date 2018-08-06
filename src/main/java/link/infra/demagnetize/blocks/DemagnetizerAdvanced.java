package link.infra.demagnetize.blocks;

import link.infra.demagnetize.Demagnetize;
import link.infra.demagnetize.ModItems;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class DemagnetizerAdvanced extends Demagnetizer {
	
	public DemagnetizerAdvanced() {
		super(Material.ROCK);
		setUnlocalizedName(Demagnetize.MODID + ".demagnetizer_advanced");
		setRegistryName("demagnetizer_advanced");
		setCreativeTab(ModItems.tab);
		setHardness(1.0F);
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new DemagnetizerAdvancedTileEntity();
	}

}
