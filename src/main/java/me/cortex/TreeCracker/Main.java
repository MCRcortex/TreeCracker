package me.cortex.TreeCracker;

import me.cortex.TreeCracker.LCG.LcgTester;
import me.cortex.TreeCracker.postProcessor.PostProcessor;
import me.cortex.TreeCracker.program.CudaProgram;
import me.cortex.TreeCracker.program.TreeCrackerProgram;
import me.cortex.TreeCracker.trees.ICrackableTree;
import me.cortex.TreeCracker.trees.Simple112BlobTree;
import me.cortex.TreeCracker.trees.Simple116BlobTree;
import me.cortex.TreeCracker.trees.TreePos;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws IOException {

        /*
        LcgTester tester = new LcgTester();
        CudaProgram cp = new CudaProgram();
        cp.lcgVariableName = "seed";
        cp.comparisonFailedReturn = "continue";

        tester.nextBoolean().equalTo(true);
        tester.nextFloat().greaterThan(0.543f);
        tester.nextInt(69).lessThan(1);
        tester.nextInt(69).lessThan(1);
        tester.nextInt(69).lessThan(1);

        System.out.println(tester.getFilterPower());
        System.out.println(cp.assembleLcgTester(tester));

        Random r = new Random(1358879 ^0x5DEECE66DL);
        System.out.println(r.nextBoolean());
        System.out.println(r.nextFloat()>0.543f);
        System.out.println(r.nextInt(69)<1);
        System.out.println(r.nextInt(69)<1);
        System.out.println(r.nextInt(69)<1);
        */



        TreeCrackerProgram program = new TreeCrackerProgram(
                new Simple116BlobTree(Simple116BlobTree.FOREST_OAK_TREE, new TreePos(0,13), 6, new int[]{1,0,0,1,  0,1,1,1, 1,1,1,1}),
                new Simple116BlobTree(Simple116BlobTree.FOREST_BIRCH_TREE, new TreePos(4,12), 5, new int[]{0,1,0,1,  0,-1,1,0, 1,0,0,0}),
                new Simple116BlobTree(Simple116BlobTree.FOREST_OAK_TREE, new TreePos(12,9), 6, new int[]{1,1,1,1,  1,1,1,0,  1,1,1,1})

        );

        program = new TreeCrackerProgram(
                new Simple112BlobTree(Simple112BlobTree.FOREST_OAK_TREE, new TreePos(7,11), 4, new int[]{-1,-1,1,-1, -1,-1,0,-1,  -1,-1,-1,-1}),
                new Simple112BlobTree(Simple112BlobTree.FOREST_OAK_TREE, new TreePos(13,10), 5, new int[]{0,-1,1,-1,  0,-1,1,-1, -1,-1,-1,-1}),

                new Simple112BlobTree(Simple112BlobTree.FOREST_BIRCH_TREE, new TreePos(13,7), 7, new int[]{1,-1,1,-1,  1,-1,0,-1,  -1,-1,1,-1}),


                new Simple112BlobTree(Simple112BlobTree.FOREST_BIRCH_TREE, new TreePos(9,5), 7, new int[]{0,-1,1,-1,  1,-1,1,-1,  1,-1,1,-1}),

                new Simple112BlobTree(Simple112BlobTree.FOREST_BIRCH_TREE, new TreePos(4,8), 5, new int[]{0,0,1,-1,  1,1,0,-1,  1,-1,1,-1})

        );

        program.generateCracker().exportSource(new File("./output/out.cu"));


    }
}
