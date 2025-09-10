package net.ankrya.rider_api.mixin;

import net.ankrya.rider_api.interfaces.geo.IGeoItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(method = "initializeClient(Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true, remap = false)
    public void initializeClient(Consumer<IClientItemExtensions> consumer, CallbackInfo ci) {
        if (this instanceof IGeoItem item) {
            item.createGeoRenderer(consumer);
            ci.cancel();
        }
    }
}
