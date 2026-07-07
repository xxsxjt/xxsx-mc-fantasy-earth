package com.xxsx.earthonlinemagic;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Mod(EarthOnlineMagic.MODID)
public class EarthOnlineMagic {
    public static final String MODID = "earth_online_magic";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    private static final List<ItemLike> TAB_ITEMS = new ArrayList<>();

    public static final DeferredBlock<Block> ALCHEMY_TABLE = simpleBlock("alchemy_table", MapColor.WOOD, SoundType.WOOD, 2.5F, "tooltip.earth_online_magic.alchemy_table");
    public static final DeferredBlock<Block> RUNE_CARVING_TABLE = simpleBlock("rune_carving_table", MapColor.STONE, SoundType.STONE, 3.0F, "tooltip.earth_online_magic.rune_carving_table");
    public static final DeferredBlock<Block> RITUAL_PEDESTAL = simpleBlock("ritual_pedestal", MapColor.COLOR_PURPLE, SoundType.STONE, 4.0F, "tooltip.earth_online_magic.ritual_pedestal");
    public static final DeferredBlock<Block> AETHER_CRYSTAL_CLUSTER = simpleBlock("aether_crystal_cluster", MapColor.COLOR_LIGHT_BLUE, SoundType.AMETHYST, 2.0F, "tooltip.earth_online_magic.aether_crystal_cluster");

    public static final DeferredItem<ArcaneNotebookItem> FIELD_ARCANE_NOTEBOOK = ITEMS.registerItem("field_arcane_notebook", ArcaneNotebookItem::new, props -> props.stacksTo(1));
    public static final DeferredItem<ArcaneInitiationNotesItem> ARCANE_INITIATION_NOTES = ITEMS.registerItem("arcane_initiation_notes", ArcaneInitiationNotesItem::new, props -> props.stacksTo(1));
    public static final DeferredItem<ArcaneAdaptationNotesItem> ARCANE_BODY_WARD_NOTES = ITEMS.registerItem("arcane_body_ward_notes",
            props -> new ArcaneAdaptationNotesItem(props.stacksTo(1), ArcaneAdaptationNotesItem.Type.BODY_WARD),
            props -> props);
    public static final DeferredItem<ArcaneAdaptationNotesItem> ARCANE_BREATH_WARD_NOTES = ITEMS.registerItem("arcane_breath_ward_notes",
            props -> new ArcaneAdaptationNotesItem(props.stacksTo(1), ArcaneAdaptationNotesItem.Type.BREATH_WARD),
            props -> props);
    public static final DeferredItem<Item> ARCANE_DUST = materialItem("arcane_dust", "tooltip.earth_online_magic.arcane_dust");
    public static final DeferredItem<Item> RUNE_INK = materialItem("rune_ink", "tooltip.earth_online_magic.rune_ink");
    public static final DeferredItem<Item> RITUAL_CHALK = materialItem("ritual_chalk", "tooltip.earth_online_magic.ritual_chalk");
    public static final DeferredItem<Item> CRYSTALLIZED_MANA_SALT = materialItem("crystallized_mana_salt", "tooltip.earth_online_magic.crystallized_mana_salt");
    public static final DeferredItem<Item> AETHER_GLASS = materialItem("aether_glass", "tooltip.earth_online_magic.aether_glass");
    public static final DeferredItem<Item> RUNE_COPPER_PLATE = materialItem("rune_copper_plate", "tooltip.earth_online_magic.rune_copper_plate");
    public static final DeferredItem<Item> AETHER_CRYSTAL = materialItem("aether_crystal", "tooltip.earth_online_magic.aether_crystal");
    public static final DeferredItem<Item> DORMANT_RITUAL_CORE = materialItem("dormant_ritual_core", "tooltip.earth_online_magic.dormant_ritual_core");

    public EarthOnlineMagic(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        TAB_ITEMS.add(FIELD_ARCANE_NOTEBOOK);
        TAB_ITEMS.add(ARCANE_INITIATION_NOTES);
        TAB_ITEMS.add(ARCANE_BODY_WARD_NOTES);
        TAB_ITEMS.add(ARCANE_BREATH_WARD_NOTES);
        modBus.addListener(this::registerCreativeTab);
        LOGGER.info("[Earth Online: Magic] NeoForge 26.2 module loaded");
    }

    private static DeferredBlock<Block> simpleBlock(String id, MapColor color, SoundType sound, float strength, String hintKey) {
        DeferredBlock<Block> block = BLOCKS.registerBlock(id, MagicFieldBlock::new, () -> BlockBehaviour.Properties.of()
                .mapColor(color)
                .strength(strength, strength * 2.0F)
                .requiresCorrectToolForDrops()
                .sound(sound));
        DeferredItem<?> item = ITEMS.registerItem(id,
                props -> new MagicBlockItem(block.get(), props, hintKey),
                props -> props);
        TAB_ITEMS.add(item);
        return block;
    }

    private static DeferredItem<Item> materialItem(String id, String hintKey) {
        DeferredItem<Item> item = ITEMS.registerItem(id, props -> new MagicMaterialItem(props, hintKey), props -> props);
        TAB_ITEMS.add(item);
        return item;
    }

    private void registerCreativeTab(RegisterEvent event) {
        event.register(Registries.CREATIVE_MODE_TAB, helper -> helper.register(id("main"),
                CreativeModeTab.builder()
                        .title(Component.translatable("itemGroup.earth_online_magic"))
                        .icon(() -> new ItemStack(FIELD_ARCANE_NOTEBOOK.get()))
                        .displayItems((params, output) -> TAB_ITEMS.forEach(output::accept))
                        .build()));
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
