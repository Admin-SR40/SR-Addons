package com.sraddons.feature.carry

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
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB
import org.apache.logging.log4j.LogManager
import java.awt.Color
import kotlin.math.sqrt

object CarryHighlightRenderer {

    private val LOGGER = LogManager.getLogger("SR-Addons-CarryHL")
    private const val BOSS_TAG = "Spawned by:"

    private val clientFilledXray: RenderType by lazy { createFilledType("carry_client", true) }
    private val clientLinesXray: RenderType by lazy { createLinesType("carry_client", true) }
    private val clientFilled: RenderType by lazy { createFilledType("carry_client", false) }
    private val clientLines: RenderType by lazy { createLinesType("carry_client", false) }

    private val bossFilledXray: RenderType by lazy { createFilledType("carry_boss", true) }
    private val bossLinesXray: RenderType by lazy { createLinesType("carry_boss", true) }
    private val bossFilled: RenderType by lazy { createFilledType("carry_boss", false) }
    private val bossLines: RenderType by lazy { createLinesType("carry_boss", false) }

    private fun createFilledType(id: String, seeThrough: Boolean): RenderType {
        val pipeline = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath("sraddons", if (seeThrough) "${id}_filled_xray" else "${id}_filled"))
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
                .withDepthWrite(!seeThrough)
                .withDepthTestFunction(if (seeThrough) DepthTestFunction.NO_DEPTH_TEST else DepthTestFunction.LEQUAL_DEPTH_TEST)
                .build()
        )
        return RenderType.create(
            "sraddons_${if (seeThrough) "${id}_filled_xray" else "${id}_filled"}",
            RenderSetup.builder(pipeline).createRenderSetup()
        )
    }

    private fun createLinesType(id: String, seeThrough: Boolean): RenderType {
        val pipeline = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath("sraddons", if (seeThrough) "${id}_lines_xray" else "${id}_lines"))
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, VertexFormat.Mode.LINES)
                .withDepthWrite(!seeThrough)
                .withDepthTestFunction(if (seeThrough) DepthTestFunction.NO_DEPTH_TEST else DepthTestFunction.LEQUAL_DEPTH_TEST)
                .build()
        )
        return RenderType.create(
            "sraddons_${if (seeThrough) "${id}_lines_xray" else "${id}_lines"}",
            RenderSetup.builder(pipeline).createRenderSetup()
        )
    }

    fun init() {
        WorldRenderEvents.END_MAIN.register { context ->
            if (!SRConfig.settings.carry.enabled) return@register

            val cfg = SRConfig.settings.carry
            val clientEnabled = cfg.clientHighlight.enabled
            val bossEnabled = cfg.bossHighlight.enabled
            if (!clientEnabled && !bossEnabled) return@register

            val mc = Minecraft.getInstance()
            val world = mc.level ?: return@register
            val player = mc.player ?: return@register

            val entities = world.entitiesForRendering()

            // Collect clients and bosses
            val clientPlayers = if (clientEnabled) findClientPlayers(entities) else emptyList()
            val bossMobs = if (bossEnabled) findBossMobs(entities) else emptyList()

            if (clientPlayers.isEmpty() && bossMobs.isEmpty()) return@register

            val seeThroughWalls = cfg.seeThroughWalls
            val renderMode = cfg.renderMode.uppercase()
            val lineWidth = cfg.lineWidth.coerceIn(1, 10).toFloat()
            val maxDistance = cfg.maxDistance.coerceIn(10, 128)
            val partialTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true)

            val camera = mc.gameRenderer.mainCamera
            val cameraPos = camera.position()
            val poseStack = context.matrices()

            poseStack.pushPose()
            poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z)
            val pose = poseStack.last()

            // Render clients
            if (clientPlayers.isNotEmpty()) {
                val clientColor = Color(
                    cfg.clientHighlight.colorRed.coerceIn(0, 255),
                    cfg.clientHighlight.colorGreen.coerceIn(0, 255),
                    cfg.clientHighlight.colorBlue.coerceIn(0, 255),
                    cfg.clientHighlight.colorAlpha.coerceIn(0, 255)
                )
                val boxes = mutableListOf<AABB>()
                for (entity in clientPlayers) {
                    if (!entity.isAlive) continue
                    try {
                        if (entity.distanceTo(player) > maxDistance) continue
                        boxes.add(getEntityBoundingBox(entity, partialTicks))
                    } catch (e: Exception) {
                        LOGGER.warn("Error calculating bounding box for client ${entity.id}", e)
                    }
                }
                if (boxes.isNotEmpty()) {
                    drawBoxes(pose, boxes, clientColor, renderMode, lineWidth, seeThroughWalls,
                        clientFilled, clientLines, clientFilledXray, clientLinesXray)
                }
            }

            // Render bosses
            if (bossMobs.isNotEmpty()) {
                val bossColor = Color(
                    cfg.bossHighlight.colorRed.coerceIn(0, 255),
                    cfg.bossHighlight.colorGreen.coerceIn(0, 255),
                    cfg.bossHighlight.colorBlue.coerceIn(0, 255),
                    cfg.bossHighlight.colorAlpha.coerceIn(0, 255)
                )
                val boxes = mutableListOf<AABB>()
                for (entity in bossMobs) {
                    if (!entity.isAlive) continue
                    try {
                        if (entity.distanceTo(player) > maxDistance) continue
                        boxes.add(getEntityBoundingBox(entity, partialTicks))
                    } catch (e: Exception) {
                        LOGGER.warn("Error calculating bounding box for boss ${entity.id}", e)
                    }
                }
                if (boxes.isNotEmpty()) {
                    drawBoxes(pose, boxes, bossColor, renderMode, lineWidth, seeThroughWalls,
                        bossFilled, bossLines, bossFilledXray, bossLinesXray)
                }
            }

            poseStack.popPose()
        }
    }

    private fun drawBoxes(
        pose: PoseStack.Pose,
        boxes: List<AABB>,
        color: Color,
        renderMode: String,
        lineWidth: Float,
        seeThroughWalls: Boolean,
        filledType: RenderType,
        linesType: RenderType,
        filledXrayType: RenderType,
        linesXrayType: RenderType
    ) {
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
            LOGGER.error("Unexpected error during carry highlight rendering", e)
        }
    }

    private fun findClientPlayers(entities: Iterable<Entity>): List<LivingEntity> {
        if (CarryState.clients.isEmpty()) return emptyList()
        val result = mutableListOf<LivingEntity>()
        for (entity in entities) {
            if (entity is Player) {
                val name = entity.name.string
                if (CarryState.clients.containsKey(name.lowercase())) {
                    result.add(entity)
                }
            }
        }
        return result
    }

    private fun findBossMobs(entities: Iterable<Entity>): List<LivingEntity> {
        if (CarryState.clients.isEmpty()) return emptyList()
        val bossArmorStands = mutableListOf<Pair<ArmorStand, String>>()

        for (entity in entities) {
            if (entity is ArmorStand) {
                val name = entity.name.string
                val idx = name.indexOf(BOSS_TAG)
                if (idx >= 0) {
                    val playerName = name.substring(idx + BOSS_TAG.length).trim()
                    if (CarryState.clients.containsKey(playerName.lowercase())) {
                        bossArmorStands.add(entity to playerName)
                    }
                }
            }
        }

        val result = mutableListOf<LivingEntity>()
        for ((armorStand, _) in bossArmorStands) {
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
