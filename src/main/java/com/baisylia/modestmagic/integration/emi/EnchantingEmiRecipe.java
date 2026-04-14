package com.baisylia.modestmagic.integration.emi;

import com.baisylia.modestmagic.block.ModBlocks;
import com.baisylia.modestmagic.recipe.custom.EnchantingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EnchantingEmiRecipe implements EmiRecipe {

    private final ResourceLocation id;
    private final List<EmiIngredient> inputs;
    private final EmiIngredient baseIngredient;
    private final List<EmiStack> outputs;

    public EnchantingEmiRecipe(EnchantingRecipe recipe) {
        this.id = recipe.getId();
        this.inputs = recipe.getIngredients().stream().map(EmiIngredient::of).collect(Collectors.toList());

        List<EmiStack> validStacks = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            ItemStack testStack = new ItemStack(item);
            if (testStack.isEnchantable()) {
                boolean isValid = false;

                for (List<Enchantment> pool : recipe.getEnchantmentPools()) {
                    boolean poolValid = true;
                    for (Enchantment e : pool) {
                        if (!e.canEnchant(testStack)) {
                            poolValid = false;
                            break;
                        }
                    }
                    if (poolValid) {
                        isValid = true;
                        break;
                    }
                }

                if (isValid) {
                    validStacks.add(EmiStack.of(testStack));
                }
            }
        }

        if (validStacks.isEmpty()) {
            validStacks.add(EmiStack.of(Items.BOOK));
        }

        this.baseIngredient = EmiIngredient.of(validStacks);

        this.outputs = new ArrayList<>();
        for (List<Enchantment> pool : recipe.getEnchantmentPools()) {
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            Map<Enchantment, Integer> appliedEnchantments = new HashMap<>();
            for (Enchantment e : pool) appliedEnchantments.put(e, 1);
            EnchantmentHelper.setEnchantments(appliedEnchantments, book);
            this.outputs.add(EmiStack.of(book));
        }
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
        List<EmiIngredient> allInputs = new ArrayList<>();
        allInputs.add(baseIngredient);
        allInputs.addAll(inputs);
        return allInputs;
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
        return inputs.size() > 6 ? 100 : 80;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int cx = 35;
        int cy = getDisplayHeight() / 2;
        int radius = inputs.size() > 6 ? 32 : 24;

        // Base item cycle slot
        widgets.addSlot(baseIngredient, cx - 9, cy - 9);

        // Rotating Pedestal items
        RotationState state = new RotationState(cx, cy, radius, inputs.size());

        for (int i = 0; i < inputs.size(); i++) {
            widgets.add(new RotatingSlotWidget(state, inputs.get(i), i));
        }

        // Pedestal Count slot
        widgets.addSlot(EmiStack.of(new ItemStack(ModBlocks.PEDESTAL.get(), inputs.size())), getDisplayWidth() - 18, getDisplayHeight() - 18).drawBack(true);

        // Arrow and cycling enchanted book
        widgets.addTexture(EmiTexture.EMPTY_ARROW, cx + radius + 16, cy - 8);
        widgets.addSlot(EmiIngredient.of(outputs), cx + radius + 51, cy - 9).recipeContext(this);
    }
}