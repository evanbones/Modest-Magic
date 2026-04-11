package com.baisylia.modestmagic.integration.emi;

import com.baisylia.modestmagic.recipe.custom.SummoningRecipe;
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

import java.util.ArrayList;
import java.util.List;

public class SummoningEmiRecipe implements EmiRecipe {

    private final ResourceLocation id;
    private final EmiIngredient base;
    private final List<EmiIngredient> inputs;
    private Entity cachedEntity;

    public SummoningEmiRecipe(SummoningRecipe recipe) {
        this.id = recipe.getId();
        this.base = EmiIngredient.of(recipe.getBase());

        this.inputs = new ArrayList<>();
        this.inputs.add(base);
        recipe.getIngredients().forEach(ing -> this.inputs.add(EmiIngredient.of(ing)));

        if (Minecraft.getInstance().level != null) {
            this.cachedEntity = recipe.getResultEntity().create(Minecraft.getInstance().level);
            if (this.cachedEntity != null && !recipe.getEntityNbt().isEmpty()) {
                this.cachedEntity.load(recipe.getEntityNbt());
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
        return 80;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int cx = 35;
        int cy = 40;
        int radius = 24;

        widgets.addSlot(base, cx - 9, cy - 9);

        // Pedestal items in a circle
        int count = inputs.size() - 1;
        for (int i = 0; i < count; i++) {
            double angle = (360.0 / count) * i - 90.0;
            int x = ModestMagicEmiPlugin.getX(cx, angle, radius);
            int y = ModestMagicEmiPlugin.getY(cy, angle, radius);
            widgets.addSlot(inputs.get(i + 1), x - 9, y - 9);
        }

        // Arrow
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 75, cy - 8);

        // Entity in output
        widgets.addDrawable(105, 10, 30, 50, (poseStack, mouseX, mouseY, delta) -> {
            if (cachedEntity instanceof LivingEntity living) {
                float scale = 15.0f; // TODO: adjust to fit inside the UI

                int entityX = 120;
                int entityY = 60;

                InventoryScreen.renderEntityInInventory(
                        entityX, entityY, (int) scale,
                        entityX - mouseX, entityY - mouseY - 20,
                        living
                );
            }
        });
    }
}