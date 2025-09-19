package net.ankrya.rider_api.help.recipe.screen.pinyinhelper_fork.ahocorasick_fork.trie.handler;

import net.ankrya.rider_api.help.recipe.screen.pinyinhelper_fork.ahocorasick_fork.trie.PayloadEmit;

public interface PayloadEmitHandler<T> {
    boolean emit(PayloadEmit<T> emit);
}
