package link.infra.demagnetize.blocks;

import link.infra.demagnetize.ConfigHandler;

public class DemagnetizerAdvancedTileEntity extends DemagnetizerTileEntity {
	
	@Override
	public int getMaxRange() {
		return ConfigHandler.demagnetizerAdvancedRange;
	}
	
	@Override
	public int getFilterSize() {
		return ConfigHandler.demagnetizerAdvancedFilterSlots;
	}

}
