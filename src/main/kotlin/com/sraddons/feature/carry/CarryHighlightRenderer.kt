package com.sraddons.feature.carry

import com.sraddons.config.SRConfig
import com.sraddons.render.HighlightUtil
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
    private const val MAX_SEEN_BOSSES = 200
    private val seenBossUUIDs = ConcurrentHashMap.newKeySet<java.util.UUID>()

    // Hypixel SkyBlock 19 Slayer miniboss name tags used for entity matching.
    // These are server-side name strings; kept as a set for O(1) contains() lookup in the render loop.
    private val MINIBOSS_NAMES = setOf(
        "Revenant Sycophant", "Revenant Champion", "Deformed Revenant",
        "Atoned Champion", "Atoned Revenant",
        "Tarantula Vermin", "Tarantula Beast", "Mutant Tarantula",
        "Primordial Jockey", "Primordial Viscount",
        "Pack Enforcer", "Sven Follower", "Sven Alpha",
        "Voidling Devotee", "Voidling Radical", "Voidcrazed Maniac",
        "Flare Demon", "Kindleheart Demon", "Burningsoul Demon"
    )

    private val clientFilledXray: RenderType by lazy { HighlightUtil.createFilledType("carry_client", true) }
    private val clientLinesXray: RenderType by lazy { HighlightUtil.createLinesType("carry_client", true) }
    private val clientFilled: RenderType by lazy { HighlightUtil.createFilledType("carry_client", false) }
    private val clientLines: RenderType by lazy { HighlightUtil.createLinesType("carry_client", false) }

    private val bossFilledXray: RenderType by lazy { HighlightUtil.createFilledType("carry_boss", true) }
    private val bossLinesXray: RenderType by lazy { HighlightUtil.createLinesType("carry_boss", true) }
    private val bossFilled: RenderType by lazy { HighlightUtil.createFilledType("carry_boss", false) }
    private val bossLines: RenderType by lazy { HighlightUtil.createLinesType("carry_boss", false) }

    private val minibossFilledXray: RenderType by lazy { HighlightUtil.createFilledType("carry_miniboss", true) }
    private val minibossLinesXray: RenderType by lazy { HighlightUtil.createLinesType("carry_miniboss", true) }
    private val minibossFilled: RenderType by lazy { HighlightUtil.createFilledType("carry_miniboss", false) }
    private val minibossLines: RenderType by lazy { HighlightUtil.createLinesType("carry_miniboss", false) }

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
            val bossMobs = if (bossEnabled) findBossMobs(entities) else emptyList()
            val minibosses = if (minibossEnabled) findMinibosses(entities, clientPlayers, cfg.minibossMaxDistance.coerceIn(4, 32)) else emptyList()

            // Boss spawn notification
            if (bossEnabled && cfg.bossSpawnNotification) {
                val currentStands = findBossArmorStands(entities)
                if (currentStands.isEmpty()) {
                    seenBossUUIDs.clear()
                } else {
                    val currentUUIDs = currentStands.map { it.uuid }.toSet()
                    for (uuid in currentUUIDs) {
                        if (uuid !in seenBossUUIDs) {
                            triggerBossSpawnNotification()
                            break
                        }
                    }
                    seenBossUUIDs.clear()
                    if (currentUUIDs.size <= MAX_SEEN_BOSSES) {
                        seenBossUUIDs.addAll(currentUUIDs)
                    }
                }
            }

            if (clientPlayers.isEmpty() && bossMobs.isEmpty() && minibosses.isEmpty()) return@register

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

            if (clientEnabled && clientPlayers.isNotEmpty()) {
                val clientColor = HighlightUtil.clampedColor(
                    cfg.clientHighlight.colorRed,
                    cfg.clientHighlight.colorGreen,
                    cfg.clientHighlight.colorBlue,
                    cfg.clientHighlight.colorAlpha
                )
                val boxes = collectBoxes(clientPlayers, player, maxDistance, partialTicks)
                if (boxes.isNotEmpty()) {
                    HighlightUtil.drawBoxes(pose, boxes, clientColor, renderMode, lineWidth, seeThroughWalls,
                        clientFilled, clientLines, clientFilledXray, clientLinesXray, LOGGER)
                }
            }

            if (bossMobs.isNotEmpty()) {
                val bossColor = HighlightUtil.clampedColor(
                    cfg.bossHighlight.colorRed,
                    cfg.bossHighlight.colorGreen,
                    cfg.bossHighlight.colorBlue,
                    cfg.bossHighlight.colorAlpha
                )
                val boxes = collectBoxes(bossMobs, player, maxDistance, partialTicks)
                if (boxes.isNotEmpty()) {
                    HighlightUtil.drawBoxes(pose, boxes, bossColor, renderMode, lineWidth, seeThroughWalls,
                        bossFilled, bossLines, bossFilledXray, bossLinesXray, LOGGER)
                }
            }

            if (minibosses.isNotEmpty()) {
                val minibossColor = HighlightUtil.clampedColor(
                    cfg.minibossHighlight.colorRed,
                    cfg.minibossHighlight.colorGreen,
                    cfg.minibossHighlight.colorBlue,
                    cfg.minibossHighlight.colorAlpha
                )
                val boxes = collectBoxes(minibosses, player, maxDistance, partialTicks)
                if (boxes.isNotEmpty()) {
                    HighlightUtil.drawBoxes(pose, boxes, minibossColor, renderMode, lineWidth, seeThroughWalls,
                        minibossFilled, minibossLines, minibossFilledXray, minibossLinesXray, LOGGER)
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
                val nameLower = entity.name.string.lowercase()
                if (CarryState.clients.containsKey(nameLower)) {
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
                    val playerNameLower = name.substring(idx + BOSS_TAG.length).trim().lowercase()
                    if (CarryState.clients.containsKey(playerNameLower)) {
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
        val mc = Minecraft.getInstance()
        val text = SRConfig.settings.carry.bossSpawnNotificationText.trim()
        mc.gui.setTimes(0, 20, 0)
        mc.gui.setTitle(Component.empty())
        mc.gui.setSubtitle(Component.literal(text).withColor(0xFF5555))
    }

    private fun findMinibosses(entities: Iterable<Entity>, clientPlayers: List<LivingEntity>, maxDistance: Int): List<LivingEntity> {
        if (clientPlayers.isEmpty()) return emptyList()
        val minibossArmorStands = entities.filterIsInstance<ArmorStand>().filter { stand ->
            val name = stand.name.string
            MINIBOSS_NAMES.any { name.contains(it, ignoreCase = true) }
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
