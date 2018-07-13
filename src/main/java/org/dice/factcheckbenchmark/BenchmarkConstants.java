package org.dice.factcheckbenchmark;


/**
 * @author Pavel Smirnov
 */

public class BenchmarkConstants {

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

    public static final String FACTCHECK_SERVICE_IMAGE_NAME = GIT_REPO_PATH + PROJECT_NAME + "factcheck-service";
    public static final String FACTCHECK_DATABASE_IMAGE_NAME = GIT_REPO_PATH + PROJECT_NAME + "factcheck-database";

    public static final String BENCHMARK_URI = "http://dice.cs.uni-paderborn.de/" + PROJECT_NAME;
    public static final String SYSTEM_URI = "http://dice.cs.uni-paderborn.de/" + PROJECT_NAME + "system";

    public static final String SDK_BUILD_DIR_PATH = ".";  //build directory, temp docker file will be created there
    public static final String SDK_JAR_FILE_PATH = "target/factcheck-benchmark-1.0.3.jar"; //should be packaged will all dependencies (via 'mvn package -DskipTests=true' command)
    public static final String SDK_WORK_DIR_PATH = "/usr/src/" + PROJECT_NAME;

    //Directory path and jar file name for factcheck-api container
    public static final String API_DATA_DIR_PATH = "factcheck-data/service/";
    public static final String API_JAR_NAME = "factcheck-service-1.0.3.jar";

    //Directory path and database file name for the factcheck-db container
    public static final String DB_DATA_DIR_PATH = "factcheck-data/db/";
    public static final String DB_FILE_NAME = "dbpedia_metrics.sql";

    public static final String ENV_KPI_RECALL = "EVALUATION_RECALL";
    public static final String ENV_KPI_PRECISION = "EVALUATION_PRECISION";
    public static final String ENV_KPI_ACCURACY = "EVALUATION_ACCURACY";
    public static final String ENV_KPI_AUC = "EVALUATION_AUC";
    public static final String ENV_KPI_ROC = "EVALUATION_ROC";
    public static final String ENV_KPI_EVALUATION_TIME = "EVALUATION_TIME";

    //Evaluation Module URIs and environment variables
    public static final String URI_KPI_RECALL = BENCHMARK_URI + "recall";
    public static final String URI_KPI_PRECISION = BENCHMARK_URI + "precision";
    public static final String URI_KPI_ACCURACY = BENCHMARK_URI + "accuracy";
    public static final String URI_KPI_ROC = BENCHMARK_URI + "roc";
    public static final String URI_KPI_AUC = BENCHMARK_URI + "auc";
    public static final String URI_KPI_EVALUATION_TIME = BENCHMARK_URI + "runTime";

    public static final String ENV_FACTBENCH_DATA_SET = "FACTBENCH_DATA_SET";
    public static final String URI_FACTBENCH_DATA_SET = BENCHMARK_URI + "dataSet";

    public static final String ENV_FACTCHECK_THRESHOLD = "FACTCHECK_THRESHOLD";
    public static final String URI_FACTCHECK_THRESHOLD = BENCHMARK_URI + "evaluationThreshold";

}
