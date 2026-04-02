package com.infrastructuresickos.dangerous_stone;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(DangerousStone.MOD_ID)
public class DangerousStone {
    public static final String MOD_ID = "dangerous_stone";

    public DangerousStone() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, DSConfig.SPEC);
        // Single manual registration on the Forge bus — StoneBreakHandler has no
        // @Mod.EventBusSubscriber annotation, preventing the double-registration bug.
        MinecraftForge.EVENT_BUS.register(new StoneBreakHandler());
    }
}
