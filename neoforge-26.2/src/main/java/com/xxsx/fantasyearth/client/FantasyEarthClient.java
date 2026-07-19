package com.xxsx.fantasyearth.client;

import com.xxsx.fantasyearth.client.model.AetherFoxModel;
import com.xxsx.fantasyearth.client.model.ArcaneSettlerModel;
import com.xxsx.fantasyearth.client.model.CrystalSpiderModel;
import com.xxsx.fantasyearth.client.model.ManaWispModel;
import com.xxsx.fantasyearth.client.model.RuneWolfModel;
import com.xxsx.fantasyearth.client.model.RunicWatcherModel;
import com.xxsx.fantasyearth.client.renderer.ArcaneSettlerRenderer;
import com.xxsx.fantasyearth.client.renderer.CrystalSpiderRenderer;
import com.xxsx.fantasyearth.client.renderer.FamiliarRenderer;
import com.xxsx.fantasyearth.client.renderer.RunicWatcherRenderer;
import com.xxsx.fantasyearth.FantasyEarth;
import com.xxsx.fantasyearth.ArcaneActionPayload;
import com.xxsx.fantasyearth.ArcaneFocus;
import com.xxsx.fantasyearth.ArcaneStatusPayload;
import com.xxsx.fantasyearth.ArcaneVisualPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.lwjgl.glfw.GLFW;

public final class FantasyEarthClient {
    private static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(FantasyEarth.id("controls"));
    private static final KeyMapping OPEN_ATTUNEMENT = new KeyMapping(
            "key.fantasy_earth.open_attunement",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            CATEGORY);
    private static final KeyMapping ACTIVATE_ARCANE_SKILL = new KeyMapping(
            "key.fantasy_earth.activate_arcane_skill",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            CATEGORY);
    private static ArcaneStatusPayload arcaneStatus = ArcaneStatusPayload.empty();
    private FantasyEarthClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(FantasyEarthClient::registerScreens);
        modBus.addListener(FantasyEarthClient::registerLayerDefinitions);
        modBus.addListener(FantasyEarthClient::registerEntityRenderers);
        modBus.addListener(FantasyEarthClient::registerGuiLayers);
        modBus.addListener(FantasyEarthClient::registerPayloadHandlers);
        modBus.addListener(FantasyEarthClient::registerKeyMappings);
        NeoForge.EVENT_BUS.addListener(FantasyEarthClient::clientTick);
        NeoForge.EVENT_BUS.addListener(ArcanePlayerAnimations::renderPlayer);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(FantasyEarth.MAGIC_MACHINE_MENU.get(), MagicMachineScreen::new);
    }

    private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(FantasyEarth.ARCANE_SEAT.get(), InvisibleSeatRenderer::new);
        event.registerEntityRenderer(FantasyEarth.RUNIC_WATCHER.get(), RunicWatcherRenderer::new);
        event.registerEntityRenderer(FantasyEarth.AETHER_FOX.get(), context ->
                new FamiliarRenderer<>(context, AetherFoxModel.LAYER_LOCATION, AetherFoxModel::new,
                        FantasyEarth.id("textures/entity/aether_fox.png"), 0.46F, 1.0F));
        event.registerEntityRenderer(FantasyEarth.RUNE_WOLF.get(), context ->
                new FamiliarRenderer<>(context, RuneWolfModel.LAYER_LOCATION, RuneWolfModel::new,
                        FantasyEarth.id("textures/entity/rune_wolf.png"), 0.52F, 1.0F));
        event.registerEntityRenderer(FantasyEarth.MANA_WISP.get(), context ->
                new FamiliarRenderer<>(context, ManaWispModel.LAYER_LOCATION, ManaWispModel::new,
                        FantasyEarth.id("textures/entity/mana_wisp.png"), 0.18F, 0.82F));
        event.registerEntityRenderer(FantasyEarth.CRYSTAL_ARMORED_SPIDER.get(), CrystalSpiderRenderer::new);
        event.registerEntityRenderer(FantasyEarth.ARCANE_SETTLER.get(), ArcaneSettlerRenderer::new);
    }

    private static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(RunicWatcherModel.LAYER_LOCATION, RunicWatcherModel::createBodyLayer);
        event.registerLayerDefinition(AetherFoxModel.LAYER_LOCATION, AetherFoxModel::createBodyLayer);
        event.registerLayerDefinition(RuneWolfModel.LAYER_LOCATION, RuneWolfModel::createBodyLayer);
        event.registerLayerDefinition(ManaWispModel.LAYER_LOCATION, ManaWispModel::createBodyLayer);
        event.registerLayerDefinition(CrystalSpiderModel.LAYER_LOCATION, CrystalSpiderModel::createBodyLayer);
        event.registerLayerDefinition(ArcaneSettlerModel.LAYER_LOCATION, ArcaneSettlerModel::createBodyLayer);
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(FantasyEarth.id("arcane_focus_hud"), ArcaneFocusHud::render);
    }

    private static void registerPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(ArcaneStatusPayload.TYPE, FantasyEarthClient::handleArcaneStatus);
        event.register(ArcaneVisualPayload.TYPE, FantasyEarthClient::handleArcaneVisual);
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(OPEN_ATTUNEMENT);
        event.register(ACTIVATE_ARCANE_SKILL);
    }

    private static void clientTick(ClientTickEvent.Post event) {
        ArcanePlayerAnimations.tick();
        while (OPEN_ATTUNEMENT.consumeClick()) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null && minecraft.getConnection() != null) {
                requestOpenAttunement();
            }
        }
        while (ACTIVATE_ARCANE_SKILL.consumeClick()) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null && minecraft.getConnection() != null && minecraft.gui.screen() == null) {
                requestActivateArcaneSkill();
            }
        }
    }

    private static void handleArcaneStatus(ArcaneStatusPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            arcaneStatus = payload;
            if (payload.openScreen() && !(Minecraft.getInstance().gui.screen() instanceof ArcaneAttunementScreen)) {
                Minecraft.getInstance().gui.setScreen(new ArcaneAttunementScreen());
            }
        });
    }

    private static void handleArcaneVisual(ArcaneVisualPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ArcanePlayerAnimations.start(payload));
    }

    public static ArcaneStatusPayload arcaneStatus() {
        return arcaneStatus;
    }

    public static void requestArcaneFocus(ArcaneFocus focus) {
        ClientPacketDistributor.sendToServer(new ArcaneActionPayload(focus.id()));
    }

    public static void requestStopAttuning() {
        ClientPacketDistributor.sendToServer(new ArcaneActionPayload(ArcaneActionPayload.STOP_RIDING));
    }

    public static void requestOpenAttunement() {
        ClientPacketDistributor.sendToServer(new ArcaneActionPayload(ArcaneActionPayload.OPEN_PANEL));
    }

    public static void requestPractice() {
        ClientPacketDistributor.sendToServer(new ArcaneActionPayload(ArcaneActionPayload.PRACTICE));
    }

    public static void requestActivateArcaneSkill() {
        ClientPacketDistributor.sendToServer(new ArcaneActionPayload(ArcaneActionPayload.ACTIVATE_SKILL));
    }

    public static void requestAttunementRefresh() {
        ClientPacketDistributor.sendToServer(new ArcaneActionPayload(ArcaneActionPayload.REFRESH_STATUS));
    }

    public static void openHandbook() {
        if (Minecraft.getInstance().getConnection() != null) {
            requestAttunementRefresh();
        }
        Minecraft.getInstance().gui.setScreen(new MagicHandbookScreen());
    }
}
