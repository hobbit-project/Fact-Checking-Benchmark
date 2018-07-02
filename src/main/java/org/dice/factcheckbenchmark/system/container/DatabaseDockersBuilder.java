package org.dice.factcheckbenchmark.system.container;

import org.dice.factcheckbenchmark.Constants;
import org.hobbit.sdk.docker.BuildBasedDockerizer;
import org.hobbit.sdk.docker.builders.BuildBasedDockersBuilder;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.dice.factcheckbenchmark.Constants.DB_DATA_DIR_PATH;
import static org.dice.factcheckbenchmark.Constants.DB_FILE_NAME;

public class DatabaseDockersBuilder extends BuildBasedDockersBuilder {

    private Path dockerWorkDir;

    public DatabaseDockersBuilder(String dockerizerName) {
        super(dockerizerName);

        imageName(Constants.GIT_REPO_PATH + Constants.PROJECT_NAME + "factcheck-mysql");
        //name for searching in logs
        containerName("database-container");
        //temp docker file will be created there
        buildDirectory(".");
    }

    public DatabaseDockersBuilder dockerWorkDir(String value) {
        this.dockerWorkDir = Paths.get(value);
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

    public DatabaseDockersBuilder imageName(String value) {
        super.imageName(value);
        return this;
    }


    private DatabaseDockersBuilder initFileReader() throws Exception {

        Path databaseFilePath = Paths.get(DB_DATA_DIR_PATH + DB_FILE_NAME);

        if (!databaseFilePath.toFile().exists()) {
            throw new Exception(databaseFilePath + " not found. Ensure database file is included in the directory.");
        } else {

            String content = "FROM mysql:5.5\n" +
                    "ENV MYSQL_ROOT_PASSWORD 12345\n" +
                    "ENV MYSQL_DATABASE dbpedia_metrics\n" +
                    "ADD " + databaseFilePath.toString() + " /docker-entrypoint-initdb.d/dbpedia_metrics.sql\n";
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
