
package dev.example.realisticwater;

import java.util.Optional;
import java.util.OptionalDouble;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public final class RealisticWaterClient implements ClientModInitializer {

    private static final RenderPipeline WATER_TEST_PIPELINE =
        RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                .withLocation(
                    Identifier.fromNamespaceAndPath(
                        RealisticWaterMod.MOD_ID,
                        "pipeline/water_test"
                    )
                )
                .build()
        );

    private static final Vector4f COLOR_MODULATOR =
        new Vector4f(1F, 1F, 1F, 1F);

    private static final Vector3f MODEL_OFFSET = new Vector3f();

    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();

    private static final StagedVertexBuffer BUFFER =
        new StagedVertexBuffer(
            () -> "Realistic Water Test",
            RenderType.SMALL_BUFFER_SIZE
        );

    private TestRenderState renderState;

    @Override
    public void onInitializeClient() {
        LevelExtractionEvents.END_EXTRACTION.register(
            this::extract
        );

        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(
            this::draw
        );

        System.out.println(
            "[Realistic Water] Client renderer loaded!"
        );
    }

    private void extract(LevelExtractionContext context) {
        // Temporary test surface at world coordinates 0, 100, 0.
        renderState = new TestRenderState(0, 100, 0);
    }

    private void draw(LevelRenderContext context) {
        if (renderState == null) {
            return;
        }

        RenderPipeline pipeline = WATER_TEST_PIPELINE;

        VertexFormat format = pipeline.getVertexFormatBinding(0);
        if (format == null) {
            return;
        }

        PrimitiveTopology primitive = pipeline.getPrimitiveTopology();

        StagedVertexBuffer.Draw draw = BUFFER.appendDraw(
            format,
            primitive,
            primitive == PrimitiveTopology.QUADS
                ? RenderSystem.getProjectionType().vertexSorting()
                : null
        );

        PoseStack poses = context.poseStack();
        Vec3 camera = context.levelState().cameraRenderState.pos;

        poses.pushPose();
        poses.translate(-camera.x, -camera.y, -camera.z);

        VertexConsumer vertices = BUFFER.getVertexBuilder(draw);

        float x = renderState.x();
        float y = renderState.y();
        float z = renderState.z();

        // A one-block blue test cube.
        addFace(poses.last().pose(), vertices,
            x, y, z + 1, x + 1, y, z + 1,
            x + 1, y + 1, z + 1, x, y + 1, z + 1);

        addFace(poses.last().pose(), vertices,
            x + 1, y, z, x, y, z,
            x, y + 1, z, x + 1, y + 1, z);

        addFace(poses.last().pose(), vertices,
            x, y, z, x, y, z + 1,
            x, y + 1, z + 1, x, y + 1, z);

        addFace(poses.last().pose(), vertices,
            x + 1, y, z + 1, x + 1, y, z,
            x + 1, y + 1, z, x + 1, y + 1, z + 1);

        addFace(poses.last().pose(), vertices,
            x, y + 1, z + 1, x + 1, y + 1, z + 1,
            x + 1, y + 1, z, x, y + 1, z);

        addFace(poses.last().pose(), vertices,
            x, y, z, x + 1, y, z,
            x + 1, y, z + 1, x, y, z + 1);

        poses.popPose();

        BUFFER.upload();

        StagedVertexBuffer.ExecuteInfo info =
            BUFFER.getExecuteInfo(draw);

        if (info != null) {
            drawBuffer(Minecraft.getInstance(), info, pipeline);
        }

        BUFFER.endFrame();
    }

    private static void addFace(
        Matrix4fc matrix,
        VertexConsumer vertices,
        float x1, float y1, float z1,
        float x2, float y2, float z2,
        float x3, float y3, float z3,
        float x4, float y4, float z4
    ) {
        // Blue with partial transparency for the test.
        vertices.addVertex(matrix, x1, y1, z1)
            .setColor(0.1F, 0.55F, 1F, 0.65F);

        vertices.addVertex(matrix, x2, y2, z2)
            .setColor(0.1F, 0.55F, 1F, 0.65F);

        vertices.addVertex(matrix, x3, y3, z3)
            .setColor(0.1F, 0.55F, 1F, 0.65F);

        vertices.addVertex(matrix, x4, y4, z4)
            .setColor(0.1F, 0.55F, 1F, 0.65F);
    }

    private static void drawBuffer(
        Minecraft client,
        StagedVertexBuffer.ExecuteInfo info,
        RenderPipeline pipeline
    ) {
        var transforms = RenderSystem.getDynamicUniforms()
            .writeTransform(
                RenderSystem.getModelViewMatrixCopy(),
                COLOR_MODULATOR,
                MODEL_OFFSET,
                TEXTURE_MATRIX
            );

        RenderTarget target = client.gameRenderer.mainRenderTarget();
        GpuTextureView color = target.getColorTextureView();

        if (color == null) {
            return;
        }

        try (RenderPass pass = RenderSystem.getDevice()
            .createCommandEncoder()
            .createRenderPass(
                () -> "Realistic Water Test",
                color,
                Optional.empty(),
                target.getDepthTextureView(),
                OptionalDouble.empty()
            )) {

            pass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", transforms);
            pass.setVertexBuffer(0, info.vertexBuffer().slice());
            pass.setIndexBuffer(info.indexBuffer(), info.indexType());

            pass.drawIndexed(
                info.indexCount(),
                1,
                info.firstIndex(),
                info.baseVertex(),
                0
            );
        }
    }

    private record TestRenderState(int x, int y, int z) {
    }
}
