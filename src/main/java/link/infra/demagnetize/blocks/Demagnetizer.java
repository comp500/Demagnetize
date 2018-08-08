package link.infra.demagnetize.blocks;

import java.util.List;

import javax.annotation.Nullable;

import link.infra.demagnetize.Demagnetize;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Demagnetizer extends Block implements ITileEntityProvider {

	public Demagnetizer() {
		super(Material.ROCK);
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
			int powerLevel = worldIn.isBlockIndirectlyGettingPowered(pos);
			((DemagnetizerTileEntity) te).updateRedstone(powerLevel > 0);
		}
	}
	
	// https://twitter.com/McJty/status/1002546886161596416
	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof DemagnetizerTileEntity) {
			ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
			NBTTagCompound tagCompound = new NBTTagCompound();
			// writeInternalNBT must be used to avoid setting the block position
			((DemagnetizerTileEntity) te).writeInternalNBT(tagCompound);

			stack.setTagCompound(tagCompound);
			drops.add(stack);
		} else {
			super.getDrops(drops, world, pos, state, fortune);
		}
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		if (willHarvest) {
			return true;
		}
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}
	
	@Override
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
		super.harvestBlock(worldIn, player, pos, state, te, stack);
		worldIn.setBlockToAir(pos);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof DemagnetizerTileEntity && stack.getTagCompound() != null) {
			// readInternalNBT must be used to avoid setting the block position
			((DemagnetizerTileEntity) te).readInternalNBT(stack.getTagCompound());
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if (stack.getTagCompound() != null) {
			tooltip.add(TextFormatting.ITALIC + I18n.format("tooltip." + Demagnetize.MODID + ".configured.name") + TextFormatting.RESET);
		}
	}
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof DemagnetizerTileEntity) {
			ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
			NBTTagCompound tagCompound = new NBTTagCompound();
			// writeInternalNBT must be used to avoid setting the block position
			((DemagnetizerTileEntity) te).writeInternalNBT(tagCompound);

			stack.setTagCompound(tagCompound);
			return stack;
		}
		return super.getPickBlock(state, target, world, pos, player);
	}

}
