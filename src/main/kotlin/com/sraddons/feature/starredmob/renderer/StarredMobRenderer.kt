package com.sraddons.feature.starredmob.renderer

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import com.sraddons.config.SRConfig
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.rendertype.RenderSetup
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.phys.AABB
import org.apache.logging.log4j.LogManager
import java.awt.Color
import kotlin.math.sqrt

object StarredMobRenderer {

    private val LOGGER = LogManager.getLogger("SR-Addons-StarredMob")
    private const val STAR_SYMBOL = "\u272f"

    private val filledXrayType: RenderType by lazy { createFilledType(true) }
    private val linesXrayType: RenderType by lazy { createLinesType(true) }
    private val filledType: RenderType by lazy { createFilledType(false) }
    private val linesType: RenderType by lazy { createLinesType(false) }

    private fun createFilledType(seeThrough: Boolean): RenderType {
        val pipeline = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath("sraddons", if (seeThrough) "filled_xray" else "filled"))
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
                .withDepthWrite(!seeThrough)
                .withDepthTestFunction(if (seeThrough) DepthTestFunction.NO_DEPTH_TEST else DepthTestFunction.LEQUAL_DEPTH_TEST)
                .build()
        )
        return RenderType.create(
            "sraddons_${if (seeThrough) "filled_xray" else "filled"}",
            RenderSetup.builder(pipeline).createRenderSetup()
        )
    }

    private fun createLinesType(seeThrough: Boolean): RenderType {
        val pipeline = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath("sraddons", if (seeThrough) "lines_xray" else "lines"))
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, VertexFormat.Mode.LINES)
                .withDepthWrite(!seeThrough)
                .withDepthTestFunction(if (seeThrough) DepthTestFunction.NO_DEPTH_TEST else DepthTestFunction.LEQUAL_DEPTH_TEST)
                .build()
        )
        return RenderType.create(
            "sraddons_${if (seeThrough) "lines_xray" else "lines"}",
            RenderSetup.builder(pipeline).createRenderSetup()
        )
    }

    fun init() {
        WorldRenderEvents.END_MAIN.register { context ->
            if (!SRConfig.settings.starredMob.enabled) return@register

            val mc = Minecraft.getInstance()
            val world = mc.level ?: return@register
            val player = mc.player ?: return@register

            val starredMobs: List<LivingEntity>
            try {
                starredMobs = findStarredMobs(world.entitiesForRendering())
            } catch (e: Exception) {
                LOGGER.error("Error finding starred mobs", e)
                return@register
            }

            if (starredMobs.isEmpty()) return@register

            val color = Color(
                SRConfig.settings.starredMob.colorRed.coerceIn(0, 255),
                SRConfig.settings.starredMob.colorGreen.coerceIn(0, 255),
                SRConfig.settings.starredMob.colorBlue.coerceIn(0, 255),
                SRConfig.settings.starredMob.colorAlpha.coerceIn(0, 255)
            )
            val seeThroughWalls = SRConfig.settings.starredMob.seeThroughWalls
            val renderMode = SRConfig.settings.starredMob.renderMode.uppercase()
            val lineWidth = SRConfig.settings.starredMob.lineWidth.coerceIn(1, 10).toFloat()
            val maxDistance = SRConfig.settings.starredMob.maxDistance.coerceIn(10, 128)
            val partialTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true)

            val camera = mc.gameRenderer.mainCamera
            val cameraPos = camera.position()
            val poseStack = context.matrices()

            poseStack.pushPose()
            poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z)
            val pose = poseStack.last()

            val boxes = mutableListOf<AABB>()
            for (entity in starredMobs) {
                if (!entity.isAlive) continue
                try {
                    if (entity.distanceTo(player) > maxDistance) continue
                    boxes.add(getEntityBoundingBox(entity, partialTicks))
                } catch (e: Exception) {
                    LOGGER.warn("Error calculating bounding box for entity ${entity.id}", e)
                }
            }

            if (boxes.isEmpty()) {
                poseStack.popPose()
                return@register
            }

            try {
                if (renderMode == "FILL" || renderMode == "BOTH") {
                    val buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR)
                    for (box in boxes) {
                        try {
                            drawFilledBox(pose, buffer, box, color)
                        } catch (e: Exception) {
                            LOGGER.warn("Error adding filled box vertices", e)
                        }
                    }
                    val meshData = buffer.build()
                    if (meshData != null) {
                        try {
                            (if (seeThroughWalls) filledXrayType else filledType).draw(meshData)
                        } catch (e: Exception) {
                            LOGGER.error("Error drawing filled boxes", e)
                        }
                    }
                }

                if (renderMode == "OUTLINE" || renderMode == "BOTH") {
                    val buffer = Tesselator.getInstance().begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH)
                    for (box in boxes) {
                        try {
                            drawOutlineBox(pose, buffer, box, color, lineWidth)
                        } catch (e: Exception) {
                            LOGGER.warn("Error adding outline box vertices", e)
                        }
                    }
                    val meshData = buffer.build()
                    if (meshData != null) {
                        try {
                            (if (seeThroughWalls) linesXrayType else linesType).draw(meshData)
                        } catch (e: Exception) {
                            LOGGER.error("Error drawing outline boxes", e)
                        }
                    }
                }
            } catch (e: Exception) {
                LOGGER.error("Unexpected error during starred mob rendering", e)
            } finally {
                poseStack.popPose()
            }
        }
    }

    private fun isDamageNumber(name: String): Boolean {
        val cleaned = name
            .replace(STAR_SYMBOL, "")
            .replace(",", "")
            .replace(" ", "")
            .replace(".", "")
        return cleaned.isNotEmpty() && cleaned.all { it.isDigit() }
    }

    private fun findStarredMobs(entities: Iterable<Entity>): List<LivingEntity> {
        val result = mutableListOf<LivingEntity>()
        val starredArmorStands = mutableListOf<ArmorStand>()

        for (entity in entities) {
            if (entity is ArmorStand) {
                val name = entity.name.string
                if (name.contains(STAR_SYMBOL) && !isDamageNumber(name)) {
                    starredArmorStands.add(entity)
                }
            } else if (entity is LivingEntity) {
                val name = entity.customName?.string ?: entity.name.string
                if (name.contains(STAR_SYMBOL) && entity !in result) {
                    result.add(entity)
                }
            }
        }

        for (armorStand in starredArmorStands) {
            val target = findNearestMobBelow(armorStand, entities)
            if (target != null && target !in result) {
                result.add(target)
            }
        }

        return result
    }

    private fun findNearestMobBelow(armorStand: ArmorStand, entities: Iterable<Entity>): LivingEntity? {
        var closest: LivingEntity? = null
        var closestDist = Double.MAX_VALUE
        val asPos = armorStand.position()

        for (entity in entities) {
            if (entity !is LivingEntity || entity is ArmorStand) continue
            val pos = entity.position()
            val dx = pos.x - asPos.x
            val dz = pos.z - asPos.z
            val dy = asPos.y - pos.y

            if (dx * dx + dz * dz <= 9.0 && dy in 0.0..6.0) {
                val dist = dx * dx + dz * dz + dy * dy
                if (dist < closestDist) {
                    closestDist = dist
                    closest = entity
                }
            }
        }
        return closest
    }

    private fun getEntityBoundingBox(entity: Entity, partialTicks: Float): AABB {
        val x = entity.xOld + (entity.x - entity.xOld) * partialTicks
        val y = entity.yOld + (entity.y - entity.yOld) * partialTicks
        val z = entity.zOld + (entity.z - entity.zOld) * partialTicks
        val offsetX = x - entity.x
        val offsetY = y - entity.y
        val offsetZ = z - entity.z
        return entity.boundingBox.move(offsetX, offsetY, offsetZ)
    }

    private fun drawFilledBox(pose: PoseStack.Pose, buffer: BufferBuilder, box: AABB, color: Color) {
        val minX = box.minX.toFloat()
        val minY = box.minY.toFloat()
        val minZ = box.minZ.toFloat()
        val maxX = box.maxX.toFloat()
        val maxY = box.maxY.toFloat()
        val maxZ = box.maxZ.toFloat()
        val r = color.red
        val g = color.green
        val b = color.blue
        val a = (color.alpha * 0.3f).toInt().coerceIn(0, 255)

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

    private fun drawOutlineBox(pose: PoseStack.Pose, buffer: BufferBuilder, box: AABB, color: Color, lineWidth: Float) {
        val minX = box.minX.toFloat()
        val minY = box.minY.toFloat()
        val minZ = box.minZ.toFloat()
        val maxX = box.maxX.toFloat()
        val maxY = box.maxY.toFloat()
        val maxZ = box.maxZ.toFloat()
        val r = color.red
        val g = color.green
        val b = color.blue
        val a = color.alpha

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
        pose: PoseStack.Pose,
        buffer: BufferBuilder,
        x1: Float, y1: Float, z1: Float,
        x2: Float, y2: Float, z2: Float,
        r: Int, g: Int, b: Int, a: Int,
        lineWidth: Float
    ) {
        val dx = x2 - x1
        val dy = y2 - y1
        val dz = z2 - z1
        val len = sqrt(dx * dx + dy * dy + dz * dz)
        val nx = if (len > 0) dx / len else 0f
        val ny = if (len > 0) dy / len else 0f
        val nz = if (len > 0) dz / len else 0f

        buffer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setNormal(nx, ny, nz).setLineWidth(lineWidth)
        buffer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setNormal(nx, ny, nz).setLineWidth(lineWidth)
    }
}
