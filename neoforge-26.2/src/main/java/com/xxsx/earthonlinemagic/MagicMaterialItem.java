package com.xxsx.earthonlinemagic;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class MagicMaterialItem extends Item {
    private final String hintKey;

    public MagicMaterialItem(Properties properties, String hintKey) {
        super(properties);
        this.hintKey = hintKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        lines.accept(Component.translatable(hintKey).withStyle(ChatFormatting.LIGHT_PURPLE));
        lines.accept(Component.translatable("tooltip.earth_online_magic.material.route").withStyle(ChatFormatting.GRAY));
    }
}
