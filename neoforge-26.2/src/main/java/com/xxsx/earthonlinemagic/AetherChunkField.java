package com.xxsx.earthonlinemagic;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;

public final class AetherChunkField {
    private static final long REFRESH_INTERVAL_TICKS = 600L;
    private static final double MAX_DISTURBANCE = 45.0D;
    private static final double DISTURBANCE_RECOVERY_PER_TICK = 0.003D;
    private static final int SCAN_RADIUS_XZ = 8;
    private static final int SCAN_RADIUS_Y = 5;

    private static final Map<ResourceKey<Level>, Map<Long, FieldState>> CACHE = new HashMap<>();

    private AetherChunkField() {
    }

    public static Reading read(Level level, BlockPos pos) {
        synchronized (CACHE) {
            FieldState state = state(level, pos);
            state.refresh(level, pos);
            return state.reading();
        }
    }

    public static void disturb(Level level, BlockPos pos, double restoredMana) {
        if (restoredMana <= 0.0D) {
            return;
        }
        synchronized (CACHE) {
            FieldState state = state(level, pos);
            state.refresh(level, pos);
            state.disturbance = clamp(state.disturbance + Math.max(0.5D, restoredMana * 0.35D), 0.0D, MAX_DISTURBANCE);
            state.lastUpdate = level.getGameTime();
        }
    }

    public static Component gradeName(int value) {
        if (value >= 75) {
            return Component.translatable("aether.earth_online_magic.grade.dense");
        }
        if (value >= 50) {
            return Component.translatable("aether.earth_online_magic.grade.stable");
        }
        if (value >= 25) {
            return Component.translatable("aether.earth_online_magic.grade.faint");
        }
        return Component.translatable("aether.earth_online_magic.grade.quiet");
    }

    private static FieldState state(Level level, BlockPos pos) {
        ChunkPos chunk = ChunkPos.containing(pos);
        Map<Long, FieldState> dimension = CACHE.computeIfAbsent(level.dimension(), key -> new HashMap<>());
        return dimension.computeIfAbsent(chunk.pack(), key -> new FieldState(level, pos, chunk));
    }

    private static int naturalBase(Level level, BlockPos pos, ChunkPos chunk) {
        String dimension = level.dimension().identifier().toString();
        int value;
        if ("minecraft:overworld".equals(dimension)) {
            value = 30;
        } else if ("minecraft:the_nether".equals(dimension)) {
            value = 22;
        } else if ("minecraft:the_end".equals(dimension)) {
            value = 34;
        } else {
            value = 24;
        }

        int y = pos.getY();
        if (y > 112) {
            value += 9;
        } else if (y > 72) {
            value += 5;
        } else if (y < -16) {
            value += 5;
        }

        long seed = level instanceof ServerLevel serverLevel ? serverLevel.getSeed() : 0L;
        long mixed = mix(seed
                ^ ((long) chunk.x() * 0xD6E8FEB86659FD93L)
                ^ ((long) chunk.z() * 0xA5A3564E27F886ABL)
                ^ dimension.hashCode());
        value += Math.floorMod(mixed, 31) - 10;
        return clamp(value, 0, 100);
    }

    private static SourceScan scanSources(Level level, BlockPos pos) {
        int crystal = 0;
        int ritual = 0;
        int rune = 0;
        int workbench = 0;

        int minX = pos.getX() - SCAN_RADIUS_XZ;
        int maxX = pos.getX() + SCAN_RADIUS_XZ;
        int minY = pos.getY() - SCAN_RADIUS_Y;
        int maxY = pos.getY() + SCAN_RADIUS_Y;
        int minZ = pos.getZ() - SCAN_RADIUS_XZ;
        int maxZ = pos.getZ() + SCAN_RADIUS_XZ;

        for (BlockPos sample : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            Block block = level.getBlockState(sample).getBlock();
            if (block == EarthOnlineMagic.AETHER_CRYSTAL_CLUSTER.get() || block == Blocks.AMETHYST_CLUSTER) {
                crystal = Math.min(36, crystal + 18);
            } else if (block == EarthOnlineMagic.RITUAL_PEDESTAL.get() || block == Blocks.ENCHANTING_TABLE) {
                ritual = Math.min(30, ritual + 15);
            } else if (block == EarthOnlineMagic.RUNE_CARVING_TABLE.get() || block == Blocks.BOOKSHELF) {
                rune = Math.min(22, rune + 5);
            } else if (block == EarthOnlineMagic.ALCHEMY_TABLE.get()
                    || block == Blocks.GLOWSTONE
                    || block == Blocks.REDSTONE_BLOCK) {
                workbench = Math.min(18, workbench + 4);
            }
        }

        Component source = Component.translatable("aether.earth_online_magic.source.natural");
        int strongest = 0;
        if (crystal > strongest) {
            strongest = crystal;
            source = Component.translatable("aether.earth_online_magic.source.crystal");
        }
        if (ritual > strongest) {
            strongest = ritual;
            source = Component.translatable("aether.earth_online_magic.source.ritual");
        }
        if (rune > strongest) {
            strongest = rune;
            source = Component.translatable("aether.earth_online_magic.source.rune");
        }
        if (workbench > strongest) {
            source = Component.translatable("aether.earth_online_magic.source.workbench");
        }
        return new SourceScan(crystal, ritual, rune, workbench, source);
    }

    private static long mix(long value) {
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53L;
        value ^= value >>> 33;
        return value;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public record Reading(int value, double disturbance, Component mainSource) {
    }

    private record SourceScan(int crystal, int ritual, int rune, int workbench, Component mainSource) {
    }

    private static final class FieldState {
        private int base;
        private int crystal;
        private int ritual;
        private int rune;
        private int workbench;
        private double disturbance;
        private long lastRefresh;
        private long lastUpdate;
        private Component mainSource = Component.translatable("aether.earth_online_magic.source.natural");

        private FieldState(Level level, BlockPos pos, ChunkPos chunk) {
            this.base = naturalBase(level, pos, chunk);
            this.lastRefresh = Long.MIN_VALUE;
            this.lastUpdate = level.getGameTime();
        }

        private void refresh(Level level, BlockPos pos) {
            long now = level.getGameTime();
            recover(now - lastUpdate);
            if (lastRefresh == Long.MIN_VALUE || now - lastRefresh >= REFRESH_INTERVAL_TICKS) {
                ChunkPos chunk = ChunkPos.containing(pos);
                base = naturalBase(level, pos, chunk);
                SourceScan scan = scanSources(level, pos);
                crystal = scan.crystal();
                ritual = scan.ritual();
                rune = scan.rune();
                workbench = scan.workbench();
                mainSource = scan.mainSource();
                lastRefresh = now;
            }
            lastUpdate = now;
        }

        private void recover(long elapsedTicks) {
            if (elapsedTicks <= 0L || disturbance <= 0.0D) {
                return;
            }
            disturbance = clamp(disturbance - elapsedTicks * DISTURBANCE_RECOVERY_PER_TICK, 0.0D, MAX_DISTURBANCE);
        }

        private Reading reading() {
            int value = clamp((int) Math.round(base + crystal + ritual + rune + workbench - disturbance), 0, 100);
            Component source = disturbance >= 28.0D && value < 35
                    ? Component.translatable("aether.earth_online_magic.source.disturbed")
                    : mainSource;
            return new Reading(value, disturbance, source);
        }
    }
}
