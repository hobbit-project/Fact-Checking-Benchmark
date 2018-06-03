package org.dice.factcheckbenchmark.benchmark;

import org.dice.factcheckbenchmark.Constants;
import org.hobbit.core.components.AbstractDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dice.factcheckbenchmark.benchmark.rdf.TripleExtractor;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class DataGenerator extends AbstractDataGenerator {
    private static final Logger logger = LoggerFactory.getLogger(DataGenerator.class);
    private String FACTBENCH_DIRECTORY;

    @Override
    public void init() throws Exception {
        // Always init the super class first!
        super.init();
        logger.debug("Init()");

        //Extract data set configuration from environment variables
        //final String DATA_GENERATOR_DIR = "/Users/oshando/Projects/IdeaProjects/factcheck-benchmark/data/factbench/";
        final String DATA_GENERATOR_DIR = Constants.SDK_WORK_DIR_PATH + "data/";
        String factcbenchPath = System.getenv(Constants.ENV_FACTBENCH_DATA_SET);

        factcbenchPath = (factcbenchPath.substring(factcbenchPath.lastIndexOf("/") + 1)).replace("-", "/");
        FACTBENCH_DIRECTORY = DATA_GENERATOR_DIR + factcbenchPath + File.separator;
        logger.debug("FactBench directory {}", FACTBENCH_DIRECTORY);
        // Your initialization code comes here...
    }

    @Override
    protected void generateData() throws Exception {
        // Create your data inside this method. You might want to use the
        // id of this data generator [getGeneratorId()] and the number of all data generators [getNumberOfGenerators()]
        // running in parallel.
        logger.debug("generateData()");
        logger.info("Walking across {}", FACTBENCH_DIRECTORY);
        sendDataFromDirectory(Paths.get(FACTBENCH_DIRECTORY));
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

                    logger.debug("sendDataToTaskGenerator()");
                    sendDataToTaskGenerator(data.getBytes());

                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            logger.debug("No files found in {}", path);
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