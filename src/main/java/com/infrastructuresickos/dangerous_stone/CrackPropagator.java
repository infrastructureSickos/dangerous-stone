package com.infrastructuresickos.dangerous_stone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public class CrackPropagator {

    /** Transformation chains: block → what it becomes when cracked */
    private static final Map<Block, Block> TRANSFORMS = Map.of(
        Blocks.STONE,              Blocks.COBBLESTONE,
        Blocks.COBBLESTONE,        Blocks.GRAVEL,
        Blocks.TUFF,               Blocks.GRAVEL,
        Blocks.STONE_BRICKS,       Blocks.CRACKED_STONE_BRICKS,
        Blocks.MOSSY_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS,
        Blocks.CHISELED_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS,
        Blocks.BLUE_ICE,           Blocks.PACKED_ICE,
        Blocks.PACKED_ICE,         Blocks.ICE,
        Blocks.ICE,                Blocks.WATER
    );

    /** Returns true if the block tag indicates a wood/log structural support. */
    private static boolean isSupport(BlockState state) {
        return state.is(net.minecraft.tags.BlockTags.LOGS);
    }

    /** Returns the crack chance for the tool in the player's main hand. */
    public static double getCrackChance(Player player) {
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof TieredItem tiered) {
            Tier tier = tiered.getTier();
            if (tier == Tiers.WOOD || tier == Tiers.GOLD) {
                return DSConfig.INSTANCE.woodCrackChance.get();
            } else if (tier == Tiers.STONE) {
                return DSConfig.INSTANCE.stoneCrackChance.get();
            } else if (tier == Tiers.IRON) {
                return DSConfig.INSTANCE.ironCrackChance.get();
            } else {
                // Diamond, Netherite, or better
                return DSConfig.INSTANCE.diamondCrackChance.get();
            }
        }
        // Bare hand counts as wood tier
        return DSConfig.INSTANCE.woodCrackChance.get();
    }

    /**
     * Runs BFS crack propagation from the broken block's position.
     * Wood/log blocks within maxDistance of the player suppress cracking.
     */
    public static void propagate(Level level, BlockPos origin, Player player, Random random) {
        double crackChance = getCrackChance(player);
        if (crackChance <= 0.0) return;

        int maxBlocks   = DSConfig.INSTANCE.maxBlocksPerBreak.get();
        int maxDist     = DSConfig.INSTANCE.maxDistance.get();
        boolean airOnly = DSConfig.INSTANCE.requireAirExposure.get();

        // Pre-scan for support (wood/log) blocks within maxDist of the player.
        // If any log is within range, the entire volume around the player is protected.
        boolean supportPresent = hasSupportNearby(level, player.blockPosition(), maxDist);

        Queue<BlockPos> frontier = new ArrayDeque<>();
        Set<BlockPos> visited   = new HashSet<>();
        frontier.add(origin);
        visited.add(origin);

        int converted = 0;

        while (!frontier.isEmpty() && converted < maxBlocks) {
            BlockPos pos = frontier.poll();
            int manhattan = Math.abs(pos.getX() - origin.getX())
                          + Math.abs(pos.getY() - origin.getY())
                          + Math.abs(pos.getZ() - origin.getZ());
            if (manhattan > maxDist) continue;

            BlockState state = level.getBlockState(pos);
            Block nextBlock  = TRANSFORMS.get(state.getBlock());

            if (nextBlock != null) {
                // Skip if protected by a nearby support
                if (supportPresent) continue;

                // Skip if air-exposure check is required and no adjacent air face exists
                if (airOnly && !hasAdjacentAir(level, pos)) continue;

                if (random.nextDouble() < crackChance) {
                    level.setBlock(pos, nextBlock.defaultBlockState(), Block.UPDATE_ALL);
                    converted++;
                }
            }

            // Expand to neighbors
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.relative(dir);
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    BlockState ns = level.getBlockState(neighbor);
                    if (TRANSFORMS.containsKey(ns.getBlock())) {
                        frontier.add(neighbor);
                    }
                }
            }
        }
    }

    private static boolean hasSupportNearby(Level level, BlockPos playerPos, int maxDist) {
        for (BlockPos pos : BlockPos.betweenClosed(
                playerPos.offset(-maxDist, -maxDist, -maxDist),
                playerPos.offset(maxDist,  maxDist,  maxDist))) {
            if (isSupport(level.getBlockState(pos))) return true;
        }
        return false;
    }

    private static boolean hasAdjacentAir(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (level.getBlockState(pos.relative(dir)).isAir()) return true;
        }
        return false;
    }
}
