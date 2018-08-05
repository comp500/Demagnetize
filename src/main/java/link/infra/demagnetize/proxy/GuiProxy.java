package link.infra.demagnetize.proxy;

import link.infra.demagnetize.blocks.DemagnetizerContainer;
import link.infra.demagnetize.blocks.DemagnetizerGui;
import link.infra.demagnetize.blocks.DemagnetizerTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiProxy implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof DemagnetizerTileEntity) {
			return new DemagnetizerContainer(player.inventory, (DemagnetizerTileEntity) te);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof DemagnetizerTileEntity) {
			DemagnetizerTileEntity containerTileEntity = (DemagnetizerTileEntity) te;
			return new DemagnetizerGui(containerTileEntity,
					new DemagnetizerContainer(player.inventory, containerTileEntity));
		}
		return null;
	}
}
