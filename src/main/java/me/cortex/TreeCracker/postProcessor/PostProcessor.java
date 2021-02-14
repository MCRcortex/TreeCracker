package me.cortex.TreeCracker.postProcessor;

import kaptainwutax.seedutils.lcg.LCG;
import kaptainwutax.seedutils.lcg.rand.JRand;
import me.cortex.TreeCracker.LCG.DiscreteLog;
import me.cortex.TreeCracker.NotImplementedException;
import me.cortex.TreeCracker.program.TreeCrackerProgram;
import randomreverser.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

//TODO: CLEAN UP THIS CODE
public class PostProcessor {
    TreeCrackerProgram.Tree[] TREES;
    int LOOKBACK;

    public PostProcessor(TreeCrackerProgram.Tree[] trees, int lookback_range) {
        this.TREES = trees;
        this.LOOKBACK = lookback_range;
    }

    public boolean canTreeSeedExist(long seed) {
        throw new NotImplementedException();
    }
}
