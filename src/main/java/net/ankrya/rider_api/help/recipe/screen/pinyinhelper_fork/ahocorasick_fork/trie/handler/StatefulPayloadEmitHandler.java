package net.ankrya.rider_api.help.recipe.screen.pinyinhelper_fork.ahocorasick_fork.trie.handler;

import net.ankrya.rider_api.help.recipe.screen.pinyinhelper_fork.ahocorasick_fork.trie.PayloadEmit;

import java.util.List;

public interface StatefulPayloadEmitHandler<T> extends PayloadEmitHandler<T>{
    List<PayloadEmit<T>> getEmits();
}
