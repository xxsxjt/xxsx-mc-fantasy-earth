package com.xxsx.earthonlinemagic.settlement;

import com.xxsx.earthonlinemagic.EarthOnlineMagic;
import com.xxsx.earthonlinemagic.entity.ArcaneSettlerEntity;
import com.xxsx.earthonlinemagic.entity.ContractableFamiliarEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class MagicSettlementAnchorBlockEntity extends BlockEntity {
    private static final int WARMUP_TICKS = 40;
    private static final int MAX_SPAWNS_PER_SERVER_TICK = 2;
    private static final double ACTIVATION_RADIUS = 48.0D;
    private static MinecraftServer budgetServer;
    private static int budgetTick = Integer.MIN_VALUE;
    private static int spawnsThisTick;

    private String settlementType = MagicSettlementFeature.Type.WITCH_HAMLET.id();
    private boolean initialized;
    private int warmupTicks;
    private int spawnIndex;

    public MagicSettlementAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(EarthOnlineMagic.SETTLEMENT_ANCHOR_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MagicSettlementAnchorBlockEntity anchor) {
        if (anchor.initialized || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (++anchor.warmupTicks < WARMUP_TICKS) {
            return;
        }
        if (!anchor.hasNearbyPlayer(serverLevel, pos)) {
            return;
        }
        if (anchor.spawnIndex >= anchor.expectedSpawnCount()) {
            anchor.finishInitialization(pos);
            return;
        }
        if (!tryAcquireSpawnBudget(serverLevel)) {
            return;
        }
        anchor.spawnNextResident(serverLevel, pos);
        anchor.spawnIndex++;
        anchor.setChanged();
        if (anchor.spawnIndex >= anchor.expectedSpawnCount()) {
            anchor.finishInitialization(pos);
        }
    }

    private static boolean tryAcquireSpawnBudget(ServerLevel level) {
        MinecraftServer server = level.getServer();
        int tick = server.getTickCount();
        if (budgetServer != server || budgetTick != tick) {
            budgetServer = server;
            budgetTick = tick;
            spawnsThisTick = 0;
        }
        if (spawnsThisTick >= MAX_SPAWNS_PER_SERVER_TICK) {
            return false;
        }
        spawnsThisTick++;
        return true;
    }

    private boolean hasNearbyPlayer(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;
        double radiusSquared = ACTIVATION_RADIUS * ACTIVATION_RADIUS;
        return !level.getPlayers(player -> player.distanceToSqr(x, y, z) <= radiusSquared, 1).isEmpty();
    }

    private int expectedSpawnCount() {
        return MagicSettlementFeature.Type.byId(settlementType) == MagicSettlementFeature.Type.GOBLIN_EXCHANGE
                ? 3 : 4;
    }

    private void spawnNextResident(ServerLevel level, BlockPos pos) {
        switch (MagicSettlementFeature.Type.byId(settlementType)) {
            case WITCH_HAMLET -> {
                switch (spawnIndex) {
                    case 0 -> spawnResident(level, pos.offset(-3, 1, 0), ArcaneSettlerEntity.Role.HEDGE_WITCH);
                    case 1 -> spawnResident(level, pos.offset(3, 1, 0), ArcaneSettlerEntity.Role.HEDGE_WITCH);
                    case 2 -> spawnResident(level, pos.offset(0, 1, -2), ArcaneSettlerEntity.Role.GOBLIN_APPRAISER);
                    case 3 -> spawnFamiliar(level, pos.offset(3, 1, 3), EarthOnlineMagic.MANA_WISP.get());
                    default -> { }
                }
            }
            case GOBLIN_EXCHANGE -> {
                switch (spawnIndex) {
                    case 0 -> spawnResident(level, pos.offset(-2, 1, 0), ArcaneSettlerEntity.Role.GOBLIN_APPRAISER);
                    case 1 -> spawnResident(level, pos.offset(2, 1, 0), ArcaneSettlerEntity.Role.GOBLIN_APPRAISER);
                    case 2 -> spawnResident(level, pos.offset(0, 1, 3), ArcaneSettlerEntity.Role.HEDGE_WITCH);
                    default -> { }
                }
            }
            case ACADEMY_OUTPOST -> {
                switch (spawnIndex) {
                    case 0 -> spawnResident(level, pos.offset(-2, 1, 0), ArcaneSettlerEntity.Role.ACADEMY_RESEARCHER);
                    case 1 -> spawnResident(level, pos.offset(2, 1, 0), ArcaneSettlerEntity.Role.ACADEMY_RESEARCHER);
                    case 2 -> spawnResident(level, pos.offset(0, 1, 3), ArcaneSettlerEntity.Role.GOBLIN_APPRAISER);
                    case 3 -> spawnFamiliar(level, pos.offset(-3, 1, 3), EarthOnlineMagic.AETHER_FOX.get());
                    default -> { }
                }
            }
        }
    }

    private void finishInitialization(BlockPos pos) {
        initialized = true;
        setChanged();
        EarthOnlineMagic.LOGGER.info("Initialized magic settlement {} at {}", settlementType, pos);
    }

    private static void spawnResident(ServerLevel level, BlockPos pos, ArcaneSettlerEntity.Role role) {
        ArcaneSettlerEntity resident = EarthOnlineMagic.ARCANE_SETTLER.get().create(level, EntitySpawnReason.STRUCTURE);
        if (resident == null) {
            return;
        }
        BlockPos spawnPos = findOpenSpawn(level, pos);
        resident.setRole(role);
        resident.snapTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                level.getRandom().nextFloat() * 360.0F, 0.0F);
        resident.setPersistenceRequired();
        if (!level.addFreshEntity(resident)) {
            EarthOnlineMagic.LOGGER.warn("Could not add magic settlement resident at {}", spawnPos);
        }
    }

    private static void spawnFamiliar(ServerLevel level, BlockPos pos,
                                      EntityType<? extends ContractableFamiliarEntity> type) {
        ContractableFamiliarEntity familiar = type.create(level, EntitySpawnReason.STRUCTURE);
        if (familiar == null) {
            return;
        }
        BlockPos spawnPos = findOpenSpawn(level, pos);
        familiar.snapTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                level.getRandom().nextFloat() * 360.0F, 0.0F);
        familiar.setPersistenceRequired();
        if (!level.addFreshEntity(familiar)) {
            EarthOnlineMagic.LOGGER.warn("Could not add magic settlement familiar at {}", spawnPos);
        }
    }

    private static BlockPos findOpenSpawn(ServerLevel level, BlockPos preferred) {
        int[] verticalOffsets = {-1, 0, 1, -2};
        for (int verticalOffset : verticalOffsets) {
            for (int radius = 0; radius <= 3; radius++) {
                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (Math.max(Math.abs(x), Math.abs(z)) != radius) {
                            continue;
                        }
                        BlockPos candidate = preferred.offset(x, verticalOffset, z);
                        if (!level.isEmptyBlock(candidate.below())
                                && level.isEmptyBlock(candidate)
                                && level.isEmptyBlock(candidate.above())) {
                            return candidate;
                        }
                    }
                }
            }
        }
        return preferred;
    }

    public void configure(String type) {
        settlementType = MagicSettlementFeature.Type.byId(type).id();
        initialized = false;
        warmupTicks = 0;
        spawnIndex = 0;
        setChanged();
    }

    public String settlementType() {
        return settlementType;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("SettlementType", settlementType);
        output.putBoolean("Initialized", initialized);
        output.putInt("WarmupTicks", warmupTicks);
        output.putInt("SpawnIndex", spawnIndex);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        settlementType = MagicSettlementFeature.Type.byId(
                input.getStringOr("SettlementType", MagicSettlementFeature.Type.WITCH_HAMLET.id())).id();
        initialized = input.getBooleanOr("Initialized", false);
        warmupTicks = input.getIntOr("WarmupTicks", 0);
        spawnIndex = Math.max(0, input.getIntOr("SpawnIndex", 0));
    }
}
