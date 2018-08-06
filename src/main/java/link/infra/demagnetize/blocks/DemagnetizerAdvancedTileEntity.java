package link.infra.demagnetize.blocks;

public class DemagnetizerAdvancedTileEntity extends DemagnetizerTileEntity {
	
	// TODO: replace with configuration
	@Override
	public int getMaxRange() {
		return 16;
	}
	
	// TODO: replace with configuration
	// might break NBT though
	@Override
	public int getFilterSize() {
		return 9;
	}

}
