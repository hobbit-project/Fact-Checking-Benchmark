package org.hobbit.sdk.examples.examplebenchmark.benchmark;

import config.FactbenchConfig;
import org.hobbit.core.components.AbstractDataGenerator;
import org.ini4j.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rdf.TripleExtractor;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class DataGenerator extends AbstractDataGenerator {
    private static final Logger logger = LoggerFactory.getLogger(DataGenerator.class);
    private FactbenchConfig FACTBENCH_CONFIG;

    @Override
    public void init() throws Exception {
        // Always init the super class first!
        super.init();
        logger.debug("Init()");

        // Your initialization code comes here...
        if ( FACTBENCH_CONFIG  == null )
            FACTBENCH_CONFIG = new FactbenchConfig(new Ini(new File("/usr/src/factcheck-benchmark/data/factbench.ini")));
           // FACTBENCH_CONFIG = new FactbenchConfig(new Ini(new File(DataGenerator.class.getResource("/factbench.ini").getFile())));
    }

    @Override
    protected void generateData() throws Exception {
        // Create your data inside this method. You might want to use the
        // id of this data generator [getGeneratorId()] and the number of all data generators [getNumberOfGenerators()]
        // running in parallel.
        logger.debug("generateData()");
        logger.info("crawling across directory: " + FACTBENCH_CONFIG.getTestDirectory());
        sendDataFromDirectory(Paths.get(FACTBENCH_CONFIG.getTestDirectory()));
    }

    private void sendDataFromDirectory(final Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String directory = setFileIdentifier(
                            String.valueOf(path.toAbsolutePath()).replace("/", "\\"),
                            String.valueOf(file.toAbsolutePath()));

                    TripleExtractor tripleExtractor = new TripleExtractor(String.valueOf(file.toAbsolutePath()));
                    String data = String.format("%s:*:%s", directory, tripleExtractor.getSimplifiedData());

                    logger.info("sending data to task generator.");
                    sendDataToTaskGenerator(data.getBytes());

                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            // no directory; single file
            System.out.println(path.getFileName().toString());
        }
    }

    private String setFileIdentifier(String rootDir, String directory) {
        directory = directory
                .replace(rootDir, "")
                .replace(".ttl", "")
                .replace("\\", "-");
        return directory.substring(1);
    }

    @Override
    public void close() throws IOException {
        // Free the resources you requested here
        logger.debug("close()");
        // Always close the super class after yours!
        super.close();
    }
}