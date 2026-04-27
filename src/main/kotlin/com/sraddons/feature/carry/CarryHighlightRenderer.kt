package com.sraddons.feature.carry

import com.sraddons.config.SRConfig
import com.sraddons.render.HighlightUtil
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB
import org.apache.logging.log4j.LogManager
import java.awt.Color

object CarryHighlightRenderer {

    private val LOGGER = LogManager.getLogger("SR-Addons-CarryHL")
    private const val BOSS_TAG = "Spawned by:"

    private val clientFilledXray: RenderType by lazy { HighlightUtil.createFilledType("carry_client", true) }
    private val clientLinesXray: RenderType by lazy { HighlightUtil.createLinesType("carry_client", true) }
    private val clientFilled: RenderType by lazy { HighlightUtil.createFilledType("carry_client", false) }
    private val clientLines: RenderType by lazy { HighlightUtil.createLinesType("carry_client", false) }

    private val bossFilledXray: RenderType by lazy { HighlightUtil.createFilledType("carry_boss", true) }
    private val bossLinesXray: RenderType by lazy { HighlightUtil.createLinesType("carry_boss", true) }
    private val bossFilled: RenderType by lazy { HighlightUtil.createFilledType("carry_boss", false) }
    private val bossLines: RenderType by lazy { HighlightUtil.createLinesType("carry_boss", false) }

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

            if (clientPlayers.isNotEmpty()) {
                val clientColor = Color(
                    cfg.clientHighlight.colorRed.coerceIn(0, 255),
                    cfg.clientHighlight.colorGreen.coerceIn(0, 255),
                    cfg.clientHighlight.colorBlue.coerceIn(0, 255),
                    cfg.clientHighlight.colorAlpha.coerceIn(0, 255)
                )
                val boxes = collectBoxes(clientPlayers, player, maxDistance, partialTicks)
                if (boxes.isNotEmpty()) {
                    HighlightUtil.drawBoxes(pose, boxes, clientColor, renderMode, lineWidth, seeThroughWalls,
                        clientFilled, clientLines, clientFilledXray, clientLinesXray, LOGGER)
                }
            }

            if (bossMobs.isNotEmpty()) {
                val bossColor = Color(
                    cfg.bossHighlight.colorRed.coerceIn(0, 255),
                    cfg.bossHighlight.colorGreen.coerceIn(0, 255),
                    cfg.bossHighlight.colorBlue.coerceIn(0, 255),
                    cfg.bossHighlight.colorAlpha.coerceIn(0, 255)
                )
                val boxes = collectBoxes(bossMobs, player, maxDistance, partialTicks)
                if (boxes.isNotEmpty()) {
                    HighlightUtil.drawBoxes(pose, boxes, bossColor, renderMode, lineWidth, seeThroughWalls,
                        bossFilled, bossLines, bossFilledXray, bossLinesXray, LOGGER)
                }
            }

            poseStack.popPose()
        }
    }

    private fun collectBoxes(
        entities: List<LivingEntity>,
        player: net.minecraft.world.entity.player.Player,
        maxDistance: Int,
        partialTicks: Float
    ): List<AABB> {
        val boxes = mutableListOf<AABB>()
        for (entity in entities) {
            if (!entity.isAlive) continue
            try {
                if (entity.distanceTo(player) > maxDistance) continue
                boxes.add(HighlightUtil.getEntityBoundingBox(entity, partialTicks))
            } catch (e: Exception) {
                LOGGER.warn("Error calculating bounding box for entity ${entity.id}", e)
            }
        }
        return boxes
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
        val bossArmorStands = mutableListOf<ArmorStand>()

        for (entity in entities) {
            if (entity is ArmorStand) {
                val name = entity.name.string
                val idx = name.indexOf(BOSS_TAG)
                if (idx >= 0) {
                    val playerName = name.substring(idx + BOSS_TAG.length).trim()
                    if (CarryState.clients.containsKey(playerName.lowercase())) {
                        bossArmorStands.add(entity)
                    }
                }
            }
        }

        val result = mutableListOf<LivingEntity>()
        for (armorStand in bossArmorStands) {
            val target = HighlightUtil.findNearestMobBelow(armorStand, entities)
            if (target != null && target !in result) {
                result.add(target)
            }
        }
        return result
    }
}
