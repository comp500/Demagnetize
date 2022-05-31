package link.infra.demagnetize.network;

import link.infra.demagnetize.Demagnetize;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
	private static int packetId = 0;

	private static final String PROTOCOL_VERSION = "2";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Demagnetize.MODID, "main"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);

	private PacketHandler() {
	}

	public static void registerMessages() {
		INSTANCE.registerMessage(packetId++, PacketDemagnetizerSettings.class, PacketDemagnetizerSettings::encode, PacketDemagnetizerSettings::decode, PacketDemagnetizerSettings::handle);
	}
}