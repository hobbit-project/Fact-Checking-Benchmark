package org.hobbit.sdk.examples.examplebenchmark.system.container;

import org.hobbit.sdk.docker.BuildBasedDockerizer;
import org.hobbit.sdk.docker.builders.BuildBasedDockersBuilder;

import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FactcheckDockersBuilder extends BuildBasedDockersBuilder {

    private Path dockerWorkDir;
    private Path jarFilePath ;

    public FactcheckDockersBuilder(String dockerizerName) {
        super(dockerizerName);


        imageName("factcheck-api");
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
       /* if (this.dockerWorkDir == null) {
            throw new Exception("WorkingDirName class is not specified for " + this.getClass().getSimpleName());
        } else if (this.jarFilePath == null) {
            throw new Exception("JarFileName class is not specified for " + this.getClass().getSimpleName());
        } else if (!this.jarFilePath.toFile().exists()) {
            throw new Exception(this.jarFilePath + " not found. May be you did not packaged it by 'mvn package -DskipTests=true' first");
        } else {
*/
            String content;

            content = "FROM openjdk:8-jdk-alpine\n" +
                    "VOLUME /tmp\n" +
                    "ARG JAR_FILE\n" +
                    "ADD /data/factcheck-api-0.1.0.jar app.jar\n" +
                    "ENTRYPOINT [\"java\",\"-Djava.security.egd=file:/dev/./urandom\",\"-jar\",\"/app.jar\"]\n";

            this.dockerFileReader(new StringReader(content));
            return this;
     //   }
    }

    public BuildBasedDockerizer build() throws Exception {
        if (this.getDockerFileReader() == null) {
            this.initFileReader();
        }

        return super.build();
    }

}
