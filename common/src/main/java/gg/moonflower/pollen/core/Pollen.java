package gg.moonflower.pollen.core;

import gg.moonflower.pollen.api.event.EventDispatcher;
import gg.moonflower.pollen.api.event.events.lifecycle.ServerLifecycleEvent;
import gg.moonflower.pollen.api.platform.Platform;
import gg.moonflower.pollen.api.registry.PollinatedRegistry;
import gg.moonflower.pollen.api.registry.ResourceRegistry;
import gg.moonflower.pollen.api.util.PollinatedModContainer;
import gg.moonflower.pollen.core.brewing.PollenBrewingRecipe;
import gg.moonflower.pollen.core.network.PollenMessages;
import gg.moonflower.pollen.pinwheel.api.client.animation.AnimationManager;
import gg.moonflower.pollen.pinwheel.api.client.geometry.GeometryModelManager;
import gg.moonflower.pollen.pinwheel.api.client.texture.GeometryTextureManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;
import java.util.function.Supplier;

@ApiStatus.Internal
public class Pollen {

    public static final String MOD_ID = "pollen";
    public static final Platform PLATFORM = Platform.builder(Pollen.MOD_ID)
            .commonInit(Pollen::onCommon)
            .clientInit(Pollen::onClient)
            .commonPostInit(Pollen::onCommonPost)
            .clientPostInit(Pollen::onClientPost)
            .build();

    private static final PollinatedRegistry<RecipeSerializer<?>> RECIPE_SERIALIZERS = PollinatedRegistry.create(Registry.RECIPE_SERIALIZER, MOD_ID);

    public static final RecipeType<PollenBrewingRecipe> BREWING = RecipeType.register(MOD_ID + ":brewing");
    public static final Supplier<RecipeSerializer<PollenBrewingRecipe>> BREWING_SERIALIZER = RECIPE_SERIALIZERS.register("brewing", PollenBrewingRecipe::createSerializer);

    private static MinecraftServer server;

    private static void onClient() {
        GeometryModelManager.init();
        GeometryTextureManager.init();
        AnimationManager.init();
        ResourceRegistry.registerBuiltinResourcePack(new ResourceLocation(MOD_ID, "test"), PollinatedModContainer.get(MOD_ID).orElseThrow(IllegalStateException::new), true);
    }

    private static void onCommon() {
        RECIPE_SERIALIZERS.register(PLATFORM);
    }

    private static void onClientPost(Platform.ModSetupContext context) {
    }

    private static void onCommonPost(Platform.ModSetupContext context) {
        EventDispatcher.register(Pollen::onServerStarting);
        EventDispatcher.register(Pollen::onServerStopped);
        PollenMessages.init();
    }

    private static void onServerStarting(ServerLifecycleEvent.Starting event) {
        server = event.getServer();
    }

    private static void onServerStopped(ServerLifecycleEvent.Stopped event) {
        server = null;
    }

    public static Optional<MinecraftServer> getRunningServer() {
        return Optional.ofNullable(server);
    }
}
