package gg.moonflower.pollen.api.render.item.v1;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * @since 2.0.0
 */
public interface DynamicItemRenderer {

    void render(ItemStack stack, ItemDisplayContext displayContext, PoseStack matrixStack, MultiBufferSource multiBufferSource, int packedLight, int combinedOverlay);
}
