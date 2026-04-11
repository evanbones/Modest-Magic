package com.baisylia.modestmagic.integration.emi;

import com.baisylia.modestmagic.recipe.custom.InfusingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class InfusingEmiRecipe implements EmiRecipe {

    private final ResourceLocation id;
    private final EmiIngredient base;
    private final List<EmiIngredient> inputs;
    private final EmiStack output;

    public InfusingEmiRecipe(InfusingRecipe recipe) {
        this.id = recipe.getId();
        this.base = EmiIngredient.of(recipe.getBase());
        this.output = EmiStack.of(recipe.getResult());

        this.inputs = new ArrayList<>();
        this.inputs.add(base);
        recipe.getIngredients().forEach(ing -> this.inputs.add(EmiIngredient.of(ing)));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return ModestMagicEmiPlugin.INFUSING;
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
        return List.of(output);
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

        // Base item in center
        widgets.addSlot(base, cx - 9, cy - 9);

        // Pedestal items in a circle
        int count = inputs.size() - 1;
        for (int i = 0; i < count; i++) {
            double angle = (360.0 / count) * i - 90.0;
            int x = ModestMagicEmiPlugin.getX(cx, angle, radius);
            int y = ModestMagicEmiPlugin.getY(cy, angle, radius);
            widgets.addSlot(inputs.get(i + 1), x - 9, y - 9);
        }

        // Arrow and output
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 75, cy - 8);
        widgets.addSlot(output, 110, cy - 9).recipeContext(this);
    }
}