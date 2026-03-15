package com.baisylia.modestmagic.event;

import com.baisylia.modestmagic.ModestMagic;
import com.baisylia.modestmagic.block.entity.ModBlockEntities;
import com.baisylia.modestmagic.block.renderer.EnchantingRenderer;
import com.baisylia.modestmagic.block.renderer.PedestalRenderer;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModestMagic.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {

        event.registerBlockEntityRenderer(
                ModBlockEntities.PEDESTAL_BLOCK_ENTITY.get(),
                PedestalRenderer::new
        );

        event.registerBlockEntityRenderer(
                ModBlockEntities.ENCHANTING_BLOCK_ENTITY.get(),
                EnchantingRenderer::new
        );
    }
}
