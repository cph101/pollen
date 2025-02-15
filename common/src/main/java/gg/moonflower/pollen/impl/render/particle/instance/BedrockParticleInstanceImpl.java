package gg.moonflower.pollen.impl.render.particle.instance;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import gg.moonflower.molangcompiler.api.MolangEnvironment;
import gg.moonflower.molangcompiler.api.MolangEnvironmentBuilder;
import gg.moonflower.molangcompiler.api.bridge.MolangVariableProvider;
import gg.moonflower.pinwheel.api.particle.ParticleData;
import gg.moonflower.pinwheel.api.particle.component.ParticleComponent;
import gg.moonflower.pinwheel.api.particle.render.ParticleRenderProperties;
import gg.moonflower.pinwheel.api.texture.ModelTexture;
import gg.moonflower.pinwheel.api.texture.TextureTable;
import gg.moonflower.pinwheel.api.transform.MatrixStack;
import gg.moonflower.pollen.api.joml.v1.JomlBridge;
import gg.moonflower.pollen.api.registry.particle.v1.BedrockParticleComponentFactory;
import gg.moonflower.pollen.api.render.geometry.v1.GeometryBufferSource;
import gg.moonflower.pollen.api.render.geometry.v1.GeometryTextureManager;
import gg.moonflower.pollen.api.render.particle.v1.BedrockParticleEmitter;
import gg.moonflower.pollen.api.render.particle.v1.MinecraftSingleQuadRenderProperties;
import gg.moonflower.pollen.api.render.particle.v1.component.BedrockParticleComponent;
import gg.moonflower.pollen.api.render.particle.v1.component.BedrockParticleRenderComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3dc;

import java.util.*;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public class BedrockParticleInstanceImpl extends BedrockParticleImpl {

    public static final ParticleGroup GROUP = new ParticleGroup(10000);
    public static final ParticleRenderType GEOMETRY_SHEET = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
        }

        @Override
        public void end(Tesselator tesselator) {
            Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
            RenderSystem.enableDepthTest();
            Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
        }

        public String toString() {
            return "GEOMETRY_SHEET";
        }
    };
    private static final MatrixStack MATRIX_STACK = MatrixStack.create();
    private static final GeometryBufferSource BUFFER_SOURCE = GeometryBufferSource.particle(Minecraft.getInstance().renderBuffers().bufferSource());
    private static final Matrix4f POSITION = new Matrix4f();
    private static final Map<String, ResourceLocation> MATERIALS = new HashMap<>();

    private final BedrockParticleEmitterImpl emitter;
    private final Set<BedrockParticleRenderComponent> renderComponents;

    @Nullable
    private ParticleRenderProperties renderProperties;

    public BedrockParticleInstanceImpl(BedrockParticleEmitterImpl emitter, ClientLevel clientLevel, double x, double y, double z) {
        super(clientLevel, x, y, z, emitter.getName());
        this.emitter = emitter;
        this.renderComponents = new HashSet<>();
        this.addComponents();
        MolangEnvironmentBuilder<? extends MolangEnvironment> builder = this.environment.edit();
        builder.copy(emitter.getEnvironment()); // create copies of state variables
        builder.setVariables(emitter); // copy references from emitter
        builder.setVariables(this.curves);
        builder.setVariables(this);
    }

    @Override
    protected @Nullable BedrockParticleComponent addComponent(BedrockParticleComponentFactory<?> type, ParticleComponent data) {
        BedrockParticleComponent component = super.addComponent(type, data);
        if (component instanceof BedrockParticleRenderComponent listener) {
            this.renderComponents.add(listener);
        }
        return component;
    }

    @Override
    public void tick() {
        super.tick();
        if ((float) this.age / 20.0F >= this.lifetime.getValue()) {
            this.remove();
        }
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float partialTicks) {
        if (this.age < 0) {
            return;
        }
        ProfilerFiller profiler = this.level.getProfiler();
        profiler.push("pollen");

        this.renderAge.setValue((this.age + partialTicks) / 20F);
        this.curves.evaluate(this.getEnvironment(), profiler);

        profiler.push("components");
        this.renderComponents.forEach(component -> component.render(camera, partialTicks));
        profiler.pop();

        if (this.renderProperties != null) {
            profiler.push("tessellate");
            MATRIX_STACK.pushMatrix();
            Vector3dc pos = this.position(partialTicks);
            Vec3 cameraPos = camera.getPosition();
            float x = (float) (pos.x() - cameraPos.x());
            float y = (float) (pos.y() - cameraPos.y());
            float z = (float) (pos.z() - cameraPos.z());
            MATRIX_STACK.translate(x, y, z);
            if (this.renderProperties instanceof MinecraftSingleQuadRenderProperties properties && properties.canRender()) {
                float zRot = Mth.lerp(partialTicks, this.oRoll, this.roll);
                MATRIX_STACK.translate(0, 0.01, 0);
                MATRIX_STACK.rotate(properties.getRotation());
                MATRIX_STACK.rotate((float) (zRot * Math.PI / 180.0F), 0, 0, 1);
                MATRIX_STACK.scale(properties.getWidth(), properties.getHeight(), 1.0F);
                this.render(properties);
            }
            MATRIX_STACK.popMatrix();
            profiler.pop();
        }

        profiler.pop();
    }

    private void render(MinecraftSingleQuadRenderProperties properties) {
        ParticleData.Description description = this.data.description();
        if (description.material() == null) {
            this.renderQuad(BUFFER_SOURCE.getBuffer(description.texture()), properties);
            return;
        }

        ResourceLocation location = MATERIALS.computeIfAbsent(description.material(), ResourceLocation::tryParse);
        if (location == null) {
            return;
        }

        TextureTable table = GeometryTextureManager.getTextures(location);
        ModelTexture[] textures = table.getLayerTextures("texture");
        if (textures.length == 0) {
            return;
        }

        if (textures.length == 1) {
            this.renderQuad(BUFFER_SOURCE.getBuffer(textures[0]), properties);
        }

        this.renderQuad(VertexMultiConsumer.create(Arrays.stream(textures).map(BUFFER_SOURCE::getBuffer).toArray(VertexConsumer[]::new)), properties);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return GEOMETRY_SHEET;
    }

    @Override
    public Optional<ParticleGroup> getParticleGroup() {
//        return Optional.of(GROUP);
        // TODO use group in 1.19.4
        return Optional.empty();
    }

    private void renderQuad(VertexConsumer consumer, MinecraftSingleQuadRenderProperties properties) {
        float uMin = properties.getUMin();
        float uMax = properties.getUMax();
        float vMin = properties.getVMin();
        float vMax = properties.getVMax();
        float r = properties.getRed();
        float g = properties.getGreen();
        float b = properties.getBlue();
        float a = properties.getAlpha();
        int light = properties.getPackedLight();

        Matrix4f matrix4f = POSITION.set(MATRIX_STACK.position());

        consumer.vertex(matrix4f, -1.0F, -1.0F, 0.0F);
        consumer.uv(uMax, vMax);
        consumer.color(r, g, b, a);
        consumer.uv2(light);
        consumer.endVertex();

        consumer.vertex(matrix4f, -1.0F, 1.0F, 0.0F);
        consumer.uv(uMax, vMin);
        consumer.color(r, g, b, a);
        consumer.uv2(light);
        consumer.endVertex();

        consumer.vertex(matrix4f, 1.0F, 1.0F, 0.0F);
        consumer.uv(uMin, vMin);
        consumer.color(r, g, b, a);
        consumer.uv2(light);
        consumer.endVertex();

        consumer.vertex(matrix4f, 1.0F, -1.0F, 0.0F);
        consumer.uv(uMin, vMax);
        consumer.color(r, g, b, a);
        consumer.uv2(light);
        consumer.endVertex();
    }

    @Override
    protected Component getPrefix() {
        return Component.empty().append(Component.literal("[Particle]").withStyle(ChatFormatting.YELLOW)).append(super.getPrefix());
    }

    @Override
    public void addMolangVariables(MolangVariableProvider.Context context) {
        context.addVariable("particle_age", this.renderAge);
        context.addVariable("particle_lifetime", this.lifetime);
        context.addVariable("particle_random_1", this.random1);
        context.addVariable("particle_random_2", this.random2);
        context.addVariable("particle_random_3", this.random3);
        context.addVariable("particle_random_4", this.random4);
    }

    @Override
    public @Nullable ParticleRenderProperties getRenderProperties() {
        return renderProperties;
    }

    @Override
    public void setRenderProperties(@Nullable ParticleRenderProperties properties) {
        this.renderProperties = properties;
    }

    @Override
    public BedrockParticleEmitter getEmitter() {
        return this.emitter;
    }
}
