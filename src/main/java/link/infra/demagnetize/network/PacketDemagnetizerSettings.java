package link.infra.demagnetize.network;

import io.netty.buffer.ByteBuf;
import link.infra.demagnetize.Demagnetize;
import link.infra.demagnetize.blocks.DemagnetizerTileEntity;
import link.infra.demagnetize.blocks.DemagnetizerTileEntity.RedstoneStatus;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketDemagnetizerSettings implements IMessage {
	
	private int range;
	private RedstoneStatus redstoneSetting = RedstoneStatus.REDSTONE_DISABLED;
	private boolean whitelist;
	private BlockPos demagnetizerBlockPos;

	@Override
	public void fromBytes(ByteBuf buf) {
		range = buf.readInt();
		try {
			redstoneSetting = RedstoneStatus.valueOf(ByteBufUtils.readUTF8String(buf));
		} catch (IllegalArgumentException e) {
			// Ignore
		}
		whitelist = buf.readBoolean();
		demagnetizerBlockPos = BlockPos.fromLong(buf.readLong());
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(range);
		ByteBufUtils.writeUTF8String(buf, redstoneSetting.name());
		buf.writeBoolean(whitelist);
		buf.writeLong(demagnetizerBlockPos.toLong());
	}

	@SuppressWarnings("unused")
	public PacketDemagnetizerSettings() {
		// for server initialisation
	}

	public PacketDemagnetizerSettings(int range, RedstoneStatus redstoneSetting, boolean whitelist, BlockPos demagnetizerBlockPos) {
		this.range = range;
		this.redstoneSetting = redstoneSetting;
		this.whitelist = whitelist;
		this.demagnetizerBlockPos = demagnetizerBlockPos;
	}

	public static class Handler implements IMessageHandler<PacketDemagnetizerSettings, IMessage> {
		@Override
		public IMessage onMessage(PacketDemagnetizerSettings message, MessageContext ctx) {
			// Always use a construct like this to actually handle your message. This ensures that
			// your 'handle' code is run on the main Minecraft thread. 'onMessage' itself
			// is called on the networking thread so it is not safe to do a lot of things
			// here.
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}

		private void handle(PacketDemagnetizerSettings message, MessageContext ctx) {
			// This code is run on the server side. So you can do server-side calculations here
			EntityPlayerMP playerEntity = ctx.getServerHandler().player;
			World world = playerEntity.getEntityWorld();
			// Check if the block (chunk) is loaded to prevent abuse from a client
			// trying to overload a server by randomly loading chunks
			if (world.isBlockLoaded(message.demagnetizerBlockPos)) {
				TileEntity te = world.getTileEntity(message.demagnetizerBlockPos);
				if (te instanceof DemagnetizerTileEntity) {
					DemagnetizerTileEntity demagTE = (DemagnetizerTileEntity) te;
					demagTE.setRange(message.range);
					demagTE.setRedstoneSetting(message.redstoneSetting);
					demagTE.setWhitelist(message.whitelist);
					demagTE.updateBlock();
				} else {
					Demagnetize.logger.warn("Player tried to change settings of something that isn't a demagnetizer (or doesn't have a TE)!");
				}
			}
		}
	}
}
