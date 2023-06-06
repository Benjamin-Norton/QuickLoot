package com.bawnorton.quickloot.mixin.client;

import com.bawnorton.quickloot.extend.EntityQuickLootContainer;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HopperMinecartEntity.class)
public abstract class HopperMinecartEntityMixin implements EntityQuickLootContainer {
}
