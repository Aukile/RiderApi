package net.ankrya.rider_api.help.recipe.screen.pinyinhelper_fork.ahocorasick_fork.trie;

import net.ankrya.rider_api.help.recipe.screen.pinyinhelper_fork.ahocorasick_fork.interval.Interval;
import net.ankrya.rider_api.help.recipe.screen.pinyinhelper_fork.ahocorasick_fork.interval.Intervalable;

/**
 * Responsible for tracking the bounds of matched terms.
 */
public class Emit extends Interval implements Intervalable {
    private final String keyword;

    public Emit(final int start, final int end, final String keyword) {
        super(start, end);
        this.keyword = keyword;
    }

    public String getKeyword() {
        return this.keyword;
    }

    @Override
    public String toString() {
        return super.toString() + "=" + this.keyword;
    }

}
