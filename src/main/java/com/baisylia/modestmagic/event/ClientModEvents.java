package com.baisylia.modestmagic.event;

import com.baisylia.modestmagic.ModestMagic;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModestMagic.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onSmithingBackground(ContainerScreenEvent.Render.Background event) {
        if (event.getContainerScreen() instanceof SmithingScreen screen) {
            Slot additionSlot = screen.getMenu().getSlot(1);

            int x = screen.getGuiLeft() + additionSlot.x;
            int y = screen.getGuiTop() + additionSlot.y;

            boolean hasItem = additionSlot.hasItem();
            boolean showTablet = (System.currentTimeMillis() / 1500L) % 2 != 0;

            if (hasItem || showTablet) {
                PoseStack poseStack = event.getPoseStack();

                GuiComponent.fill(poseStack, x, y, x + 16, y + 16, 0xFF8B8B8B);

                if (!hasItem) {
                    RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
                    RenderSystem.enableBlend();

                    TextureAtlasSprite tabletSprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                            .apply(new ResourceLocation(ModestMagic.MOD_ID, "item/empty_slot_tablet"));

                    GuiComponent.blit(poseStack, x, y, 0, 16, 16, tabletSprite);
                }
            }
        }
    }
}