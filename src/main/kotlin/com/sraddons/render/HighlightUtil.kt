package com.sraddons.render

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.pipeline.DepthStencilState
import com.mojang.blaze3d.platform.CompareOp
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.rendertype.RenderSetup
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.phys.AABB
import net.minecraft.util.ARGB
import org.apache.logging.log4j.Logger

object HighlightUtil {

    private const val MAX_HORIZONTAL_DIST_SQ = 9.0
    private const val MIN_VERTICAL_OFFSET = 0.0
    private const val MAX_VERTICAL_OFFSET = 6.0
    private const val FILL_ALPHA_MULTIPLIER = 0.3f

    fun createFilledType(id: String): RenderType {
        val pipeline = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath("sraddons", "${id}_filled"))
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
                .withDepthStencilState(DepthStencilState(
                    CompareOp.LESS_THAN_OR_EQUAL,
                    true
                ))
                .build()
        )
        return RenderType.create(
            "sraddons_${id}_filled",
            RenderSetup.builder(pipeline).createRenderSetup()
        )
    }

    fun createLinesType(id: String): RenderType {
        val pipeline = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath("sraddons", "${id}_lines"))
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, VertexFormat.Mode.LINES)
                .withDepthStencilState(DepthStencilState(
                    CompareOp.LESS_THAN_OR_EQUAL,
                    true
                ))
                .build()
        )
        return RenderType.create(
            "sraddons_${id}_lines",
            RenderSetup.builder(pipeline).createRenderSetup()
        )
    }

    fun getEntityBoundingBox(entity: Entity, partialTicks: Float): AABB {
        val x = entity.xOld + (entity.x - entity.xOld) * partialTicks
        val y = entity.yOld + (entity.y - entity.yOld) * partialTicks
        val z = entity.zOld + (entity.z - entity.zOld) * partialTicks
        return entity.boundingBox.move(x - entity.x, y - entity.y, z - entity.z)
    }

    fun findNearestMobBelow(armorStand: ArmorStand, entities: Iterable<Entity>): LivingEntity? {
        var closest: LivingEntity? = null
        var closestDist = Double.MAX_VALUE
        val asPos = armorStand.position()

        for (entity in entities) {
            if (entity !is LivingEntity || entity is ArmorStand || entity == Minecraft.getInstance().player) continue
            val pos = entity.position()
            val dx = pos.x - asPos.x
            val dz = pos.z - asPos.z
            val dy = asPos.y - pos.y

            if (dx * dx + dz * dz <= MAX_HORIZONTAL_DIST_SQ && dy in MIN_VERTICAL_OFFSET..MAX_VERTICAL_OFFSET) {
                val dist = dx * dx + dz * dz + dy * dy
                if (dist < closestDist) {
                    closestDist = dist
                    closest = entity
                }
            }
        }
        return closest
    }

    fun collectBoxes(
        entities: List<LivingEntity>,
        player: net.minecraft.world.entity.player.Player,
        maxDistance: Int,
        partialTicks: Float,
        logger: Logger
    ): List<AABB> {
        val boxes = mutableListOf<AABB>()
        for (entity in entities) {
            if (!entity.isAlive) continue
            try {
                if (entity.distanceTo(player) > maxDistance) continue
                boxes.add(getEntityBoundingBox(entity, partialTicks))
            } catch (e: Exception) {
                logger.warn("Error calculating bounding box for entity {}", entity.id, e)
            }
        }
        return boxes
    }

    fun drawBoxes(
        pose: PoseStack.Pose,
        boxes: List<AABB>,
        color: Int,
        renderMode: String,
        lineWidth: Float,
        filledType: RenderType,
        linesType: RenderType,
        logger: Logger
    ) {
        try {
            if (renderMode == "FILL" || renderMode == "BOTH") {
                val buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR)
                for (box in boxes) {
                    try {
                        drawFilledBox(pose, buffer, box, color)
                    } catch (e: Exception) {
                        logger.warn("Error adding filled box vertices", e)
                    }
                }
                val meshData = buffer.build()
                if (meshData != null) {
                    try {
                        filledType.draw(meshData)
                    } catch (e: Exception) {
                        logger.error("Error drawing filled boxes", e)
                    }
                }
            }

            if (renderMode == "OUTLINE" || renderMode == "BOTH") {
                val buffer = Tesselator.getInstance().begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH)
                for (box in boxes) {
                    try {
                        drawOutlineBox(pose, buffer, box, color, lineWidth)
                    } catch (e: Exception) {
                        logger.warn("Error adding outline box vertices", e)
                    }
                }
                val meshData = buffer.build()
                if (meshData != null) {
                    try {
                        linesType.draw(meshData)
                    } catch (e: Exception) {
                        logger.error("Error drawing outline boxes", e)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Unexpected error during highlight rendering", e)
        }
    }

    private fun drawFilledBox(pose: PoseStack.Pose, buffer: BufferBuilder, box: AABB, color: Int) {
        val minX = box.minX.toFloat()
        val minY = box.minY.toFloat()
        val minZ = box.minZ.toFloat()
        val maxX = box.maxX.toFloat()
        val maxY = box.maxY.toFloat()
        val maxZ = box.maxZ.toFloat()
        val r = ARGB.red(color)
        val g = ARGB.green(color)
        val b = ARGB.blue(color)
        val a = (ARGB.alpha(color) * FILL_ALPHA_MULTIPLIER).toInt().coerceIn(0, 255)

        // Bottom face
        buffer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a)
        buffer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a)
        buffer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a)
        buffer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a)
        buffer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a)
        buffer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a)

        // Top face
        buffer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a)
        buffer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a)
        buffer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a)
        buffer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a)
        buffer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a)
        buffer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a)

        // North face
        buffer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a)
        buffer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a)
        buffer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a)
        buffer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a)
        buffer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a)
        buffer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a)

        // South face
        buffer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a)
        buffer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a)
        buffer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a)
        buffer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a)
        buffer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a)
        buffer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a)

        // West face
        buffer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a)
        buffer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a)
        buffer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a)
        buffer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a)
        buffer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a)
        buffer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a)

        // East face
        buffer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a)
        buffer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a)
        buffer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a)
        buffer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a)
        buffer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a)
        buffer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a)
    }

    private fun drawOutlineBox(pose: PoseStack.Pose, buffer: BufferBuilder, box: AABB, color: Int, lineWidth: Float) {
        val minX = box.minX.toFloat()
        val minY = box.minY.toFloat()
        val minZ = box.minZ.toFloat()
        val maxX = box.maxX.toFloat()
        val maxY = box.maxY.toFloat()
        val maxZ = box.maxZ.toFloat()
        val r = ARGB.red(color)
        val g = ARGB.green(color)
        val b = ARGB.blue(color)
        val a = ARGB.alpha(color)

        // Bottom edges
        addLine(pose, buffer, minX, minY, minZ, maxX, minY, minZ, r, g, b, a, lineWidth)
        addLine(pose, buffer, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a, lineWidth)
        addLine(pose, buffer, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a, lineWidth)
        addLine(pose, buffer, minX, minY, maxZ, minX, minY, minZ, r, g, b, a, lineWidth)

        // Top edges
        addLine(pose, buffer, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a, lineWidth)
        addLine(pose, buffer, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a, lineWidth)
        addLine(pose, buffer, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a, lineWidth)
        addLine(pose, buffer, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a, lineWidth)

        // Vertical edges
        addLine(pose, buffer, minX, minY, minZ, minX, maxY, minZ, r, g, b, a, lineWidth)
        addLine(pose, buffer, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a, lineWidth)
        addLine(pose, buffer, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a, lineWidth)
        addLine(pose, buffer, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a, lineWidth)
    }

    private fun addLine(
        pose: PoseStack.Pose, buffer: BufferBuilder,
        x1: Float, y1: Float, z1: Float,
        x2: Float, y2: Float, z2: Float,
        r: Int, g: Int, b: Int, a: Int,
        lineWidth: Float
    ) {
        buffer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setNormal(0f, 1f, 0f).setLineWidth(lineWidth)
        buffer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setNormal(0f, 1f, 0f).setLineWidth(lineWidth)
    }
}
