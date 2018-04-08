package org.hobbit.sdk.examples.examplebenchmark.benchmark;

import config.IniConfig;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.hobbit.core.components.AbstractDataGenerator;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rdf.TripleExtractor;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DataGenerator extends AbstractDataGenerator {
    private static final Logger logger = LoggerFactory.getLogger(DataGenerator.class);
    //TODO files do not load when leaf directory is provided: src/main/resources/factbench/test/correct/award
    private final String factBenchPath = "src/main/resources/factbench/test/correct";

    @Override
    public void init() throws Exception {
        // Always init the super class first!
        super.init();
        logger.debug("Init()");
        // Your initialization code comes here...
    }

    @Override
    protected void generateData() throws Exception {
        // Create your data inside this method. You might want to use the
        // id of this data generator and the number of all data generators
        // running in parallel.
        logger.debug("generateData()");

        int dataGeneratorId = getGeneratorId();
        int numberOfGenerators = getNumberOfGenerators();

        logger.info("Loading models");
        Map<String, ArrayList<Model>> factBenchModels = readFiles(factBenchPath);

        logger.info("Sending Models to TaskGenerator");
        //For each model, send it's data and expected result to the TaskGenerator
        for (Map.Entry<String, ArrayList<Model>> entry : factBenchModels.entrySet()) {

            entry.getValue().forEach(model -> {
                try {
                    sendDataToTaskGenerator(modelToBytes(model, entry.getKey()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private Map<String, ArrayList<Model>> readFiles(String directoryPath) {

        Map<String, ArrayList<Model>> map = new HashMap<String, ArrayList<Model>>();

        map = walk(map, directoryPath);
        return map;
    }

    private static Map<String, byte[]> crawlDatasetDirectory(final Path path) throws IOException {
        Map<String, byte[]> datasetBytesMap = new HashMap<>();
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String rootDir = String.valueOf(path.toAbsolutePath()).replace("/", "\\");
                    String directory = String.valueOf(file.toAbsolutePath())
                            .replace(rootDir, "")
                            .replace(".ttl", "")
                            .replace("\\", "-");
                    directory = directory.substring(1);

                    datasetBytesMap.put(directory, fileToBytes(String.valueOf(file.toAbsolutePath())));
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            // no directory; single file
            System.out.println(path.getFileName().toString());
        }
        return datasetBytesMap;
    }

    public static byte[] fileToBytes(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }

    public Map<String, ArrayList<Model>> walk(Map map, String path) {

        File root = new File(path);
        File[] list = root.listFiles();

        if (list == null)
            return null;

        for (File f : list) {
            String filePath = f.getAbsolutePath();

            if (f.isDirectory()) {
                if (!map.containsKey(filePath)) {
                    map.put(filePath, new ArrayList<Model>());
                }
                walk(map, f.getAbsolutePath());
            } else {

                if (filePath.endsWith(".ttl")) {
                    ArrayList<Model> models = (ArrayList<Model>) map.get(f.getAbsoluteFile().getParentFile().getAbsolutePath());
                    try {
                        Model model = ModelFactory.createDefaultModel();
                        model.read(new FileReader(f), null, "TURTLE");
                        models.add(model);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return map;
    }

    //Converts model and expected answer to bytes
    protected byte[] modelToBytes(Model tripleModel, String modelPath) {

        String expectedAnswer = String.valueOf(modelPath.contains("correct"));

        StringWriter stringWriter = new StringWriter();
        tripleModel.write(stringWriter, "TURTLE");

        String dataString = expectedAnswer + ":*:" + stringWriter.toString();

        return dataString.getBytes();
    }
    @Override
    public void close() throws IOException {
        // Free the resources you requested here
        logger.debug("close()");
        // Always close the super class after yours!
        super.close();
    }

    public static void main(String[] args) {
        try {
            System.out.println(IniConfig.configInstance.testDirectory.replace("/", "\\"));
            Map<String, byte[]> datasetBytesMap = crawlDatasetDirectory(Paths.get(IniConfig.configInstance.testDirectory));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}