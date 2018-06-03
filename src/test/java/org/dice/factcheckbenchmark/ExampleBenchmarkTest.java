package org.dice.factcheckbenchmark;

import org.dice.factcheckbenchmark.benchmark.*;
import org.dice.factcheckbenchmark.system.SystemAdapter;
import org.hobbit.core.components.Component;
import org.hobbit.sdk.ComponentsExecutor;
import org.hobbit.sdk.EnvironmentVariablesWrapper;
import org.hobbit.sdk.JenaKeyValue;
import org.hobbit.sdk.docker.RabbitMqDockerizer;
import org.hobbit.sdk.docker.builders.hobbit.*;
import org.dice.factcheckbenchmark.system.container.FactcheckDockersBuilder;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.commandreactions.MultipleCommandsReaction;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;

import static org.hobbit.sdk.CommonConstants.*;

/**
 * @author Pavel Smirnov
 */

public class ExampleBenchmarkTest extends EnvironmentVariablesWrapper {

    private RabbitMqDockerizer rabbitMqDockerizer;
    private ComponentsExecutor componentsExecutor;
    private CommandQueueListener commandQueueListener;

    BenchmarkDockerBuilder benchmarkBuilder;
    DataGenDockerBuilder dataGeneratorBuilder;
    TaskGenDockerBuilder taskGeneratorBuilder;
    EvalStorageDockerBuilder evalStorageBuilder;
    SystemAdapterDockerBuilder systemAdapterBuilder;
    EvalModuleDockerBuilder evalModuleBuilder;
    // DatabaseDockersBuilder databaseBuilder;
    FactcheckDockersBuilder factcheckBuilder;


    public void init(Boolean useCachedImage) throws Exception {

        benchmarkBuilder = new BenchmarkDockerBuilder(new ExampleDockersBuilder(BenchmarkController.class, Constants.BENCHMARK_IMAGE_NAME).useCachedImage(useCachedImage));
        dataGeneratorBuilder = new DataGenDockerBuilder(new ExampleDockersBuilder(DataGenerator.class, Constants.DATAGEN_IMAGE_NAME).useCachedImage(useCachedImage).addFileOrFolder("data"));
        taskGeneratorBuilder = new TaskGenDockerBuilder(new ExampleDockersBuilder(TaskGenerator.class, Constants.TASKGEN_IMAGE_NAME).useCachedImage(useCachedImage));
        evalStorageBuilder = new EvalStorageDockerBuilder(new ExampleDockersBuilder(EvalStorage.class, Constants.EVAL_STORAGE_IMAGE_NAME).useCachedImage(useCachedImage));
        systemAdapterBuilder = new SystemAdapterDockerBuilder(new ExampleDockersBuilder(SystemAdapter.class, Constants.SYSTEM_IMAGE_NAME).useCachedImage(useCachedImage));
        evalModuleBuilder = new EvalModuleDockerBuilder(new ExampleDockersBuilder(EvalModule.class, Constants.EVALMODULE_IMAGE_NAME).useCachedImage(useCachedImage));
        // databaseBuilder = new DatabaseDockersBuilder("database-dockerizer");
        //factcheckBuilder = new FactcheckDockersBuilder("api-dockerizer");
    }


    @Test
    @Ignore
    public void buildImages() throws Exception {

        init(false);
        benchmarkBuilder.build().prepareImage();
        dataGeneratorBuilder.build().prepareImage();
        taskGeneratorBuilder.build().prepareImage();
        evalStorageBuilder.build().prepareImage();
        evalModuleBuilder.build().prepareImage();
        systemAdapterBuilder.build().prepareImage();
        //databaseBuilder.build().prepareImage();
        //factcheckBuilder.build().prepareImage();
    }

    @Test
    public void checkHealth() throws Exception {
        checkHealth(false);
    }

    @Test
    public void checkHealthDockerized() throws Exception {
        checkHealth(true);
    }

    private void checkHealth(Boolean dockerized) throws Exception {

        Boolean useCachedImages = true;
        init(useCachedImages);

        rabbitMqDockerizer = RabbitMqDockerizer.builder().build();

        setupCommunicationEnvironmentVariables(rabbitMqDockerizer.getHostName(), "session_" + String.valueOf(new Date().getTime()));
        setupBenchmarkEnvironmentVariables(EXPERIMENT_URI, createBenchmarkParameters());
        setupGeneratorEnvironmentVariables(1, 1);
        setupSystemEnvironmentVariables(Constants.SYSTEM_URI, createSystemParameters());


        Component benchmarkController = new BenchmarkController();
        Component dataGen = new DataGenerator();
        Component taskGen = new TaskGenerator();
        Component evalStorage = new EvalStorage();
        Component systemAdapter = new SystemAdapter();
        Component evalModule = new EvalModule();
        //  Component database = databaseBuilder.build();
        // Component factcheck = factcheckBuilder.build();

        if (dockerized) {

            benchmarkController = benchmarkBuilder.build();
            dataGen = dataGeneratorBuilder.build();
            taskGen = taskGeneratorBuilder.build();
            evalStorage = evalStorageBuilder.build();
            evalModule = evalModuleBuilder.build();
            systemAdapter = systemAdapterBuilder.build();
        }

        commandQueueListener = new CommandQueueListener();
        componentsExecutor = new ComponentsExecutor(commandQueueListener, environmentVariables);

        rabbitMqDockerizer.run();

        commandQueueListener.setCommandReactions(
                new MultipleCommandsReaction(componentsExecutor, commandQueueListener)
                        .dataGenerator(dataGen).dataGeneratorImageName(dataGeneratorBuilder.getImageName())
                        .taskGenerator(taskGen).taskGeneratorImageName(taskGeneratorBuilder.getImageName())
                        .evalStorage(evalStorage).evalStorageImageName(evalStorageBuilder.getImageName())
                        //.database(database).databaseImageName(databaseBuilder.getImageName())
                        // .factcheck(factcheck).factcheckImageName(factcheckBuilder.getImageName())
                        .evalModule(evalModule).evalModuleImageName(evalModuleBuilder.getImageName())
                        .systemContainerId(systemAdapterBuilder.getImageName())
        );

        componentsExecutor.submit(commandQueueListener);
        commandQueueListener.waitForInitialisation();

        //Add images to components executor
        //componentsExecutor.submit(database, databaseBuilder.getImageName());
        //componentsExecutor.submit(factcheck, factcheckBuilder.getImageName());
        componentsExecutor.submit(benchmarkController);
        componentsExecutor.submit(systemAdapter, systemAdapterBuilder.getImageName());

        commandQueueListener.waitForTermination();

        rabbitMqDockerizer.stop();

        Assert.assertFalse(componentsExecutor.anyExceptions());
    }


    public JenaKeyValue createBenchmarkParameters() {
        JenaKeyValue kv = new JenaKeyValue();
        //kv.setValue(BENCHMARK_MODE_INPUT_NAME, BENCHMARK_MODE_DYNAMIC+":10:1");
        return kv;
    }

    private static JenaKeyValue createSystemParameters() {
        JenaKeyValue kv = new JenaKeyValue();
        //kv.setValue(BENCHMARK_MODE_INPUT_NAME, BENCHMARK_MODE_DYNAMIC+":10:1");
        return kv;
    }


}
