package com.sraddons.feature.starredmob.renderer

import com.sraddons.config.SRConfig
import com.sraddons.config.toColor
import com.sraddons.render.HighlightUtil
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.phys.AABB
import org.apache.logging.log4j.LogManager

object StarredMobRenderer {

    private val LOGGER = LogManager.getLogger("SR-Addons-StarredMob")
    private const val STAR_SYMBOL = "\u272f"

    private val filledXrayType: RenderType by lazy { HighlightUtil.createFilledType("starredmob", true) }
    private val linesXrayType: RenderType by lazy { HighlightUtil.createLinesType("starredmob", true) }
    private val filledType: RenderType by lazy { HighlightUtil.createFilledType("starredmob", false) }
    private val linesType: RenderType by lazy { HighlightUtil.createLinesType("starredmob", false) }

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

            val color = SRConfig.settings.starredMob.toColor()
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

            val boxes = HighlightUtil.collectBoxes(starredMobs, player, maxDistance, partialTicks, LOGGER)

            if (boxes.isNotEmpty()) {
                HighlightUtil.drawBoxes(pose, boxes, color, renderMode, lineWidth, seeThroughWalls,
                    filledType, linesType, filledXrayType, linesXrayType, LOGGER)
            }

            poseStack.popPose()
        }
    }

    private val nonDigitRegex = Regex("[^0-9]")

    private fun isDamageNumber(name: String): Boolean {
        val cleaned = name.replace(STAR_SYMBOL, "").replace(nonDigitRegex, "")
        return cleaned.isNotEmpty()
    }

    private fun findStarredMobs(entities: Iterable<Entity>): List<LivingEntity> {
        val result = LinkedHashSet<LivingEntity>()
        val starredArmorStands = mutableListOf<ArmorStand>()
        val mobs = HighlightUtil.filterLivingMobs(entities)

        for (entity in entities) {
            if (entity is ArmorStand) {
                val name = entity.name.string
                if (name.contains(STAR_SYMBOL) && !isDamageNumber(name)) {
                    starredArmorStands.add(entity)
                }
            } else if (entity is LivingEntity) {
                val name = entity.customName?.string ?: entity.name.string
                if (name.contains(STAR_SYMBOL)) {
                    result.add(entity)
                }
            }
        }

        for (armorStand in starredArmorStands) {
            val target = HighlightUtil.findNearestMobBelow(armorStand, mobs)
            if (target != null) {
                result.add(target)
            }
        }

        return result.toList()
    }
}
