package link.infra.demagnetize.blocks;

import link.infra.demagnetize.Demagnetize;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

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
	@Nonnull
	@SuppressWarnings("deprecation")
	public ActionResultType onBlockActivated(@Nonnull BlockState state, World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand hand, @Nonnull BlockRayTraceResult result) {
		if (!world.isRemote) {
			TileEntity tileEntity = world.getTileEntity(pos);
			if (tileEntity instanceof INamedContainerProvider) {
				NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) tileEntity, tileEntity.getPos());
			} else {
				throw new IllegalStateException("Demagnetizer TileEntity invalid in onBlockActivated position!");
			}
		}
		return ActionResultType.SUCCESS;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(@Nonnull BlockState state, World worldIn, @Nonnull BlockPos pos, @Nonnull Block neighborBlock, @Nonnull BlockPos neighborPos, boolean isMoving) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof DemagnetizerTileEntity) {
			int powerLevel = worldIn.getRedstonePowerFromNeighbors(pos);
			((DemagnetizerTileEntity) te).updateRedstone(powerLevel > 0);
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn) {
		if (stack.getTag() != null) {
			tooltip.add(new TranslationTextComponent("tooltip." + Demagnetize.MODID + ".configured").applyTextStyles(TextFormatting.ITALIC, TextFormatting.GRAY));
		}
	}

	@Nonnull
	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof DemagnetizerTileEntity) {
			ItemStack stack = new ItemStack(this);
			CompoundNBT tagCompound = new CompoundNBT();
			CompoundNBT tagCompoundFull = new CompoundNBT();
			te.write(tagCompoundFull);

			// Copy only specific tags to tagCompound
			if (tagCompoundFull.contains("items"))
				tagCompound.put("items", tagCompoundFull.getCompound("items"));
			if (tagCompoundFull.contains("redstone"))
				tagCompound.putString("redstone", tagCompoundFull.getString("redstone"));
			if (tagCompoundFull.contains("range"))
				tagCompound.putInt("range", tagCompoundFull.getInt("range"));
			if (tagCompoundFull.contains("whitelist"))
				tagCompound.putBoolean("whitelist", tagCompoundFull.getBoolean("whitelist"));

			stack.setTagInfo("BlockEntityTag", tagCompound);
			return stack;
		}
		return super.getPickBlock(state, target, world, pos, player);
	}

}
