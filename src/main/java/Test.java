import kaptainwutax.seedutils.lcg.LCG;
import kaptainwutax.seedutils.lcg.rand.JRand;
import kaptainwutax.seedutils.mc.ChunkRand;
import kaptainwutax.seedutils.mc.MCVersion;
import me.cortex.TreeCracker.LCG.DiscreteLog;

public class Test {
    public static void main(String[] args) {
        ChunkRand chunkRand = new ChunkRand();
        long seed = chunkRand.setPopulationSeed(1234,-34 * 16,43 * 16, MCVersion.v1_16_4);
        long decoSeed = chunkRand.setDecoratorSeed(seed, 1, 8, MCVersion.v1_16_4);

        JRand rand = JRand.ofScrambledSeed(decoSeed);
        System.out.println(rand.getSeed());



        System.out.println(DiscreteLog.distanceFromZero(LCG.JAVA.nextSeed(0)));
        System.out.println(kaptainwutax.seedutils.lcg.DiscreteLog.distanceFromZero(LCG.JAVA,LCG.JAVA.nextSeed(0)));
    }
}
