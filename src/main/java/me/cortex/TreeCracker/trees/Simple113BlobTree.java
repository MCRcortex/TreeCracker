package me.cortex.TreeCracker.trees;

import me.cortex.TreeCracker.LCG.LcgTester;
import me.cortex.TreeCracker.NotImplementedException;

public class Simple113BlobTree implements ICrackableTree {
    /*
        OAK = register("oak", Feature.TREE.configure((new TreeFeatureConfig.Builder(new SimpleBlockStateProvider(ConfiguredFeatures.States.OAK_LOG), new SimpleBlockStateProvider(ConfiguredFeatures.States.OAK_LEAVES), new BlobFoliagePlacer(UniformIntDistribution.of(2), UniformIntDistribution.of(0), 3), new StraightTrunkPlacer(4, 2, 0), new TwoLayersFeatureSize(1, 0, 1))).ignoreVines().build()));
        BIRCH_OTHER = register("birch_other", Feature.RANDOM_SELECTOR.configure(new RandomFeatureConfig(ImmutableList.of(BIRCH_BEES_0002.withChance(0.2F), FANCY_OAK_BEES_0002.withChance(0.1F)), OAK_BEES_0002)).decorate(ConfiguredFeatures.Decorators.SQUARE_HEIGHTMAP).decorate(Decorator.COUNT_EXTRA.configure(new CountExtraDecoratorConfig(10, 0.1F, 1))));
    */
    Simple113BlobTreeConfig config;
    TreePos pos;
    int treeTrunkHeight;
    int[] leaves;

    private static int[] strToLeafArray(String leaves) {
        int[] leaveP = new int[12];
        int i = 0;
        for (char c : leaves.toCharArray()) {
            if (c == '0') {
                leaveP[i] = 0;
            }
            else if (c == '1') {
                leaveP[i] = 1;
            }
            else if (c == '2') {
                leaveP[i] = -1;
            } else if (c == ' ') {
                continue;
            } else {
                throw new IllegalArgumentException("E");
            }
            i++;
        }
        return leaveP;
    }
    public Simple113BlobTree(Simple113BlobTreeConfig config, TreePos pos, int trunkHeight, String leaves) {
        this(config, pos, trunkHeight, strToLeafArray(leaves));
    }
    public Simple113BlobTree(Simple113BlobTreeConfig config, TreePos pos, int trunkHeight, int[] leaves) {
        this.config = config;
        this.pos = pos;
        this.treeTrunkHeight = trunkHeight;
        this.leaves = leaves;

        if (treeTrunkHeight < config.baseHeight || treeTrunkHeight > (config.baseHeight + config.randomHeight)) {
            throw new IllegalArgumentException("Invalid tree height given");
        }

        if (leaves.length != 12) {
            throw new IllegalArgumentException("Leaf count not equal to the number of mutable leafs");
        }
    }


    @Override
    public void generateTreeTest(LcgTester treeTest) {
        config.selector.generateSelector(treeTest);

        //Do tree height
        treeTest.nextInt(config.randomHeight).equalTo(treeTrunkHeight - config.baseHeight);
        //treeTest.advance();
        if (config.randomSecondHeight != 0) {
            throw new NotImplementedException("Adding 2 lcgs not implemented");
        }

        //Leaves generate bottom up in 1.13.2
        for(int leafExists : leaves) {
            if (leafExists == -1) {
                treeTest.advance();
            } else {
                treeTest.nextInt(2).equalTo(leafExists);
            }
        }
    }

    @Override
    public TreePos getTreePosition() {
        return pos;
    }

    //this.registerFeature(GenerationStep.Feature.VEGETAL_DECORATION, Biome.<RandomFeatureSelectorConfig, CountExtraChanceDecoratorConfig>method_8699(Feature.randomFeatureSelector, new RandomFeatureSelectorConfig(new Feature[] { Feature.birchTreeFeature, Feature.largeOakFeature }, new class_1000138[] { class_1000138.field_1001673, class_1000138.field_1001673 }, new float[] { 0.2f, 0.1f }, (Feature<FC>)Feature.oakTreeFeature, (FC)class_1000138.field_1001673), ForestBiome.field_1000798, new CountExtraChanceDecoratorConfig(10, 0.1f, 1)));
    public static final Simple113BlobTreeConfig FOREST_OAK_TREE = new Simple113BlobTreeConfig(tester -> {tester.nextFloat().greaterThanOrEqualTo(0.2F); tester.nextFloat().greaterThanOrEqualTo(0.1f);}, 4,3);
    public static final Simple113BlobTreeConfig FOREST_BIRCH_TREE = new Simple113BlobTreeConfig(tester -> {tester.nextFloat().lessThan(0.2F);}, 5,3);
    //public static final Simple113BlobTreeConfig TALL_FOREST_BIRCH_TREE = new Simple113BlobTreeConfig(tester -> {tester.nextFloat().lessThan(0.2F);}, 5,3);
    public static class Simple113BlobTreeConfig {
        ITreeTypeSelector selector;

        int baseHeight;
        int randomHeight;
        int randomSecondHeight;

        public Simple113BlobTreeConfig(ITreeTypeSelector selector, int baseHeight, int randomHeight) {
            this(selector, baseHeight, randomHeight, 0);
        }
        public Simple113BlobTreeConfig(ITreeTypeSelector selector, int baseHeight, int randomHeight, int randomSecondHeight) {
            this.selector = selector;
            this.baseHeight = baseHeight;
            this.randomHeight = randomHeight;
            this.randomSecondHeight = randomSecondHeight;
        }
    }
}
