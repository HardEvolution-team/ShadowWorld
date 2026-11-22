package com.electrifiedded.shadowworld.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

/** Thrown element stones: 0 fire, 1 water, 2 earth, 3 air, 4 element sphere. */
public class EntityElementStone extends EntityThrowable {
    private static final DataParameter<Integer> TYPE = EntityDataManager.createKey(EntityElementStone.class, DataSerializers.VARINT);

    public EntityElementStone(World w) { super(w); }

    public EntityElementStone(World w, EntityLivingBase thrower, int type) {
        super(w, thrower);
        dataManager.set(TYPE, type);
    }

    @Override protected void entityInit() { super.entityInit(); dataManager.register(TYPE, 0); }
    public int getType() { return dataManager.get(TYPE); }

    @Override
    protected void onImpact(RayTraceResult r) {
        if (world.isRemote) return;
        BlockPos p = new BlockPos(posX, posY, posZ);
        switch (getType()) {
            case 0 -> {
                if (r.entityHit != null) r.entityHit.setFire(100);
                if (world.isAirBlock(p)) world.setBlockState(p, Blocks.FIRE.getDefaultState());
            }
            case 1 -> { if (world.isAirBlock(p)) world.setBlockState(p, Blocks.FLOWING_WATER.getDefaultState()); }
            case 2 -> world.createExplosion(this, posX, posY, posZ, 10.0F, true);
            case 3 -> world.addWeatherEffect(new EntityLightningBolt(world, posX, posY, posZ, false));
            default -> {
                world.addWeatherEffect(new EntityLightningBolt(world, posX - 1, posY, posZ - 1, false));
                world.addWeatherEffect(new EntityLightningBolt(world, posX + 1, posY, posZ + 1, false));
                world.createExplosion(this, posX, posY, posZ, 15.0F, true);
                world.setBlockState(p.add(1, 0, 1), Blocks.FLOWING_WATER.getDefaultState());
                world.setBlockState(p.add(-1, 0, -1), Blocks.FLOWING_LAVA.getDefaultState());
            }
        }
        setDead();
    }
}
