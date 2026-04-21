package com.baisylia.modestmagic.integration.emi;

import com.baisylia.modestmagic.block.ModBlocks;
import com.baisylia.modestmagic.recipe.custom.InfusingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class InfusingEmiRecipe implements EmiRecipe {

    private static final ResourceLocation BACKGROUND = new ResourceLocation("modestmagic", "textures/gui/emi_background.png");
    private final ResourceLocation id;
    private final EmiIngredient base;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;

    public InfusingEmiRecipe(InfusingRecipe recipe) {
        this.id = recipe.getId();
        this.base = EmiIngredient.of(recipe.getBase());
        this.outputs = recipe.getResults().stream().map(EmiStack::of).toList();

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
        return outputs;
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
        widgets.addTexture(BACKGROUND, 0, 0, getDisplayWidth(), getDisplayHeight(), 0, 0);

        int cx = 35;
        int cy = getDisplayHeight() / 2;
        int radius = 24;

        List<EmiIngredient> pedestalItems = inputs.subList(1, inputs.size());
        List<EmiIngredient> circleItems = pedestalItems.size() > 6 ? pedestalItems.subList(0, 6) : pedestalItems;
        List<EmiIngredient> extraItems = pedestalItems.size() > 6 ? pedestalItems.subList(6, pedestalItems.size()) : List.of();

        RotationState state = new RotationState(cx, cy, radius, circleItems.size());

        widgets.add(new RotatingLettersWidget(
                new ResourceLocation("modestmagic", "textures/gui/enchanted_letters.png"),
                cx, cy, radius + 6
        ));

        widgets.add(new HoveringSlotWidget(base, cx - 9, cy - 9, 0));

        for (int i = 0; i < circleItems.size(); i++) {
            widgets.add(new RotatingSlotWidget(state, circleItems.get(i), i + 1));
        }

        // Pedestal Count slot
        widgets.addSlot(EmiStack.of(new ItemStack(ModBlocks.PEDESTAL.get(), pedestalItems.size())), getDisplayWidth() - 18, getDisplayHeight() - 18).drawBack(true);

        // Extra Ingredients cycling slot
        if (!extraItems.isEmpty()) {
            List<EmiIngredient> consolidated = new ArrayList<>();

            for (EmiIngredient ing : extraItems) {
                boolean found = false;
                EmiStack firstStack = ing.getEmiStacks().isEmpty() ? null : ing.getEmiStacks().get(0);

                for (EmiIngredient existing : consolidated) {
                    EmiStack existingFirst = existing.getEmiStacks().isEmpty() ? null : existing.getEmiStacks().get(0);

                    if (firstStack != null && existingFirst != null && firstStack.isEqual(existingFirst)) {
                        for (EmiStack stack : existing.getEmiStacks()) {
                            stack.setAmount(stack.getAmount() + 1);
                        }
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    List<EmiStack> copies = new ArrayList<>();
                    for (EmiStack stack : ing.getEmiStacks()) {
                        EmiStack copy = stack.copy();
                        copy.setAmount(1);
                        copies.add(copy);
                    }
                    consolidated.add(EmiIngredient.of(copies));
                }
            }

            int startX = 65;
            int startY = 58;
            for (int i = 0; i < consolidated.size(); i++) {
                int xOffset = (i % 3) * 18;
                int yOffset = (i / 3) * 18;
                widgets.addSlot(consolidated.get(i), startX + xOffset, startY + yOffset).drawBack(true);
            }
        }

        // Arrow and cycling output
        widgets.addTexture(EmiTexture.EMPTY_ARROW, cx + radius + 16, cy - 8);
        widgets.add(new HoveringSlotWidget(EmiIngredient.of(outputs), cx + radius + 51, cy - 9, 2)).recipeContext(this);
    }
}