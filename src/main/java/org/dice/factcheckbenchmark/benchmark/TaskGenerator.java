package org.dice.factcheckbenchmark.benchmark;

import org.dice.factcheckbenchmark.BenchmarkConstants;
import org.hobbit.core.components.AbstractSequencingTaskGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class TaskGenerator extends AbstractSequencingTaskGenerator {
    private static final Logger logger = LoggerFactory.getLogger(TaskGenerator.class);

    @Override
    public void init() throws Exception {
        // Always init the super class first!
        super.init();
        logger.debug("Init()");
        setAckTimeout(150000L);
        // Your initialization code comes here...
    }

    @Override
    protected void generateTask(byte[] data) throws Exception {

        // Create tasks based on the incoming data inside this method.
        // You might want to use the id of this task generator and the
        // number of all task generators running in parallel.
        logger.debug("generateTask()");

        //TODO Research how these data members can be used
        int dataGeneratorId = getGeneratorId();
        int numberOfGenerators = getNumberOfGenerators();

        // Create an ID for the task
        String taskId = getNextTaskId();

        //Split data using separator to extract query and expected
        final String REGEX_SEPARATOR = ":\\*:";
        String[] dataString = new String(data).split(REGEX_SEPARATOR);
        dataString[1] = dataString[1].replaceAll("task/dataset/s", taskId + "/dataset/" + System.getenv(BenchmarkConstants.ENV_FACTBENCH_DATA_SET));

        // Send the task to the system (and store the timestamp)
        long timestamp = System.currentTimeMillis();

        logger.debug("sendTaskToSystemAdapter({})->{}", taskId, dataString[1]);
        sendTaskToSystemAdapter(taskId, dataString[1].getBytes());

        // Send the expected answer to the evaluation store
        logger.debug("sendTaskToEvalStorage({})->{}", taskId, dataString[0]);
        sendTaskToEvalStorage(taskId, timestamp, dataString[0].getBytes());
    }

    @Override
    public void close() throws IOException {
        // Free the resources you requested here
        logger.debug("close()");
        // Always close the super class after yours!
        super.close();
    }

}