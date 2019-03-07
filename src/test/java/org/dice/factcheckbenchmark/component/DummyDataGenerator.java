package org.dice.factcheckbenchmark.component;


import org.dice.factcheckbenchmark.BenchmarkConstants;
import org.hobbit.core.components.AbstractDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dice.factcheckbenchmark.benchmark.rdf.TripleExtractor;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static org.dice.factcheckbenchmark.BenchmarkConstants.API_DATA_DIR_PATH;
import static org.dice.factcheckbenchmark.BenchmarkConstants.API_JAR_NAME;

public class DummyDataGenerator extends AbstractDataGenerator {
    private static final Logger logger = LoggerFactory.getLogger(org.dice.factcheckbenchmark.benchmark.DataGenerator.class);
    private String FACTBENCH_DIRECTORY;

    @Override
    public void init() throws Exception {
        // Always init the super class first!
        super.init();
        logger.debug("Init()");

        //Extract data set configuration from environment variables
        final String DATA_GENERATOR_DIR = Paths.get("data/factbench").toFile().getAbsolutePath() + File.separator;
        String factcbenchPath = System.getenv(BenchmarkConstants.ENV_FACTBENCH_DATA_SET);

        factcbenchPath = (factcbenchPath.substring(factcbenchPath.lastIndexOf("/") + 1)).replace("-", "/");
        FACTBENCH_DIRECTORY = DATA_GENERATOR_DIR + factcbenchPath + File.separator;
        logger.info("FactBench directory {}", FACTBENCH_DIRECTORY);
        // Your initialization code comes here...
    }

    @Override
    protected void generateData() throws Exception {
        // Create your data inside this method. You might want to use the
        // id of this data generator [getGeneratorId()] and the number of all data generators [getNumberOfGenerators()]
        // running in parallel.
        logger.debug("generateData()");
        sendDataFromDirectory(Paths.get(FACTBENCH_DIRECTORY));
    }

    private void sendDataFromDirectory(final Path factbenchPath) throws IOException {
        if (Files.isDirectory(factbenchPath)) {
            Files.walkFileTree(factbenchPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {

                    if (filePath.toString().endsWith("ttl")) {

                        String fileIdentifier = setFileIdentifier(
                                String.valueOf(factbenchPath.toAbsolutePath()).replace("/", "\\"),
                                String.valueOf(filePath.toAbsolutePath()));

                        TripleExtractor tripleExtractor = new TripleExtractor(String.valueOf(filePath.toAbsolutePath()));
                        String data = String.format("%s:*:%s", fileIdentifier, tripleExtractor.getSimplifiedData());

                        logger.debug("sendDataToTaskGenerator({})", filePath.getFileName().toString());
                        sendDataToTaskGenerator(data.getBytes());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            logger.debug("No files found in {}", factbenchPath);
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