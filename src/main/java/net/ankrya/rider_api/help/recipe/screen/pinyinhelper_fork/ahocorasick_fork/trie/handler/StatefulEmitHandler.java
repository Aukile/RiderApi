package net.ankrya.rider_api.help.recipe.screen.pinyinhelper_fork.ahocorasick_fork.trie.handler;

import net.ankrya.rider_api.help.recipe.screen.pinyinhelper_fork.ahocorasick_fork.trie.Emit;

import java.util.List;

public interface StatefulEmitHandler extends EmitHandler {
    List<Emit> getEmits();
}
