package org.hobbit.sdk.examples.examplebenchmark.system.container;

import org.hobbit.sdk.docker.BuildBasedDockerizer;
import org.hobbit.sdk.docker.builders.BuildBasedDockersBuilder;

import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DatabaseDockersBuilder extends BuildBasedDockersBuilder {

    private Class[] runnerClass;
    private Path dockerWorkDir;
    private Path jarFilePath;
    private List<String> filesToAdd = new ArrayList();

    public DatabaseDockersBuilder(String dockerizerName) {
        super(dockerizerName);
        imageName("factcheck-mysql");
        //name for searching in logs
        containerName("database-container");
        //temp docker file will be created there
     //   buildDirectory("/Users/oshando/Projects/IdeaProjects/factcheck-benchmark/src/main/java/org/hobbit/sdk/examples/examplebenchmark/system/");
buildDirectory(".");

/*
        addEnvironmentVariable("HOBBIT_RABBIT_HOST", (String)System.getenv().get("HOBBIT_RABBIT_HOST"));
        addEnvironmentVariable("HOBBIT_SESSION_ID", (String)System.getenv().get("HOBBIT_SESSION_ID"));
        addNetworks(CommonConstants.HOBBIT_NETWORKS);
        addEnvironmentVariable("SYSTEM_PARAMETERS_MODEL", (String)System.getenv().get("SYSTEM_PARAMETERS_MODEL"));
        addEnvironmentVariable("HOBBIT_CONTAINER_NAME", getContainerName());
*/
    }


    public DatabaseDockersBuilder runnerClass(Class... values) {
        this.runnerClass = values;
        return this;
    }

    public DatabaseDockersBuilder dockerWorkDir(String value) {
        this.dockerWorkDir = Paths.get(value);
        return this;
    }

    public DatabaseDockersBuilder jarFilePath(String value) {
        this.jarFilePath = Paths.get(value).toAbsolutePath();
        return this;
    }

    public DatabaseDockersBuilder useCachedImage(Boolean value) {
        super.useCachedImage(value);
        return this;
    }

    public DatabaseDockersBuilder useCachedContainer(Boolean value) {
        super.useCachedContainer(value);
        return this;
    }

    public DatabaseDockersBuilder customDockerFileReader(Reader value) {
        super.dockerFileReader(value);
        return this;
    }

    public DatabaseDockersBuilder imageName(String value) {
        super.imageName(value);
        return this;
    }

    public DatabaseDockersBuilder addFileOrFolder(String path) {
        this.filesToAdd.add(path);
        return this;
    }

    private DatabaseDockersBuilder initFileReader() throws Exception {

        //this.buildDirectory(".");
        String content = "FROM mysql:5.5\n" +
                "ENV MYSQL_ROOT_PASSWORD 12345\n" +
                "ENV MYSQL_DATABASE dbpedia_metrics\n" +
                "ADD data/dbpedia_metrics.sql /docker-entrypoint-initdb.d/dbpedia_metrics.sql\n";
        this.dockerFileReader(new StringReader(content));
        return this;

    }

    public BuildBasedDockerizer build() throws Exception {
        if (this.getDockerFileReader() == null) {
            this.initFileReader();
        }

        return super.build();
    }
}
