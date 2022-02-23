package gg.moonflower.pollen.core.network.play;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import gg.moonflower.pollen.api.network.packet.PollinatedPacket;
import gg.moonflower.pollen.api.network.packet.PollinatedPacketContext;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public class ServerboundUpdateSettingsPacket implements PollinatedPacket<PollenServerPlayPacketHandler> {

    private final String entitlement;
    private final JsonObject settings;

    public ServerboundUpdateSettingsPacket(String entitlement, JsonObject settings) {
        this.entitlement = entitlement;
        this.settings = settings;
    }

    public ServerboundUpdateSettingsPacket(FriendlyByteBuf buf) throws IOException {
        this.entitlement = buf.readUtf();
        try {
            this.settings = new JsonParser().parse(buf.readUtf()).getAsJsonObject();
        } catch (JsonParseException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf) throws IOException {
        buf.writeUtf(this.entitlement);
        buf.writeUtf(this.settings.toString());
    }

    @Override
    public void processPacket(PollenServerPlayPacketHandler handler, PollinatedPacketContext ctx) {
        handler.handleUpdateSettingsPacket(this, ctx);
    }

    public String getEntitlement() {
        return entitlement;
    }

    public JsonObject getSettings() {
        return settings;
    }
}
