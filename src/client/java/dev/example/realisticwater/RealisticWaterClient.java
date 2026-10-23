
package dev.example.realisticwater;

import java.util.Optional;
import java.util.OptionalDouble;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

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

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class RealisticWaterClient implements ClientModInitializer {

    private static final RenderPipeline TEST_PIPELINE =
        RenderPipelines.register(
            RenderPipeline.builder(
                RenderPipelines.DEBUG_FILLED_SNIPPET
            )
                .withLocation(
                    Identifier.fromNamespaceAndPath(
                        RealisticWaterMod.MOD_ID,
                        "pipeline/water_test"
                    )
                )
                .withDepthStencilState(Optional.empty())
                .build()
        );

    private static final Vector4f COLOR_MODULATOR =
        new Vector4f(1f, 1f, 1f, 1f);

    private static final Vector3f MODEL_OFFSET =
        new Vector3f();

    private static final Matrix4f TEXTURE_MATRIX =
        new Matrix4f();

    private static final StagedVertexBuffer STAGED_BUFFER =
        new StagedVertexBuffer(
            () -> "Realistic Water Test Buffer",
            RenderType.SMALL_BUFFER_SIZE
        );

    private WaypointRenderState waypointState;

    @Override
    public void onInitializeClient() {
        LevelExtractionEvents.END_EXTRACTION.register(
            this::extractWaypoint
        );

        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(
            this::renderAndDrawWaypoint
        );

        System.out.println(
            "[Realistic Water] Test renderer initialized!"
        );
    }

    private void extractWaypoint(LevelExtractionContext context) {
        waypointState = new WaypointRenderState(
            0, 100, 0,
            0f, 1f, 0f, 0.75f
        );
    }

    private void renderAndDrawWaypoint(LevelRenderContext context) {
        WaypointRenderState state = waypointState;

        if (state == null) {
            return;
        }

        VertexFormat format =
            TEST_PIPELINE.getVertexFormatBinding(0);

        if (format == null) {
            return;
        }

        PrimitiveTopology primitive =
            TEST_PIPELINE.getPrimitiveTopology();

        StagedVertexBuffer.Draw draw =
            STAGED_BUFFER.appendDraw(
                format,
                primitive,
                primitive == PrimitiveTopology.QUADS
                    ? RenderSystem.getProjectionType().vertexSorting()
                    : null
            );

        renderWaypoint(context, draw, state);

        STAGED_BUFFER.upload();

        StagedVertexBuffer.ExecuteInfo info =
            STAGED_BUFFER.getExecuteInfo(draw);

        if (info != null) {
            drawBuffer(
                Minecraft.getInstance(),
                info,
                TEST_PIPELINE
            );
        }

        STAGED_BUFFER.endFrame();
    }

    private void renderWaypoint(
        LevelRenderContext context,
        StagedVertexBuffer.Draw draw,
        WaypointRenderState state
    ) {
        PoseStack matrices = context.poseStack();
        Vec3 camera = context.levelState().cameraRenderState.pos;

        matrices.pushPose();
        matrices.translate(
            -camera.x,
            -camera.y,
            -camera.z
        );

        VertexConsumer builder =
            STAGED_BUFFER.getVertexBuilder(draw);

        renderFilledBox(
            matrices.last().pose(),
            builder,
            state.x(),
            state.y(),
            state.z(),
            state.x() + 1,
            state.y() + 1,
            state.z() + 1,
            state.r(),
            state.g(),
            state.b(),
            state.a()
        );

        matrices.popPose();
    }

    private static void renderFilledBox(
        Matrix4fc matrix,
        VertexConsumer buffer,
        float minX,
        float minY,
        float minZ,
        float maxX,
        float maxY,
        float maxZ,
        float red,
        float green,
        float blue,
        float alpha
    ) {
        // Front
        buffer.addVertex(matrix, minX, minY, maxZ)
            .setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, maxX, minY, maxZ)
            .setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, maxX, maxY, maxZ)
            .setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, minX, maxY, maxZ)
            .setColor(red, green, blue, alpha);

        // Back
        buffer.addVertex(matrix, maxX, minY, minZ)
            .setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, minX, minY, minZ)
            .setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, minX, maxY, minZ)
            .setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, maxX, maxY, minZ)
            .setColor(red, green, blue, alpha);

        // Left
        buffer.addVertex(matrix, minX, minY, minZ)
            .setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, minX, minY, maxZ)
            .setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, minX, maxY, maxZ)
            .setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, minX, maxY, minZ)
            .setColor(red, green, blue, alpha);

        // Right
        buffer.addVertex(matrix, maxX, minY, maxZ)
            .setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, maxX, minY, minZ)
            .setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, maxX, maxY, minZ)
            .setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, maxX, maxY, maxZ)
            .setColor(red, green, blue, alpha);

        // Top
        buffer.addVertex(matrix, minX, maxY, maxZ)
            .setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, maxX, maxY, maxZ)
            .setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, maxX, maxY, minZ)
            .setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, minX, maxY, minZ)
            .setColor(red, green, blue, alpha);

        // Bottom
        buffer.addVertex(matrix, minX, minY, minZ)
            .setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, maxX, minY, minZ)
            .setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, maxX, minY, maxZ)
            .setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, minX, minY, maxZ)
            .setColor(red, green, blue, alpha);
    }

    private static void drawBuffer(
        Minecraft client,
        StagedVertexBuffer.ExecuteInfo info,
        RenderPipeline pipeline
    ) {
        GpuBufferSlice transforms =
            RenderSystem.getDynamicUniforms().writeTransform(
                RenderSystem.getModelViewMatrixCopy(),
                COLOR_MODULATOR,
                MODEL_OFFSET,
                TEXTURE_MATRIX
            );

        RenderTarget target =
            client.gameRenderer.mainRenderTarget();

        GpuTextureView colorTexture =
            target.getColorTextureView();

        if (colorTexture == null) {
            return;
        }

        try (RenderPass pass =
            RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(
                    () -> "Realistic Water test rendering",
                    colorTexture,
                    Optional.empty(),
                    target.getDepthTextureView(),
                    OptionalDouble.empty()
                )
        ) {
            pass.setPipeline(pipeline);

            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", transforms);

            pass.setVertexBuffer(
                0,
                info.vertexBuffer().slice()
            );

            pass.setIndexBuffer(
                info.indexBuffer(),
                info.indexType()
            );

            pass.drawIndexed(
                info.indexCount(),
                1,
                info.firstIndex(),
                info.baseVertex(),
                0
            );
        }
    }

    public static void close() {
        STAGED_BUFFER.close();
    }

    private record WaypointRenderState(
        int x,
        int y,
        int z,
        float r,
        float g,
        float b,
        float a
    ) {}
}
