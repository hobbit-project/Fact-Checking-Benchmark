package org.dice.factcheckbenchmark.system.container;

import org.dice.factcheckbenchmark.Constants;
import org.hobbit.sdk.docker.BuildBasedDockerizer;
import org.hobbit.sdk.docker.builders.BuildBasedDockersBuilder;

import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.dice.factcheckbenchmark.Constants.API_DATA_DIR_PATH;
import static org.dice.factcheckbenchmark.Constants.API_JAR_NAME;

public class FactcheckDockersBuilder extends BuildBasedDockersBuilder {

    private Path dockerWorkDir;
    private Path jarFilePath;

    public FactcheckDockersBuilder(String dockerizerName) {
        super(dockerizerName);

        imageName(Constants.GIT_REPO_PATH + Constants.PROJECT_NAME + "factcheck-api");
        //name for searching in logs
        containerName("factcheck-container");
        //temp docker file will be created there
        buildDirectory(".");
    }

    public FactcheckDockersBuilder dockerWorkDir(String value) {
        this.dockerWorkDir = Paths.get(value);
        return this;
    }

    public FactcheckDockersBuilder jarFilePath(String value) {
        this.jarFilePath = Paths.get(value).toAbsolutePath();
        return this;
    }


    public FactcheckDockersBuilder useCachedImage(Boolean value) {
        super.useCachedImage(value);
        return this;
    }

    public FactcheckDockersBuilder useCachedContainer(Boolean value) {
        super.useCachedContainer(value);
        return this;
    }

    public FactcheckDockersBuilder customDockerFileReader(Reader value) {
        super.dockerFileReader(value);
        return this;
    }

    public FactcheckDockersBuilder imageName(String value) {
        super.imageName(value);
        return this;
    }


    private FactcheckDockersBuilder initFileReader() throws Exception {

        Path jarFilePath = Paths.get(API_DATA_DIR_PATH + API_JAR_NAME);

        if (!jarFilePath.toFile().exists()) {
            throw new Exception(jarFilePath + " not found. Ensure that factcheck-api jar is in the right directory.");
        } else {

            String content = "FROM openjdk:8-jdk-alpine\n" +
                    "VOLUME /tmp\n" +
                    "ARG JAR_FILE\n" +
                    "ADD " + jarFilePath.toString() + " app.jar\n" +
                    "ADD " + API_DATA_DIR_PATH + "machinelearning /data/machinelearning\n" +
                    "ADD " + API_DATA_DIR_PATH + "wordnet /data/wordnet\n" +
                    "ADD " + API_DATA_DIR_PATH + "defacto.ini defacto.ini\n" +
                    "EXPOSE 8080\n" +
                    "ENTRYPOINT [\"java\",\"-Djava.security.egd=file:/dev/./urandom\",\"-jar\",\"/app.jar\"]\n";

            this.dockerFileReader(new StringReader(content));
            return this;
        }
    }

    public BuildBasedDockerizer build() throws Exception {
        if (this.getDockerFileReader() == null) {
            this.initFileReader();
        }

        return super.build();
    }

}
