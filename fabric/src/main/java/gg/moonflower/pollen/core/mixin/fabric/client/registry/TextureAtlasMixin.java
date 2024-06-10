package gg.moonflower.pollen.core.mixin.fabric.client.registry;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gg.moonflower.pollen.api.event.registry.v1.RegisterAtlasSpriteEvent;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(TextureAtlas.class)
public abstract class TextureAtlasMixin {

    @Shadow
    public abstract ResourceLocation location();

    @WrapOperation(method = "upload", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;", ordinal = 0))
    public Collection<SpriteContents> injectSprites(Map<ResourceLocation, SpriteContents> instance, Operation<Collection<SpriteContents>> original) {
        RegisterAtlasSpriteEvent.event(this.location()).invoker().registerSprites((TextureAtlas) (Object) this, (loc, resourceManager) -> {
            try {
                instance.put(
                        loc,
                        SpriteLoader.loadSprite(loc, resourceManager.getResourceOrThrow(loc))
                );
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        return instance.values();
    }
}
