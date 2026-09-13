package com.sraddons.render

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.rendertype.RenderSetup
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.util.ARGB
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.shapes.Shapes
import org.apache.logging.log4j.Logger

object HighlightUtil {
    private const val MAX_HORIZONTAL_DIST_SQ = 9.0
    private const val MIN_VERTICAL_OFFSET = 0.0
    private const val MAX_VERTICAL_OFFSET = 6.0

    /** FILL 模式的填充 alpha 乘数 */
    private const val FILL_ALPHA = 0.3f

    fun createFilledType(id: String): RenderType =
        RenderType.create(
            "sraddons_${id}_filled",
            RenderSetup.builder(RenderPipelines.DEBUG_FILLED_BOX).createRenderSetup(),
        )

    fun createLinesType(id: String): RenderType =
        RenderType.create(
            "sraddons_${id}_lines",
            RenderSetup.builder(RenderPipelines.LINES).createRenderSetup(),
        )

    fun getEntityBoundingBox(
        entity: Entity,
        partialTicks: Float,
    ): AABB {
        val x = entity.xOld + (entity.x - entity.xOld) * partialTicks
        val y = entity.yOld + (entity.y - entity.yOld) * partialTicks
        val z = entity.zOld + (entity.z - entity.zOld) * partialTicks
        return entity.boundingBox.move(x - entity.x, y - entity.y, z - entity.z)
    }

    fun findNearestMobBelow(
        armorStand: ArmorStand,
        entities: Iterable<Entity>,
    ): LivingEntity? {
        var closest: LivingEntity? = null
        var closestDist = Double.MAX_VALUE
        val asPos = armorStand.position()

        for (entity in entities) {
            if (entity !is LivingEntity || entity is ArmorStand || entity ===
                net.minecraft.client.Minecraft
                    .getInstance()
                    .player
            ) {
                continue
            }
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
        logger: Logger,
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

    /**
     * 26.2 及以上：FILL 模式通过 submitCustomGeometry 手动绘制面，
     * OUTLINE 模式通过 submitShapeOutline 绘制线框。
     */
    fun drawBoxes(
        collector: SubmitNodeCollector,
        poseStack: PoseStack,
        boxes: List<AABB>,
        color: Int,
        renderMode: String,
        lineWidth: Float,
        filledType: RenderType,
        linesType: RenderType,
        logger: Logger,
    ) {
        try {
            for (box in boxes) {
                if (renderMode == "FILL" || renderMode == "BOTH") {
                    collector.submitCustomGeometry(poseStack, filledType) { pose, vc ->
                        drawFilledBox(pose, vc, box, color)
                    }
                }
                if (renderMode == "OUTLINE" || renderMode == "BOTH") {
                    val shape = Shapes.create(box)
                    collector.submitShapeOutline(poseStack, shape, linesType, color, lineWidth, false)
                }
            }
        } catch (e: Exception) {
            logger.error("Error drawing highlight boxes", e)
        }
    }

    /** 手动绘制填充 AABB（6 面 × 4 顶点 = 24 顶点） */
    private fun drawFilledBox(
        pose: PoseStack.Pose,
        vc: com.mojang.blaze3d.vertex.VertexConsumer,
        box: AABB,
        color: Int,
    ) {
        val m = pose.pose()
        val r = ARGB.red(color)
        val g = ARGB.green(color)
        val b = ARGB.blue(color)
        val a = (ARGB.alpha(color) * FILL_ALPHA).toInt().coerceIn(0, 255)
        val x1 = box.minX.toFloat()
        val y1 = box.minY.toFloat()
        val z1 = box.minZ.toFloat()
        val x2 = box.maxX.toFloat()
        val y2 = box.maxY.toFloat()
        val z2 = box.maxZ.toFloat()

        // Bottom
        vc.addVertex(m, x1, y1, z1).setColor(r, g, b, a)
        vc.addVertex(m, x2, y1, z1).setColor(r, g, b, a)
        vc.addVertex(m, x2, y1, z2).setColor(r, g, b, a)
        vc.addVertex(m, x1, y1, z2).setColor(r, g, b, a)
        // Top
        vc.addVertex(m, x1, y2, z1).setColor(r, g, b, a)
        vc.addVertex(m, x1, y2, z2).setColor(r, g, b, a)
        vc.addVertex(m, x2, y2, z2).setColor(r, g, b, a)
        vc.addVertex(m, x2, y2, z1).setColor(r, g, b, a)
        // North (-Z)
        vc.addVertex(m, x1, y1, z1).setColor(r, g, b, a)
        vc.addVertex(m, x1, y2, z1).setColor(r, g, b, a)
        vc.addVertex(m, x2, y2, z1).setColor(r, g, b, a)
        vc.addVertex(m, x2, y1, z1).setColor(r, g, b, a)
        // South (+Z)
        vc.addVertex(m, x1, y1, z2).setColor(r, g, b, a)
        vc.addVertex(m, x2, y1, z2).setColor(r, g, b, a)
        vc.addVertex(m, x2, y2, z2).setColor(r, g, b, a)
        vc.addVertex(m, x1, y2, z2).setColor(r, g, b, a)
        // West (-X)
        vc.addVertex(m, x1, y1, z1).setColor(r, g, b, a)
        vc.addVertex(m, x1, y1, z2).setColor(r, g, b, a)
        vc.addVertex(m, x1, y2, z2).setColor(r, g, b, a)
        vc.addVertex(m, x1, y2, z1).setColor(r, g, b, a)
        // East (+X)
        vc.addVertex(m, x2, y1, z1).setColor(r, g, b, a)
        vc.addVertex(m, x2, y2, z1).setColor(r, g, b, a)
        vc.addVertex(m, x2, y2, z2).setColor(r, g, b, a)
        vc.addVertex(m, x2, y1, z2).setColor(r, g, b, a)
    }
}
