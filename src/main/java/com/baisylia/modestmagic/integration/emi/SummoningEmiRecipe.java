package com.baisylia.modestmagic.integration.emi;

import com.baisylia.modestmagic.block.ModBlocks;
import com.baisylia.modestmagic.recipe.custom.SummoningRecipe;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SummoningEmiRecipe implements EmiRecipe {

    private final ResourceLocation id;
    private final EmiIngredient base;
    private final List<EmiIngredient> inputs;
    private final List<Entity> cachedEntities;

    public SummoningEmiRecipe(SummoningRecipe recipe) {
        this.id = recipe.getId();
        this.base = EmiIngredient.of(recipe.getBase());

        this.inputs = new ArrayList<>();
        this.inputs.add(base);
        recipe.getIngredients().forEach(ing -> this.inputs.add(EmiIngredient.of(ing)));

        this.cachedEntities = new ArrayList<>();
        if (Minecraft.getInstance().level != null) {
            for (SummoningRecipe.SummonOutcome outcome : recipe.getOutcomes()) {
                Entity entity = outcome.entity().create(Minecraft.getInstance().level);
                if (entity != null && !outcome.nbt().isEmpty()) entity.load(outcome.nbt());
                if (entity != null) cachedEntities.add(entity);
            }
        }
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return ModestMagicEmiPlugin.SUMMONING;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of();
    }

    @Override
    public int getDisplayWidth() {
        return 140;
    }

    @Override
    public int getDisplayHeight() {
        return inputs.size() > 6 ? 100 : 80;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int cx = 35;
        int cy = getDisplayHeight() / 2;
        int radius = inputs.size() > 6 ? 32 : 24;

        widgets.addSlot(base, cx - 9, cy - 9);

        // Rotating Pedestal items
        List<EmiIngredient> pedestalItems = inputs.subList(1, inputs.size());
        RotationState state = new RotationState(cx, cy, radius, pedestalItems.size());

        for (int i = 0; i < pedestalItems.size(); i++) {
            widgets.add(new RotatingSlotWidget(state, pedestalItems.get(i), i));
        }

        // Pedestal Count slot
        widgets.addSlot(EmiStack.of(new ItemStack(ModBlocks.PEDESTAL.get(), pedestalItems.size())), getDisplayWidth() - 18, getDisplayHeight() - 18).drawBack(true);

        // Arrow
        widgets.addTexture(EmiTexture.EMPTY_ARROW, cx + radius + 16, cy - 8);

        int slotX = cx + radius - 4;
        int slotY = cy - 24;

        widgets.addDrawable(slotX, slotY, 18, 18, (poseStack, mouseX, mouseY, delta) -> {
            if (!cachedEntities.isEmpty()) {
                int index = (int) ((System.currentTimeMillis() / 1500L) % cachedEntities.size());
                Entity currentEntity = cachedEntities.get(index);

                if (currentEntity instanceof LivingEntity living) {
                    double width = living.getBbWidth();
                    double height = living.getBbHeight();
                    double len = (width + width + height) / 3.0;

                    if (len > 1.05) len = (len + Math.sqrt(len)) / 2.0;
                    float scale = (float) (1.05 / len * 14.0);

                    int entityX = slotX + 9;
                    int entityY = slotY + 17;

                    PoseStack modelViewStack = RenderSystem.getModelViewStack();
                    modelViewStack.pushPose();
                    modelViewStack.mulPoseMatrix(poseStack.last().pose());
                    RenderSystem.applyModelViewMatrix();

                    InventoryScreen.renderEntityInInventory(
                            entityX, entityY, (int) scale,
                            entityX - mouseX, entityY - mouseY - (scale * 1.5f),
                            living
                    );

                    modelViewStack.popPose();
                    RenderSystem.applyModelViewMatrix();
                }
            }
        });
    }
}