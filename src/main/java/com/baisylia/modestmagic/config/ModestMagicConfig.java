package com.baisylia.modestmagic.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModestMagicConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue THROW_ITEMS_ON_PEDESTALS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("general");
        THROW_ITEMS_ON_PEDESTALS = builder
                .comment("If true, dropped items will automatically be picked up by empty pedestals.")
                .define("throwItemsOnPedestals", true);
        builder.pop();

        SPEC = builder.build();
    }
}