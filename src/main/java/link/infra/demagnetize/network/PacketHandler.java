package link.infra.demagnetize.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
    private static int packetId = 0;
    private static int nextID() {
        return packetId++;
    }

    public static SimpleNetworkWrapper INSTANCE = null;

    private PacketHandler() {}

    public static void registerMessages(String channelName) {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(channelName);
        registerMessages();
    }

    private static void registerMessages() {
        // Register messages which are sent from the client to the server here:
        INSTANCE.registerMessage(PacketDemagnetizerSettings.Handler.class, PacketDemagnetizerSettings.class, nextID(), Side.SERVER);
    }
}