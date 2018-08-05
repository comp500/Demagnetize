package link.infra.demagnetize.blocks;

import link.infra.demagnetize.Demagnetize;
import link.infra.demagnetize.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Demagnetizer extends Block implements ITileEntityProvider {

	public Demagnetizer() {
		super(Material.ROCK);
		setUnlocalizedName(Demagnetize.MODID + ".demagnetizer");
		setRegistryName("demagnetizer");
		setCreativeTab(ModItems.tab);
		setHardness(1.0F);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new DemagnetizerTileEntity();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		// Only execute on the server
		if (world.isRemote) {
			return true;
		}
		TileEntity te = world.getTileEntity(pos);
		if (!(te instanceof DemagnetizerTileEntity)) {
			return false;
		}
		player.openGui(Demagnetize.instance, DemagnetizerGui.GUI_ID, world, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}
	
	@SideOnly(Side.CLIENT)
	public void initModel() {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof DemagnetizerTileEntity) {
			((DemagnetizerTileEntity) te).updateRedstone(worldIn.isBlockPowered(pos));
		}
	}
	
	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing sid) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof DemagnetizerTileEntity) {
			return ((DemagnetizerTileEntity) te).isRedstoneEnabled();
		}
		return false;
	}

}
