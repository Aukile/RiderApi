package net.ankrya.rider_api.interfaces.inside_use;

import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

public interface IVariable<T> {
    int typeId();

    void write(FriendlyByteBuf buf, T value);
    T read(FriendlyByteBuf buf);

    Tag write(T value);
    T read(Tag nbt);
}
