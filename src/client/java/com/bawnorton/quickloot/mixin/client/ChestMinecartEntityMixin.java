package com.bawnorton.quickloot.mixin.client;

import com.bawnorton.quickloot.extend.EntityQuickLootContainer;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChestMinecartEntity.class)
public abstract class ChestMinecartEntityMixin implements EntityQuickLootContainer {
}
