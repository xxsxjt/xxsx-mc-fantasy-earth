package com.xxsx.fantasyearth;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class ArcanaEvents {
    private static final String ARCANA_PREFIX = "earth_arcana.";

    private ArcanaEvents() {
    }

    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (ModList.get().isLoaded("xuanhuan_earth") || ModList.get().isLoaded("earth_online_xuanhuan")) {
            return;
        }
        CompoundTag source = event.getOriginal().getPersistentData();
        CompoundTag target = event.getEntity().getPersistentData();
        ArcanaPower.migrateLegacyData(source);
        ArcanaPower.migrateLegacyData(target);
        for (String key : source.keySet()) {
            if (!key.startsWith(ARCANA_PREFIX)) {
                continue;
            }
            Tag value = source.get(key);
            if (value != null) {
                target.put(key, value.copy());
            }
        }
    }
}
