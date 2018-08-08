package link.infra.demagnetize.blocks;

import link.infra.demagnetize.ConfigHandler;

public class DemagnetizerAdvancedTileEntity extends DemagnetizerTileEntity {
	
	@Override
	public int getMaxRange() {
		return ConfigHandler.demagnetizerAdvancedRange;
	}
	
	// TODO: replace with configuration
	// might break NBT though
	@Override
	public int getFilterSize() {
		return 9;
	}

}
