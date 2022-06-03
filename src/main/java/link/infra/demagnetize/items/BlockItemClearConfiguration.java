package link.infra.demagnetize.items;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class BlockItemClearConfiguration extends BlockItem {
	public BlockItemClearConfiguration(Block block, Properties props) {
		super(block, props);
	}

	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
		if (!entity.level.isClientSide) {
			BlockPos pos = entity.getOnPos();
			if (entity.level.getBlockState(pos).getBlock() == Blocks.WATER) {
				stack.setTag(null);
			}
		}
		return super.onEntityItemUpdate(stack, entity);
	}
}
