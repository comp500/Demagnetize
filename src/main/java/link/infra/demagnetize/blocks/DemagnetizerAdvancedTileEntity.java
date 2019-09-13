package link.infra.demagnetize.blocks;

import link.infra.demagnetize.ConfigHandler;

public class DemagnetizerAdvancedTileEntity extends DemagnetizerTileEntity {

	public DemagnetizerAdvancedTileEntity() {
		super(true);
	}
	
	@Override
	public int getMaxRange() {
		return ConfigHandler.DEMAGNETIZER_ADVANCED_RANGE.get();
	}
	
	@Override
	public int getFilterSize() {
		return ConfigHandler.DEMAGNETIZER_ADVANCED_FILTER_SLOTS.get();
	}

}
