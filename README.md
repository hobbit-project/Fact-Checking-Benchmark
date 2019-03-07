# Factcheck Benchmark

This repository implements the components required by [Hobbit](https://github.com/hobbit-project) benchmarking platform to benchmark [FactCheck](https://github.com/dice-group/FactCheck) (an algorithm for validating statements by finding confirming sources for it on the web) using the models provided by [FactBench](https://github.com/SmartDataAnalytics/FactBench) (a multilingual benchmark for the evaluation of fact validation algorithms). Before running the benchmark, the FactBench data set(test or train) can be selected as well as the threshold that FactCheck should use. The benchmark provides the following metrics: recall, precision, accuracy, precision, algorithm's run time and ROC/AUC value.

## Running the Benchmark 
1) Follow the steps outlined for setting up the SDK [benchmark environment](https://github.com/hobbit-project/java-sdk-example#before-you-start).
2) Create `data/factbench` directory in the root of the project. Clone the the FactBench repository and copy the [test](https://github.com/SmartDataAnalytics/FactBench/tree/master/test) and [train](https://github.com/SmartDataAnalytics/FactBench/tree/master/train) directories to the `data/factbench`. 
3) Create `factcheck-data/db` directory and place the `dbpedia_metrics.sql` file from the [FactCheck](https://github.com/dice-group/FactCheck) repository in the directory. This file will be used to initialize the FactCheck database Docker container, when it is being created.
4) Create `factcheck-data/service` directory and ensure the following items are present:
   - the compiled `factcheck-service-*.jar` file from the [factcheck-service](https://github.com/dice-group/FactCheck/tree/master/factcheck-service) project.
   - the `/machinelearning` directory from the [FactCheck](https://github.com/dice-group/FactCheck) repository. The path would be `factcheck-data/service/machinelearning`. 
   - the `/wordnet` directory from the [FactCheck](https://github.com/dice-group/FactCheck) repository. The path would be `factcheck-data/service/wordnet`.
   - the `defacto.ini` file stores the configuration that the Docker container uses to initialize the application.
   - other resources that [factcheck-service](https://github.com/dice-group/FactCheck/tree/master/factcheck-service)  requires.
5) Update [BenchmarkConstants](https://github.com/oshando/Fact-Checking-Benchmark/blob/master/src/main/java/org/dice/factcheckbenchmark/BenchmarkConstants.java) with the correct image names and directories.
6) When debugging the benchmark, update [DummySystemAdapter](https://github.com/oshando/Fact-Checking-Benchmark/blob/master/src/test/java/org/dice/factcheckbenchmark/component/DummySystemAdapter.java) so that it can access the local instance of [factcheck-service](https://github.com/dice-group/FactCheck/tree/master/factcheck-service).

## Deploying the benchmark
1) Update [ExampleBenchmarkTest]() to use the deployment ready components instead of the dummy components. 
2) Run the `buildSystemImages()` to build the factcheck-service and factcheck-database containers.
2) Follow the [steps](https://github.com/hobbit-project/java-sdk-example#how-to-create-a-benchmark) to build the docker images that will be uploaded to the remote respositories. Please find the basic benchmark component implementations in the [sources folder](https://github.com/hobbit-project/java-sdk-example/tree/master/src/main/java/org/hobbit/sdk/examples/examplebenchmark/benchmark). You may extend the components with logic of your benchmark and debug the components as pure java codes by running the `checkHealth()` method from [ExampleBenchmarkTest](https://github.com/hobbit-project/java-sdk-example/blob/master/src/test/java/org/hobbit/sdk/examples/ExampleBenchmarkTest.java)). You may specify input parameters models for benchmark and system you are running.
3) Update the container names and then execute [push.sh](https://github.com/oshando/Fact-Checking-Benchmark/blob/master/push.sh), after logging into the repository, to push the images.