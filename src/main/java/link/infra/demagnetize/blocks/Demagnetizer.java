package link.infra.demagnetize.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class Demagnetizer extends Block {
	private final boolean isAdvanced;

	public Demagnetizer(boolean isAdvanced) {
		super(Properties.create(Material.ROCK).hardnessAndResistance(1.0F));

		this.isAdvanced = isAdvanced;
		if (isAdvanced) {
			setRegistryName("demagnetizer_advanced");
		} else {
			setRegistryName("demagnetizer");
		}
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new DemagnetizerTileEntity(isAdvanced);
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
		if (!world.isRemote) {
			TileEntity tileEntity = world.getTileEntity(pos);
			if (tileEntity instanceof INamedContainerProvider) {
				NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) tileEntity, tileEntity.getPos());
			} else {
				throw new IllegalStateException("Demagnetizer TileEntity invalid in onBlockActivated position!");
			}
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof DemagnetizerTileEntity) {
			int powerLevel = worldIn.getRedstonePowerFromNeighbors(pos);
			((DemagnetizerTileEntity) te).updateRedstone(powerLevel > 0);
		}
	}
//
//	// https://twitter.com/McJty/status/1002546886161596416
//	@Override
//	public void getDrops(@Nonnull NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, @Nonnull BlockState state, int fortune) {
//		TileEntity te = world.getTileEntity(pos);
//		if (te instanceof DemagnetizerTileEntity) {
//			ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
//			CompoundNBT tagCompound = new CompoundNBT();
//			// writeInternalNBT must be used to avoid setting the block position
//			((DemagnetizerTileEntity) te).writeInternalNBT(tagCompound);
//
//			stack.setTagCompound(tagCompound);
//			drops.add(stack);
//		} else {
//			super.getDrops(drops, world, pos, state, fortune);
//		}
//	}
//
//	@Override
//	public boolean removedByPlayer(@Nonnull BlockState state, World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, boolean willHarvest) {
//		if (willHarvest) {
//			return true;
//		}
//		return super.removedByPlayer(state, world, pos, player, false);
//	}
//
//	@Override
//	public void harvestBlock(@Nonnull World worldIn, PlayerEntity player, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable TileEntity te, ItemStack stack) {
//		super.harvestBlock(worldIn, player, pos, state, te, stack);
//		worldIn.setBlockToAir(pos);
//	}
//
//	@Override
//	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
//		TileEntity te = worldIn.getTileEntity(pos);
//		if (te instanceof DemagnetizerTileEntity && stack.getTagCompound() != null) {
//			// readInternalNBT must be used to avoid setting the block position
//			((DemagnetizerTileEntity) te).readInternalNBT(stack.getTagCompound());
//		}
//	}
//
//	@Override
//	@SideOnly(Side.CLIENT)
//	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
//		if (stack.getTagCompound() != null) {
//			tooltip.add(TextFormatting.ITALIC + I18n.format("tooltip." + Demagnetize.MODID + ".configured") + TextFormatting.RESET);
//		}
//	}
//
//	@Nonnull
//	@Override
//	public ItemStack getPickBlock(@Nonnull BlockState state, RayTraceResult target, World world, @Nonnull BlockPos pos, PlayerEntity player) {
//		TileEntity te = world.getTileEntity(pos);
//		if (te instanceof DemagnetizerTileEntity) {
//			ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
//			CompoundNBT tagCompound = new CompoundNBT();
//			// writeInternalNBT must be used to avoid setting the block position
//			((DemagnetizerTileEntity) te).writeInternalNBT(tagCompound);
//
//			stack.setTagCompound(tagCompound);
//			return stack;
//		}
//		return super.getPickBlock(state, target, world, pos, player);
//	}

}
