package com.infrastructuresickos.dangerous_stone;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Random;

/**
 * Handles the block-break event.
 * Registered manually on the FORGE bus — do NOT add @Mod.EventBusSubscriber.
 */
public class StoneBreakHandler {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof Level level)) return;
        if (level.isClientSide()) return;
        if (!(event.getPlayer() instanceof Player player)) return;

        CrackPropagator.propagate(level, event.getPos(), player, RANDOM);
    }
}
