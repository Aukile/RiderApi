package net.ankrya.rider_api.help.recipe.screen.pinyinhelper_fork.ahocorasick_fork.trie.handler;

import net.ankrya.rider_api.help.recipe.screen.pinyinhelper_fork.ahocorasick_fork.trie.PayloadEmit;

import java.util.ArrayList;
import java.util.List;

public class DefaultPayloadEmitHandler<T> implements StatefulPayloadEmitHandler<T> {

    private final List<PayloadEmit<T>> emits = new ArrayList<>();

    @Override
    public boolean emit(final PayloadEmit<T> emit) {
        this.emits.add(emit);
        return true;
    }

    @Override
    public List<PayloadEmit<T>> getEmits() {
        return this.emits;
    }
}
