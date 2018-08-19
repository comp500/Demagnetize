package link.infra.demagnetize.blocks;

import net.minecraft.tileentity.TileEntity;

// Used so that DemagnetizerTileEntity can call functions without explicitly importing Botania classes
public interface IDemagnetizerSolegnoliaCompat {
	
	public void setRange(int range);
	
	public void setActive(boolean active);
	
	public void onUpdate();
	
	public void setSupertile(TileEntity te);

}
