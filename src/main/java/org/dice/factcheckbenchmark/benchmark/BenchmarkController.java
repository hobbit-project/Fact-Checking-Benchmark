package org.dice.factcheckbenchmark.benchmark;

import org.apache.commons.lang.ArrayUtils;
import org.apache.jena.rdf.model.NodeIterator;
import org.dice.factcheckbenchmark.BenchmarkConstants;
import org.hobbit.core.Commands;
import org.hobbit.core.components.AbstractBenchmarkController;
import org.hobbit.sdk.JenaKeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class BenchmarkController extends AbstractBenchmarkController {
    private static final Logger logger = LoggerFactory.getLogger(BenchmarkController.class);
    private static JenaKeyValue parameters;
    private double FACTCHECK_THRESHOLD;

    @Override
    public void init() throws Exception {
        super.init();
        logger.debug("Init()");

        parameters = new JenaKeyValue.Builder().buildFrom(benchmarkParamModel);
        logger.debug("BenchmarkModel: " + parameters.encodeToString());
        // Your initialization code comes here...


        // You might want to load parameters from the benchmarks parameter model

        /*
            Obtain FactBench data set from param model
         */
        NodeIterator iterator = benchmarkParamModel.listObjectsOfProperty(benchmarkParamModel
                .getProperty(BenchmarkConstants.URI_FACTBENCH_DATA_SET));

        String dataSet = "http://project-hobbit.eu/factcheck-benchmark/evaluation";
        if (iterator.hasNext()) {
            try {

                dataSet = iterator.next().asResource().getURI();
                logger.debug("Dataset {} selected", dataSet);

            } catch (Exception e) {
                logger.debug("Couldn't get " + BenchmarkConstants.URI_FACTBENCH_DATA_SET + " parameter from parameter model.", e);
            }
        }

        /*
            Obtain threshold for Factcheck
         */
        iterator = benchmarkParamModel.listObjectsOfProperty(benchmarkParamModel
                .getProperty(BenchmarkConstants.URI_FACTCHECK_THRESHOLD));

        FACTCHECK_THRESHOLD = 0.2;
        if (iterator.hasNext()) {
            try {

                FACTCHECK_THRESHOLD = iterator.next().asLiteral().getDouble();
                logger.debug("Factcheck threshold {} set", FACTCHECK_THRESHOLD);

            } catch (Exception e) {
                logger.debug("Couldn't get " + BenchmarkConstants.URI_FACTCHECK_THRESHOLD + " parameter from model.", e);
            }
        }

        // Create the other components

        // Create data generators

        int numberOfDataGenerators = 1;
        String[] envVariables = new String[]{BenchmarkConstants.ENV_FACTBENCH_DATA_SET + "=" + dataSet};

        logger.debug("createDataGenerators()");
        createDataGenerators(BenchmarkConstants.DATAGEN_IMAGE_NAME, numberOfDataGenerators, envVariables);


        // Create task generators
        int numberOfTaskGenerators = 1;
        envVariables = new String[]{"key1=value1"};

        logger.debug("createTaskGenerators()");
        createTaskGenerators(BenchmarkConstants.TASKGEN_IMAGE_NAME, numberOfTaskGenerators, envVariables);

        // Create evaluation storage
        logger.debug("createEvaluationStorage()");
        //You can use standard evaluation storage (git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage)
        //createEvaluationStorage();
        //or simplified local-one from the SDK
        envVariables = (String[]) ArrayUtils.add(DEFAULT_EVAL_STORAGE_PARAMETERS, "HOBBIT_RABBIT_HOST=" + this.rabbitMQHostName);
        envVariables = (String[]) org.apache.commons.lang.ArrayUtils.add(envVariables, "ACKNOWLEDGEMENT_FLAG=true");
        this.createEvaluationStorage(BenchmarkConstants.EVAL_STORAGE_IMAGE_NAME, envVariables);

        // Wait for all components to finish their initialization
        waitForComponents();
    }

    private void waitForComponents() {
        logger.debug("waitForComponents()");
        //throw new NotImplementedException();
    }

    @Override
    protected void executeBenchmark() throws Exception {
        logger.debug("executeBenchmark(sending TASK_GENERATOR_START_SIGNAL & DATA_GENERATOR_START_SIGNAL)");
        // give the start signals
        sendToCmdQueue(Commands.TASK_GENERATOR_START_SIGNAL);
        sendToCmdQueue(Commands.DATA_GENERATOR_START_SIGNAL);

        // wait for the data generators to finish their work

        logger.debug("waitForDataGenToFinish() to send DATA_GENERATION_FINISHED_SIGNAL");
        waitForDataGenToFinish();

////
////        // wait for the task generators to finish their work

        logger.debug("waitForTaskGenToFinish() to finish to send TASK_GENERATION_FINISHED_SIGNAL");
        waitForTaskGenToFinish();

////
////        // wait for the system to terminate. Note that you can also use
////        // the method waitForSystemToFinish(maxTime) where maxTime is
////        // a long value defining the maximum amount of time the benchmark
////        // will wait for the system to terminate.
        //taskGenContainerIds.add("system");

        logger.debug("waitForSystemToFinish() to finish to send TASK_GENERATION_FINISHED_SIGNAL");
        waitForSystemToFinish();

        // Create the evaluation module

        String[] envVariables = new String[]{BenchmarkConstants.ENV_KPI_ACCURACY + "=" + BenchmarkConstants.URI_KPI_ACCURACY,
                BenchmarkConstants.ENV_KPI_ROC + "=" + BenchmarkConstants.URI_KPI_ROC,
                BenchmarkConstants.ENV_KPI_AUC + "=" + BenchmarkConstants.URI_KPI_AUC,
                BenchmarkConstants.ENV_KPI_EVALUATION_TIME + "=" + BenchmarkConstants.URI_KPI_EVALUATION_TIME,
                BenchmarkConstants.ENV_KPI_RECALL + "=" + BenchmarkConstants.URI_KPI_RECALL,
                BenchmarkConstants.ENV_KPI_PRECISION + "=" + BenchmarkConstants.URI_KPI_PRECISION,
                BenchmarkConstants.ENV_FACTCHECK_THRESHOLD + "=" + String.valueOf(FACTCHECK_THRESHOLD)};
        createEvaluationModule(BenchmarkConstants.EVALMODULE_IMAGE_NAME, envVariables);

        // wait for the evaluation to finish
        waitForEvalComponentsToFinish();

        // the evaluation module should have sent an RDF model containing the
        // results. We should add the configuration of the benchmark to this
        // model.
        // this.resultModel.add(...);

        // Send the resultModul to the platform controller and terminate
        sendResultModel(resultModel);
    }

    @Override
    public void close() throws IOException {
        logger.debug("close()");
        // Free the resources you requested here

        // Always close the super class after yours!
        super.close();
    }

}
