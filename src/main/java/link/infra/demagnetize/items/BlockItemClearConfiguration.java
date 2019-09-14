package link.infra.demagnetize.items;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class BlockItemClearConfiguration extends BlockItem {
	public BlockItemClearConfiguration(Block block, Properties props) {
		super(block, props);
	}

	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
		if (!entity.world.isRemote) {
			BlockPos pos = entity.getPosition();
			if (entity.world.getBlockState(pos).getBlock() == Blocks.WATER) {
				stack.setTag(null);
			}
		}
		return super.onEntityItemUpdate(stack, entity);
	}
}
