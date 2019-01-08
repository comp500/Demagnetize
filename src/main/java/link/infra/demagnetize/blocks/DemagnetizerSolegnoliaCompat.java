package link.infra.demagnetize.blocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.common.block.subtile.functional.SubTileSolegnolia;

// Imitates a Solegnolia to disable the Botania magnet
public class DemagnetizerSolegnoliaCompat extends SubTileSolegnolia {
	
	private int range;
	
	@Override
	public double getRange() {
		return range;
	}
	
	public void setRange(int range) {
		// Range is +1 of normal range due to how hasSolegnoliaAround calculates AABBs
		this.range = range + 1;
	}
	
	// For setting redstone signal from settings in TE
	public void setActive(boolean active) {
		// isSolegnoliaAround checks if redstoneSignal == 0
		this.redstoneSignal = active ? 0 : 1;
	}
	
	// Control redstone manually
	@Override
	public boolean acceptsRedstone() {
		return false;
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
	}
	
	// no-op some functions
	@SideOnly(Side.CLIENT)
	@Override
	public void renderHUD(Minecraft mc, ScaledResolution res) {}
	
	@Override
	public boolean canSelect(EntityPlayer player, ItemStack wand, BlockPos pos, EnumFacing side) { return false; }
	
	@Override
	public boolean bindTo(EntityPlayer player, ItemStack wand, BlockPos pos, EnumFacing side) { return false; }
	
	@Override
	public LexiconEntry getEntry() { return null; }

}
