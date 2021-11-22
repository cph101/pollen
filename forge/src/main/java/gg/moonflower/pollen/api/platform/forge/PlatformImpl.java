package gg.moonflower.pollen.api.platform.forge;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.fmllegacy.LogicalSidedProvider;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.Executor;

@ApiStatus.Internal
public class PlatformImpl {

    public static boolean isProduction() {
        return FMLLoader.isProduction();
    }

    public static Executor getGameExecutor() {
        return LogicalSidedProvider.WORKQUEUE.get(EffectiveSide.get());
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
