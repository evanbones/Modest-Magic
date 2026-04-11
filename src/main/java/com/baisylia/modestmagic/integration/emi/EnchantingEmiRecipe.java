package com.baisylia.modestmagic.integration.emi;

import com.baisylia.modestmagic.recipe.custom.EnchantingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EnchantingEmiRecipe implements EmiRecipe {

    private final ResourceLocation id;
    private final List<EmiIngredient> inputs;
    private final EmiStack output;

    public EnchantingEmiRecipe(EnchantingRecipe recipe) {
        this.id = recipe.getId();
        this.inputs = recipe.getIngredients().stream().map(EmiIngredient::of).collect(Collectors.toList());

        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        Map<Enchantment, Integer> appliedEnchantments = new HashMap<>();
        for (Enchantment e : recipe.getEnchantments()) {
            appliedEnchantments.put(e, 1);
        }
        EnchantmentHelper.setEnchantments(appliedEnchantments, book);
        this.output = EmiStack.of(book);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return ModestMagicEmiPlugin.ENCHANTING;
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

        // Empty generic slot in the center with a tooltip since EnchantingRecipe doesn't mandate a base item
        widgets.addSlot(EmiStack.EMPTY, cx - 9, cy - 9)
                .appendTooltip(Component.translatable("tooltip.modestmagic.any_enchantable_item"));

        // Pedestal items
        int count = inputs.size();
        for (int i = 0; i < count; i++) {
            double angle = (360.0 / count) * i - 90.0;
            int x = ModestMagicEmiPlugin.getX(cx, angle, radius);
            int y = ModestMagicEmiPlugin.getY(cy, angle, radius);
            widgets.addSlot(inputs.get(i), x - 9, y - 9);
        }

        // Arrow and enchanted book output
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 75, cy - 8);
        widgets.addSlot(output, 110, cy - 9).recipeContext(this);
    }
}