package com.sraddons.feature.carry

import com.sraddons.config.SRConfig
import com.sraddons.config.toColor
import com.sraddons.render.HighlightUtil
import com.sraddons.util.TitleUtil
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB
import org.apache.logging.log4j.LogManager
import java.util.concurrent.ConcurrentHashMap

object CarryHighlightRenderer {

    private val LOGGER = LogManager.getLogger("SR-Addons-CarryHL")
    private const val BOSS_TAG = "Spawned by:"
    private const val BOSS_UUID_PRUNE_INTERVAL = 1200 // 60 seconds at 20 TPS
    private val seenBossUUIDs = HashSet<java.util.UUID>()
    private var ticksSinceBossPrune = 0

    // Hypixel SkyBlock Slayer miniboss name tags, driven by config.
    // Set-based lookup provides O(1) contains() for entity matching in the render loop.

    private val clientFilled: RenderType by lazy { HighlightUtil.createFilledType("carry_client") }
    private val clientLines: RenderType by lazy { HighlightUtil.createLinesType("carry_client") }

    private val bossFilled: RenderType by lazy { HighlightUtil.createFilledType("carry_boss") }
    private val bossLines: RenderType by lazy { HighlightUtil.createLinesType("carry_boss") }

    private val minibossFilled: RenderType by lazy { HighlightUtil.createFilledType("carry_miniboss") }
    private val minibossLines: RenderType by lazy { HighlightUtil.createLinesType("carry_miniboss") }

    fun init() {
        WorldRenderEvents.END_MAIN.register { context ->
            if (!SRConfig.settings.carry.enabled) return@register

            val cfg = SRConfig.settings.carry
            val clientEnabled = cfg.clientHighlight.enabled
            val bossEnabled = cfg.bossHighlight.enabled
            val minibossEnabled = cfg.minibossHighlight.enabled
            if (!clientEnabled && !bossEnabled && !minibossEnabled) return@register

            val mc = Minecraft.getInstance()
            val world = mc.level ?: return@register
            val player = mc.player ?: return@register

            val entities = world.entitiesForRendering()

            val clientPlayers = if (clientEnabled || minibossEnabled) findClientPlayers(entities) else emptyList()
            val bossArmorStands = if (bossEnabled) findBossArmorStands(entities) else emptyList()
            val bossMobs = if (bossArmorStands.isNotEmpty()) findBossMobs(bossArmorStands, entities) else emptyList()
            val minibosses = if (minibossEnabled) findMinibosses(entities, clientPlayers, cfg.minibossMaxDistance.coerceIn(4, 32)) else emptyList()

            // Boss spawn notification — incremental tracking
            if (bossEnabled && cfg.bossSpawnNotification) {
                for (stand in bossArmorStands) {
                    if (seenBossUUIDs.add(stand.uuid)) {
                        triggerBossSpawnNotification()
                        break
                    }
                }
                ticksSinceBossPrune++
                if (ticksSinceBossPrune >= BOSS_UUID_PRUNE_INTERVAL) {
                    ticksSinceBossPrune = 0
                    val currentIds = bossArmorStands.mapTo(HashSet()) { it.uuid }
                    seenBossUUIDs.retainAll(currentIds)
                }
            }

            if (clientPlayers.isEmpty() && bossMobs.isEmpty() && minibosses.isEmpty()) return@register

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

            if (clientEnabled && clientPlayers.isNotEmpty()) {
                renderGroup(pose, clientPlayers, cfg.clientHighlight, maxDistance, partialTicks, renderMode, lineWidth,
                    clientFilled, clientLines)
            }
            if (bossMobs.isNotEmpty()) {
                renderGroup(pose, bossMobs, cfg.bossHighlight, maxDistance, partialTicks, renderMode, lineWidth,
                    bossFilled, bossLines)
            }
            if (minibosses.isNotEmpty()) {
                renderGroup(pose, minibosses, cfg.minibossHighlight, maxDistance, partialTicks, renderMode, lineWidth,
                    minibossFilled, minibossLines)
            }

            poseStack.popPose()
        }
    }

    private fun renderGroup(
        pose: com.mojang.blaze3d.vertex.PoseStack.Pose,
        entities: List<LivingEntity>,
        config: SRConfig.CarryHighlightConfig,
        maxDistance: Int, partialTicks: Float,
        renderMode: String, lineWidth: Float,
        filledType: net.minecraft.client.renderer.rendertype.RenderType,
        linesType: net.minecraft.client.renderer.rendertype.RenderType
    ) {
        val color = config.toColor()
        val boxes = HighlightUtil.collectBoxes(entities, Minecraft.getInstance().player!!, maxDistance, partialTicks, LOGGER)
        if (boxes.isNotEmpty()) {
            HighlightUtil.drawBoxes(pose, boxes, color, renderMode, lineWidth,
                filledType, linesType, LOGGER)
        }
    }

    private fun findClientPlayers(entities: Iterable<Entity>): List<LivingEntity> {
        if (CarryState.clients.isEmpty()) return emptyList()
        val result = mutableListOf<LivingEntity>()
        for (entity in entities) {
            if (entity is Player) {
                val nameLower = entity.name.string.lowercase()
                if (CarryState.clients.containsKey(nameLower)) {
                    result.add(entity)
                }
            }
        }
        return result
    }

    private fun findBossMobs(bossArmorStands: List<ArmorStand>, entities: Iterable<Entity>): List<LivingEntity> {
        val result = mutableListOf<LivingEntity>()
        for (armorStand in bossArmorStands) {
            val target = HighlightUtil.findNearestMobBelow(armorStand, entities)
            if (target != null && target !in result) {
                result.add(target)
            }
        }
        return result
    }

    private fun findBossArmorStands(entities: Iterable<Entity>): List<ArmorStand> {
        if (CarryState.clients.isEmpty()) return emptyList()
        return entities.filterIsInstance<ArmorStand>().filter { stand ->
            val name = stand.name.string
            val idx = name.indexOf(BOSS_TAG)
            if (idx < 0) return@filter false
            val playerNameLower = name.substring(idx + BOSS_TAG.length).trim().lowercase()
            CarryState.clients.containsKey(playerNameLower)
        }
    }

    private fun triggerBossSpawnNotification() {
        TitleUtil.showSubtitle(SRConfig.settings.carry.bossSpawnNotificationText.trim())
    }

    private fun findMinibosses(entities: Iterable<Entity>, clientPlayers: List<LivingEntity>, maxDistance: Int): List<LivingEntity> {
        if (clientPlayers.isEmpty()) return emptyList()
        val minibossArmorStands = entities.filterIsInstance<ArmorStand>().filter { stand ->
            val name = stand.name.string
            CarryState.minibossNames.any { name.contains(it, ignoreCase = true) }
        }
        if (minibossArmorStands.isEmpty()) return emptyList()

        val maxDistSq = (maxDistance * maxDistance).toDouble()
        val result = mutableListOf<LivingEntity>()
        for (armorStand in minibossArmorStands) {
            val nearAnyClient = clientPlayers.any { armorStand.distanceToSqr(it) <= maxDistSq }
            if (!nearAnyClient) continue
            val target = HighlightUtil.findNearestMobBelow(armorStand, entities)
            if (target != null && target !in result) {
                result.add(target)
            }
        }
        return result
    }
}
