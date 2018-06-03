package org.dice.factcheckbenchmark;


/**
 * @author Pavel Smirnov
 */

public class Constants {

    public static String GIT_REPO_PATH = "git.project-hobbit.eu:4567/oshando/";
    //public static String GIT_REPO_PATH = "";
    //public static String PROJECT_NAME = "sdk-example-benchmark/";
    public static String PROJECT_NAME = "factcheck-benchmark/";

    //use these constants within BenchmarkController
    public static final String BENCHMARK_IMAGE_NAME = GIT_REPO_PATH + PROJECT_NAME + "benchmark-controller";
    public static final String DATAGEN_IMAGE_NAME = GIT_REPO_PATH + PROJECT_NAME + "datagen";
    public static final String TASKGEN_IMAGE_NAME = GIT_REPO_PATH + PROJECT_NAME + "taskgen";
    public static final String EVAL_STORAGE_IMAGE_NAME = GIT_REPO_PATH + PROJECT_NAME + "eval-storage";
    public static final String EVALMODULE_IMAGE_NAME = GIT_REPO_PATH + PROJECT_NAME + "eval-module";
    public static final String SYSTEM_IMAGE_NAME = GIT_REPO_PATH + PROJECT_NAME + "system-adapter";

    public static final String BENCHMARK_URI = "http://project-hobbit.eu/" + PROJECT_NAME;
    public static final String SYSTEM_URI = "http://project-hobbit.eu/" + PROJECT_NAME + "system";

    public static final String SDK_BUILD_DIR_PATH = ".";  //build directory, temp docker file will be created there
    public static final String SDK_JAR_FILE_PATH = "target/factcheck-benchmark-1.0.1.jar"; //should be packaged will all dependencies (via 'mvn package -DskipTests=true' command)
    public static final String SDK_WORK_DIR_PATH = "/usr/src/" + PROJECT_NAME;

    public static final String ENV_KPI_RECALL = "EVALUATION_RECALL";
    public static final String ENV_KPI_PRECISION = "EVALUATION_PRECISION";
    public static final String ENV_KPI_ACCURACY = "EVALUATION_ACCURACY";
    public static final String ENV_KPI_ROC_AUC = "EVALUATION_ROC_AUC";
    public static final String ENV_KPI_EVALUATION_TIME = "EVALUATION_TIME";

    //Evaluation Module URIs and environment variables
    public static final String URI_KPI_RECALL = BENCHMARK_URI + "recall";
    public static final String URI_KPI_PRECISION = BENCHMARK_URI + "precision";
    public static final String URI_KPI_ACCURACY = BENCHMARK_URI + "accuracy";
    public static final String URI_KPI_ROC_AUC = BENCHMARK_URI + "aucRoc";
    public static final String URI_KPI_EVALUATION_TIME = BENCHMARK_URI + "runTime";


    public static final String ENV_FACTBENCH_DATA_SET = "FACTBENCH_DATA_SET";
    public static final String URI_FACTBENCH_DATA_SET = BENCHMARK_URI + "dataSet";

}
