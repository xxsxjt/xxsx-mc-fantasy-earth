package com.xxsx.fantasyearth.integration.jei;

import com.xxsx.fantasyearth.FantasyEarth;
import com.xxsx.fantasyearth.MagicMachineBlock;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

@JeiPlugin
public class MagicJeiPlugin implements IModPlugin {
    private static final Map<MagicMachineBlock.Kind, IRecipeType<MagicMachineBlock.Recipe>> TYPES = createTypes();

    @Override
    public Identifier getPluginUid() {
        return FantasyEarth.id("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        for (MagicMachineBlock.Kind kind : MagicMachineBlock.Kind.values()) {
            registration.addRecipeCategories(new MagicMachineJeiCategory(guiHelper, kind, recipeTypeFor(kind), machineFor(kind)));
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        for (MagicMachineBlock.Kind kind : MagicMachineBlock.Kind.values()) {
            registration.addRecipes(recipeTypeFor(kind), MagicMachineBlock.recipesFor(kind));
        }
        registration.addItemStackInfo(new ItemStack(FantasyEarth.FIELD_ARCANE_NOTEBOOK.get()),
                line("jei.fantasy_earth.handbook.0", ChatFormatting.LIGHT_PURPLE),
                line("jei.fantasy_earth.handbook.1", ChatFormatting.WHITE),
                line("jei.fantasy_earth.handbook.2", ChatFormatting.AQUA),
                line("jei.fantasy_earth.handbook.3", ChatFormatting.YELLOW));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (MagicMachineBlock.Kind kind : MagicMachineBlock.Kind.values()) {
            registration.addCraftingStation(recipeTypeFor(kind), machineFor(kind));
        }
    }

    public static IRecipeType<MagicMachineBlock.Recipe> recipeTypeFor(MagicMachineBlock.Kind kind) {
        return TYPES.get(kind);
    }

    private static Map<MagicMachineBlock.Kind, IRecipeType<MagicMachineBlock.Recipe>> createTypes() {
        EnumMap<MagicMachineBlock.Kind, IRecipeType<MagicMachineBlock.Recipe>> types = new EnumMap<>(MagicMachineBlock.Kind.class);
        for (MagicMachineBlock.Kind kind : MagicMachineBlock.Kind.values()) {
            types.put(kind, IRecipeType.create(FantasyEarth.MODID, "processing_" + kind.blockId(), MagicMachineBlock.Recipe.class));
        }
        return Collections.unmodifiableMap(types);
    }

    private static ItemLike machineFor(MagicMachineBlock.Kind kind) {
        return switch (kind) {
            case ALCHEMY_TABLE -> FantasyEarth.ALCHEMY_TABLE.get();
            case RUNE_CARVING_TABLE -> FantasyEarth.RUNE_CARVING_TABLE.get();
            case RITUAL_PEDESTAL -> FantasyEarth.RITUAL_PEDESTAL.get();
        };
    }

    private static Component line(String key, ChatFormatting color) {
        return Component.translatable(key).withStyle(color);
    }
}
