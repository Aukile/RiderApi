package net.ankrya.rider_api.help.recipe.screen.pinyinhelper_fork.ahocorasick_fork.trie.handler;

import net.ankrya.rider_api.help.recipe.screen.pinyinhelper_fork.ahocorasick_fork.trie.Emit;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractStatefulEmitHandler implements StatefulEmitHandler {

    private final List<Emit> emits = new ArrayList<>();

    public void addEmit(final Emit emit) {
        this.emits.add(emit);
    }

    @Override
    public List<Emit> getEmits() {
        return this.emits;
    }

}
