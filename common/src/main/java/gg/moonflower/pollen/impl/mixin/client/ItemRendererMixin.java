package gg.moonflower.pollen.impl.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import gg.moonflower.pollen.api.registry.render.v1.ItemRendererRegistry;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @Shadow
    @Final
    private ItemModelShaper itemModelShaper;

    @Unique
    private Item pollen$capturedItem;

    @Unique
    private Item pollen$capturedHandItem;

    @Inject(method = "render", at = @At("HEAD"))
    public void capture(ItemStack itemStack, ItemDisplayContext itemDisplayContext, boolean bl, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, BakedModel bakedModel, CallbackInfo ci) {
        this.pollen$capturedItem = itemStack.getItem();
    }

    @ModifyVariable(method = "render", ordinal = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z", ordinal = 0, shift = At.Shift.BEFORE), argsOnly = true)
    public BakedModel render(BakedModel original) {
        if (ItemRendererRegistry.getHandModel(this.pollen$capturedItem) != null) {
            return this.itemModelShaper.getItemModel(this.pollen$capturedItem);
        }
        return original;
    }

    @Inject(method = "getModel", at = @At("HEAD"))
    public void capture(ItemStack itemStack, Level level, LivingEntity livingEntity, int i, CallbackInfoReturnable<BakedModel> cir) {
        this.pollen$capturedHandItem = itemStack.getItem();
    }

    @ModifyVariable(method = "getModel", ordinal = 0, at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/renderer/ItemModelShaper;getItemModel(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/resources/model/BakedModel;", shift = At.Shift.AFTER))
    public BakedModel getModel(BakedModel original) {
        ModelResourceLocation modelLocation = ItemRendererRegistry.getHandModel(this.pollen$capturedHandItem);
        if (modelLocation != null) {
            return this.itemModelShaper.getModelManager().getModel(modelLocation);
        }
        return original;
    }
}
