package net.ankrya.rider_api.interfaces;

import net.minecraft.client.resources.model.ModelState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public interface ICCosmic {
    @OnlyIn(Dist.CLIENT)
    ModelState getModeState();
}
