package org.hobbit.sdk.examples.examplebenchmark.benchmark;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.hobbit.core.components.AbstractDataGenerator;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataGenerator extends AbstractDataGenerator {
    private static final Logger logger = LoggerFactory.getLogger(DataGenerator.class);
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
        int dataGeneratorId = getGeneratorId();
        int numberOfGenerators = getNumberOfGenerators();

        logger.debug("generateData()");

        logger.info("Loading models");
        Map<String, ArrayList<Model>> factBenchModels = readFiles(factBenchPath);

        // Sending Data
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

}