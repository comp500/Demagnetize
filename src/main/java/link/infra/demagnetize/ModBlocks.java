package link.infra.demagnetize;

import link.infra.demagnetize.blocks.Demagnetizer;
import link.infra.demagnetize.blocks.DemagnetizerContainer;
import link.infra.demagnetize.blocks.DemagnetizerTileEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class ModBlocks {
	@ObjectHolder("demagnetize:demagnetizer")
	public static Demagnetizer DEMAGNETIZER;

	@ObjectHolder("demagnetize:demagnetizer_advanced")
	public static Demagnetizer DEMAGNETIZER_ADVANCED;

	@ObjectHolder("demagnetize:demagnetizer")
	public static TileEntityType<DemagnetizerTileEntity> DEMAGNETIZER_TILE_ENTITY;

	@ObjectHolder("demagnetize:demagnetizer_advanced")
	public static TileEntityType<DemagnetizerTileEntity> DEMAGNETIZER_ADVANCED_TILE_ENTITY;

	@ObjectHolder("demagnetize:demagnetizer")
	public static ContainerType<DemagnetizerContainer> DEMAGNETIZER_CONTAINER;
}
