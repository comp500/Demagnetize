package link.infra.demagnetize.blocks;

import link.infra.demagnetize.Demagnetize;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class Demagnetizer extends BaseEntityBlock {
	private final boolean isAdvanced;

	public Demagnetizer(boolean isAdvanced) {
		super(Properties.of(Material.STONE).strength(1.0F));

		this.isAdvanced = isAdvanced;
		if (isAdvanced) {
			setRegistryName("demagnetizer_advanced");
		} else {
			setRegistryName("demagnetizer");
		}
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
		return new DemagnetizerTileEntity(isAdvanced, pos, state);
	}

	@Override
	@Nonnull
	@SuppressWarnings("deprecation")
	public InteractionResult use(@Nonnull BlockState state, Level world, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult result) {
		if (!world.isClientSide()) {
			BlockEntity tileEntity = world.getBlockEntity(pos);
			if (tileEntity instanceof MenuProvider) {
				NetworkHooks.openGui((ServerPlayer) player, (MenuProvider) tileEntity, tileEntity.getBlockPos());
			} else {
				throw new IllegalStateException("Demagnetizer TileEntity invalid in onBlockActivated position!");
			}
		}
		return InteractionResult.SUCCESS;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(@Nonnull BlockState state, Level worldIn, @Nonnull BlockPos pos, @Nonnull Block neighborBlock, @Nonnull BlockPos neighborPos, boolean isMoving) {
		BlockEntity te = worldIn.getBlockEntity(pos);
		if (te instanceof DemagnetizerTileEntity) {
			int powerLevel = worldIn.getBestNeighborSignal(pos);
			((DemagnetizerTileEntity) te).updateRedstone(powerLevel > 0);
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn) {
		if (stack.getTag() != null) {
			tooltip.add(new TranslatableComponent("tooltip." + Demagnetize.MODID + ".configured").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
		}
	}

	@Nonnull
	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
		BlockEntity te = level.getBlockEntity(pos);
		if (te instanceof DemagnetizerTileEntity) {
			ItemStack stack = new ItemStack(this);
			CompoundTag tagCompound = new CompoundTag();
			CompoundTag tagCompoundFull = te.serializeNBT();


			// Copy only specific tags to tagCompound
			if (tagCompoundFull.contains("items"))
				tagCompound.put("items", tagCompoundFull.getCompound("items"));
			if (tagCompoundFull.contains("redstone"))
				tagCompound.putString("redstone", tagCompoundFull.getString("redstone"));
			if (tagCompoundFull.contains("range"))
				tagCompound.putInt("range", tagCompoundFull.getInt("range"));
			if (tagCompoundFull.contains("whitelist"))
				tagCompound.putBoolean("whitelist", tagCompoundFull.getBoolean("whitelist"));

			stack.addTagElement("BlockEntityTag", tagCompound);
			return stack;
		}
		return super.getCloneItemStack(state, target, level, pos, player);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @Nonnull BlockState state,
																  @Nonnull BlockEntityType<T> type) {
		return level.isClientSide ? null
				: (level0, pos, state0, blockEntity) -> ((DemagnetizerTileEntity) blockEntity).tick();
	}

	@Nonnull
	public RenderShape getRenderShape(@Nonnull BlockState blockState) {
		return RenderShape.MODEL;
	}
}
