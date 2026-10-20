package com.soulland.entity;

import com.soulland.SoulLandMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Bentuk energi yang muncul saat skill besar dilepas.
 *
 * <p>Satu entri dengan beberapa mode supaya jumlah kelas render tetap kecil: gelombang
 * tanah, berkas lurus, pilar vertikal, dan bola yang meledak.
 */
public class SoulVfxEntity extends Entity {

    public enum Mode {
        /** Cincin yang melebar di tanah. */
        WAVE,
        /** Berkas energi lurus ke depan. */
        BEAM,
        /** Pilar cahaya vertikal. */
        COLUMN,
        /** Bola energi meledak ke segala arah. */
        ORB
    }

    private static final EntityDataAccessor<Integer> DATA_MODE =
            SynchedEntityData.defineId(SoulVfxEntity.class, EntityDataSerializers.INTEGER);
    private static final EntityDataAccessor<Integer> DATA_COLOR =
            SynchedEntityData.defineId(SoulVfxEntity.class, EntityDataSerializers.INTEGER);
    private static final EntityDataAccessor<Integer> DATA_COLOR2 =
            SynchedEntityData.defineId(SoulVfxEntity.class, EntityDataSerializers.INTEGER);
    private static final EntityDataAccessor<Integer> DATA_LIFE =
            SynchedEntityData.defineId(SoulVfxEntity.class, EntityDataSerializers.INTEGER);
    private static final EntityDataAccessor<Float> DATA_SIZE =
            SynchedEntityData.defineId(SoulVfxEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_DIR_X =
            SynchedEntityData.defineId(SoulVfxEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_DIR_Y =
            SynchedEntityData.defineId(SoulVfxEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_DIR_Z =
            SynchedEntityData.defineId(SoulVfxEntity.class, EntityDataSerializers.FLOAT);

    private int maxLife = 24;

    public SoulVfxEntity(EntityType<? extends SoulVfxEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public SoulVfxEntity(Level level, Mode mode) {
        this(com.soulland.entity.ModEntities.SOUL_VFX.get(), level);
        setMode(mode);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_MODE, 0);
        this.entityData.define(DATA_COLOR, 0xFFFFFF);
        this.entityData.define(DATA_COLOR2, 0xFFFFFF);
        this.entityData.define(DATA_LIFE, 24);
        this.entityData.define(DATA_SIZE, 4f);
        this.entityData.define(DATA_DIR_X, 0f);
        this.entityData.define(DATA_DIR_Y, 0f);
        this.entityData.define(DATA_DIR_Z, 1f);
    }

    /** Arah berkas energi. Disimpan sebagai angka supaya renderer tidak bergantung pada rotasi entitas. */
    public void setDirection(float x, float y, float z) {
        this.entityData.set(DATA_DIR_X, x);
        this.entityData.set(DATA_DIR_Y, y);
        this.entityData.set(DATA_DIR_Z, z);
    }

    public float dirX() {
        return this.entityData.get(DATA_DIR_X);
    }

    public float dirY() {
        return this.entityData.get(DATA_DIR_Y);
    }

    public float dirZ() {
        return this.entityData.get(DATA_DIR_Z);
    }

    public void setMode(Mode mode) {
        this.entityData.set(DATA_MODE, mode.ordinal());
    }

    public Mode mode() {
        Mode[] values = Mode.values();
        int index = this.entityData.get(DATA_MODE);
        return index >= 0 && index < values.length ? values[index] : Mode.WAVE;
    }

    public void setColors(int color, int color2) {
        this.entityData.set(DATA_COLOR, color);
        this.entityData.set(DATA_COLOR2, color2);
    }

    public int color() {
        return this.entityData.get(DATA_COLOR);
    }

    public int color2() {
        return this.entityData.get(DATA_COLOR2);
    }

    public void setLife(int life) {
        this.maxLife = Math.max(1, life);
        this.entityData.set(DATA_LIFE, this.maxLife);
    }

    public void setSize(float size) {
        this.entityData.set(DATA_SIZE, Math.max(1f, size));
    }

    public float size() {
        return this.entityData.get(DATA_SIZE);
    }

    /** Kemajuan hidup entitas, 0.0 sampai 1.0. */
    public float progress() {
        int life = this.entityData.get(DATA_LIFE);
        return 1f - MthClamp(life / (float) this.maxLife);
    }

    private static float MthClamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    @Override
    public void tick() {
        super.tick();
        int life = this.entityData.get(DATA_LIFE) - 1;
        this.entityData.set(DATA_LIFE, life);
        if (life <= 0) {
            this.discard();
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Mode", this.entityData.get(DATA_MODE));
        tag.putInt("Color", this.entityData.get(DATA_COLOR));
        tag.putInt("Color2", this.entityData.get(DATA_COLOR2));
        tag.putInt("Life", this.entityData.get(DATA_LIFE));
        tag.putFloat("Size", this.entityData.get(DATA_SIZE));
        tag.putFloat("DirX", this.entityData.get(DATA_DIR_X));
        tag.putFloat("DirY", this.entityData.get(DATA_DIR_Y));
        tag.putFloat("DirZ", this.entityData.get(DATA_DIR_Z));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        this.entityData.set(DATA_MODE, tag.getInt("Mode"));
        this.entityData.set(DATA_COLOR, tag.getInt("Color"));
        this.entityData.set(DATA_COLOR2, tag.getInt("Color2"));
        this.maxLife = Math.max(1, tag.getInt("Life"));
        this.entityData.set(DATA_LIFE, this.maxLife);
        this.entityData.set(DATA_SIZE, tag.getFloat("Size"));
        this.entityData.set(DATA_DIR_X, tag.getFloat("DirX"));
        this.entityData.set(DATA_DIR_Y, tag.getFloat("DirY"));
        this.entityData.set(DATA_DIR_Z, tag.getFloat("DirZ"));
    }

    public static String registryName() {
        return SoulLandMod.MODID;
    }
}
