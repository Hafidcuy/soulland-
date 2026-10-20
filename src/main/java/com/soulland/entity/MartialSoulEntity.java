package com.soulland.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Martial soul yang melayang di belakang pemiliknya.
 *
 * <p>Ini yang membuat martial soul terasa hidup, bukan sekadar teks di chat. Saat pemilik
 * memakai skill, companion ini ikut maju supaya aksi terlihat punya sumber.
 */
public class MartialSoulEntity extends Entity {

    private static final EntityDataAccessor<String> DATA_SOUL =
            SynchedEntityData.defineId(MartialSoulEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_ECHO =
            SynchedEntityData.defineId(MartialSoulEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TINT =
            SynchedEntityData.defineId(MartialSoulEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_OWNER =
            SynchedEntityData.defineId(MartialSoulEntity.class, EntityDataSerializers.STRING);

    private int echoTicks = 0;
    private double anchorX;
    private double anchorY;
    private double anchorZ;

    public MartialSoulEntity(EntityType<? extends MartialSoulEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_SOUL, "");
        this.entityData.define(DATA_ECHO, 0);
        this.entityData.define(DATA_TINT, 0xFFFFFF);
        this.entityData.define(DATA_OWNER, "");
    }

    public void setOwner(java.util.UUID uuid) {
        this.entityData.set(DATA_OWNER, uuid == null ? "" : uuid.toString());
    }

    public java.util.UUID getOwnerId() {
        String value = this.entityData.get(DATA_OWNER);
        if (value == null || value.isEmpty()) return null;
        try {
            return java.util.UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String soulId() {
        return this.entityData.get(DATA_SOUL);
    }

    public void setSoulId(String id) {
        this.entityData.set(DATA_SOUL, id == null ? "" : id);
    }

    public int tint() {
        return this.entityData.get(DATA_TINT);
    }

    public void setTint(int tint) {
        this.entityData.set(DATA_TINT, tint);
    }

    /** Dipanggil server saat pemilik memakai skill: companion maju sekejap lalu kembali. */
    public void echo() {
        this.echoTicks = 14;
        this.entityData.set(DATA_ECHO, this.echoTicks);
    }

    public float echoProgress() {
        int value = this.entityData.get(DATA_ECHO);
        return value <= 0 ? 0f : value / 14f;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }

        ServerPlayer owner = owner();
        if (owner == null || owner.isRemoved()) {
            this.discard();
            return;
        }

        if (this.echoTicks > 0) {
            this.echoTicks--;
            this.entityData.set(DATA_ECHO, this.echoTicks);
        }

        Vec3 look = owner.getLookAngle();
        double side = 1.1;
        double back = 1.6 - this.echoTicks * 0.25;
        double up = 1.5 + Math.sin(this.tickCount * 0.1) * 0.15;

        double targetX = owner.getX() - look.x * back + look.z * side;
        double targetY = owner.getY() + up;
        double targetZ = owner.getZ() - look.z * back - look.x * side;

        if (this.tickCount < 3) {
            this.anchorX = targetX;
            this.anchorY = targetY;
            this.anchorZ = targetZ;
        } else {
            this.anchorX += (targetX - this.anchorX) * 0.18;
            this.anchorY += (targetY - this.anchorY) * 0.18;
            this.anchorZ += (targetZ - this.anchorZ) * 0.18;
        }

        this.setPos(this.anchorX, this.anchorY, this.anchorZ);
        this.setYRot(owner.getYRot());
        this.setXRot(owner.getXRot());
    }

    private ServerPlayer owner() {
        java.util.UUID uuid = getOwnerId();
        if (uuid == null) return null;
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            return serverLevel.getServer().getPlayerList().getPlayer(uuid);
        }
        return null;
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
        tag.putString("Soul", this.entityData.get(DATA_SOUL));
        tag.putInt("Tint", this.entityData.get(DATA_TINT));
        tag.putString("Owner", this.entityData.get(DATA_OWNER));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        this.entityData.set(DATA_SOUL, tag.getString("Soul"));
        this.entityData.set(DATA_TINT, tag.getInt("Tint"));
        if (tag.contains("Owner")) {
            this.entityData.set(DATA_OWNER, tag.getString("Owner"));
        }
    }
}
