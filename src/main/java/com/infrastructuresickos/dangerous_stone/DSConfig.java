package com.infrastructuresickos.dangerous_stone;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class DSConfig {
    public static final ForgeConfigSpec SPEC;
    public static final DSConfig INSTANCE;

    static {
        Pair<DSConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(DSConfig::new);
        INSTANCE = specPair.getLeft();
        SPEC = specPair.getRight();
    }

    public final ForgeConfigSpec.DoubleValue woodCrackChance;
    public final ForgeConfigSpec.DoubleValue stoneCrackChance;
    public final ForgeConfigSpec.DoubleValue ironCrackChance;
    public final ForgeConfigSpec.DoubleValue diamondCrackChance;
    public final ForgeConfigSpec.IntValue maxBlocksPerBreak;
    public final ForgeConfigSpec.IntValue maxDistance;
    public final ForgeConfigSpec.BooleanValue requireAirExposure;

    private DSConfig(ForgeConfigSpec.Builder builder) {
        builder.push("crack_chances");
        woodCrackChance     = builder.comment("Crack propagation chance for wood-tier tools (0.0–1.0)")
                                     .defineInRange("wood",     0.50, 0.0, 1.0);
        stoneCrackChance    = builder.comment("Crack propagation chance for stone-tier tools (0.0–1.0)")
                                     .defineInRange("stone",    0.35, 0.0, 1.0);
        ironCrackChance     = builder.comment("Crack propagation chance for iron-tier tools (0.0–1.0)")
                                     .defineInRange("iron",     0.10, 0.0, 1.0);
        diamondCrackChance  = builder.comment("Crack propagation chance for diamond/netherite-tier tools (0.0–1.0)")
                                     .defineInRange("diamond",  0.00, 0.0, 1.0);
        builder.pop();

        maxBlocksPerBreak = builder.comment("Maximum number of blocks converted per mining event")
                                   .defineInRange("maxBlocksPerBreak", 24, 1, 256);
        maxDistance       = builder.comment("Maximum Manhattan distance for BFS propagation")
                                   .defineInRange("maxDistance", 6, 1, 32);
        requireAirExposure = builder.comment("If true, only blocks with at least one air-adjacent face can crack")
                                    .define("requireAirExposure", false);
    }
}
