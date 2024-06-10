package gg.moonflower.pollen.api.joml.v1;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

/**
 * Bridges the vanilla Minecraft math types with JOML. In Minecraft 1.19.4 this is no longer necessary.
 *
 * @author Ocelot
 * @since 2.0.0
 */
public interface JomlBridge {

    static Quaternionf quaternionFromAxisAngle(Vector3f axis, float rotationAngle, boolean degrees) {
        if (degrees) {
            rotationAngle *= 0.017453292F;
        }

        Quaternionf it = new Quaternionf();

        float f = Mth.sin(rotationAngle / 2.0F);
        it.x = axis.x() * f;
        it.y = axis.y() * f;
        it.z = axis.z() * f;
        it.w = Mth.cos(rotationAngle / 2.0F);

        return it;
    }

    static Vec3 set(Vec3 minecraftVector, Vector3dc jomlVector) {
        minecraftVector = new Vec3(jomlVector.x(), jomlVector.y(), jomlVector.z());
        return minecraftVector;
    }

    static Vector3d set(Vector3d jomlVector, Vec3 minecraftVector) {
        return jomlVector.set(minecraftVector.x, minecraftVector.y, minecraftVector.z);
    }
}
