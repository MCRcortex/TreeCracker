package me.cortex.TreeCracker.program;

import me.cortex.TreeCracker.LCG.ConfiguredLcg;
import me.cortex.TreeCracker.LCG.LcgTester;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.stream.Collectors;

import static me.cortex.TreeCracker.LCG.LcgComparison.GetInvertedTypeStringOperator;

public class CudaProgram {
    private String currentSource;

    public void loadFromResources(String name) throws IOException {
        URL file = Thread.currentThread().getContextClassLoader().getResource(name);
        if (file == null)
            throw new IllegalStateException("Resource file not found");
        currentSource = IOUtils.toString(file, Charset.defaultCharset());
    }

    public void replaceFirstUsingKeyword(String keyword, String replaceWith) {
        if (!currentSource.contains(keyword))
            throw new IllegalStateException("Keyword not in source");
        currentSource = currentSource.replaceFirst(keyword, replaceWith);
    }

    public void replaceAllUsingKeyword(String keyword, String replaceWith) {
        if (!currentSource.contains(keyword))
            throw new IllegalStateException("Keyword not in source");
        currentSource = currentSource.replaceAll(keyword, replaceWith);
    }

    public String getSource() {
        return currentSource;
    }



    public void exportSource(File file) throws IOException {
        Files.write(file.toPath(), currentSource.getBytes());
    }

    /*
    public void exportSource() throws IOException {
        Files.write(file.toPath(), currentSource.getBytes());
    }
    */
}
