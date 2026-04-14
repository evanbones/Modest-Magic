package com.baisylia.modestmagic.integration.emi;

import com.baisylia.modestmagic.ModestMagic;
import com.baisylia.modestmagic.block.ModBlocks;
import com.baisylia.modestmagic.recipe.ModRecipes;
import com.baisylia.modestmagic.recipe.custom.EnchantingRecipe;
import com.baisylia.modestmagic.recipe.custom.InfusingRecipe;
import com.baisylia.modestmagic.recipe.custom.SummoningRecipe;
import com.baisylia.modestmagic.recipe.custom.TabletSmithingRecipe;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

@EmiEntrypoint
public class ModestMagicEmiPlugin implements EmiPlugin {

    public static final EmiRecipeCategory INFUSING = new EmiRecipeCategory(
            new ResourceLocation(ModestMagic.MOD_ID, "infusing"),
            EmiStack.of(ModBlocks.ALTAR.get())
    );

    public static final EmiRecipeCategory ENCHANTING = new EmiRecipeCategory(
            new ResourceLocation(ModestMagic.MOD_ID, "enchanting"),
            EmiStack.of(ModBlocks.ALTAR.get())
    );

    public static final EmiRecipeCategory SUMMONING = new EmiRecipeCategory(
            new ResourceLocation(ModestMagic.MOD_ID, "summoning"),
            EmiStack.of(ModBlocks.ALTAR.get())
    );

    /**
     * Helper to rotate points around a center (for circle layouts)
     */
    public static int getX(int cx, double angle, int radius) {
        return (int) (cx + Math.cos(Math.toRadians(angle)) * radius);
    }

    public static int getY(int cy, double angle, int radius) {
        return (int) (cy + Math.sin(Math.toRadians(angle)) * radius);
    }

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(INFUSING);
        registry.addCategory(ENCHANTING);
        registry.addCategory(SUMMONING);

        registry.addWorkstation(INFUSING, EmiStack.of(ModBlocks.ALTAR.get()));
        registry.addWorkstation(ENCHANTING, EmiStack.of(ModBlocks.ALTAR.get()));
        registry.addWorkstation(SUMMONING, EmiStack.of(ModBlocks.ALTAR.get()));

        for (InfusingRecipe recipe : registry.getRecipeManager().getAllRecipesFor(ModRecipes.INFUSING_TYPE.get())) {
            registry.addRecipe(new InfusingEmiRecipe(recipe));
        }

        for (EnchantingRecipe recipe : registry.getRecipeManager().getAllRecipesFor(ModRecipes.ENCHANTING_TYPE.get())) {
            registry.addRecipe(new EnchantingEmiRecipe(recipe));
        }

        for (SummoningRecipe recipe : registry.getRecipeManager().getAllRecipesFor(ModRecipes.SUMMONING_TYPE.get())) {
            registry.addRecipe(new SummoningEmiRecipe(recipe));
        }

        // fix for our custom EMI smithing recipes
        for (Recipe<?> recipe : registry.getRecipeManager().getAllRecipesFor(RecipeType.SMITHING)) {
            if (recipe instanceof TabletSmithingRecipe tabletRecipe) {
                registry.removeRecipes(tabletRecipe.getId());
                registry.addRecipe(new TabletSmithingEmiRecipe(tabletRecipe));
            }
        }
    }
}