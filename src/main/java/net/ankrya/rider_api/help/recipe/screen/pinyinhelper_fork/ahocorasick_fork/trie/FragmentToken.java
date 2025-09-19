package net.ankrya.rider_api.help.recipe.screen.pinyinhelper_fork.ahocorasick_fork.trie;

public class FragmentToken extends Token {

    public FragmentToken(String fragment) {
        super(fragment);
    }

    @Override
    public boolean isMatch() {
        return false;
    }

    @Override
    public Emit getEmit() {
        return null;
    }

}
