package gg.moonflower.pollen.api.registry.client.forge;

import gg.moonflower.pollen.core.Pollen;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ApiStatus.Internal
@Mod.EventBusSubscriber(modid = Pollen.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ColorRegistryImpl {

    private static final Set<Consumer<ColorHandlerEvent.Item>> ITEM_COLORS = new HashSet<>();
    private static final Set<Consumer<ColorHandlerEvent.Block>> BLOCK_COLORS = new HashSet<>();

    @SubscribeEvent
    public static void onEvent(ColorHandlerEvent.Item event) {
        ITEM_COLORS.forEach(consumer -> consumer.accept(event));
    }

    @SubscribeEvent
    public static void onEvent(ColorHandlerEvent.Block event) {
        BLOCK_COLORS.forEach(consumer -> consumer.accept(event));
    }

    @SafeVarargs
    public static void register(ItemColor itemColor, Supplier<? extends ItemLike>... items) {
        ITEM_COLORS.add(event -> {
            for (Supplier<? extends ItemLike> item : items) {
                event.getItemColors().register(itemColor, item.get());
            }
        });
    }

    @SafeVarargs
    public static void register(BlockColor blockColor, Supplier<Block>... blocks) {
        BLOCK_COLORS.add(event -> {
            for (Supplier<Block> block : blocks) {
                event.getBlockColors().register(blockColor, block.get());
            }
        });
    }
}
