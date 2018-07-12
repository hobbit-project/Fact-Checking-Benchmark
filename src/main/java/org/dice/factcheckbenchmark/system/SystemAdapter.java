package org.dice.factcheckbenchmark.system;

import org.dice.factcheckbenchmark.BenchmarkConstants;
import org.dice.factcheckbenchmark.system.api.Client;
import org.hobbit.core.Constants;
import org.hobbit.core.components.AbstractSystemAdapter;
import org.hobbit.sdk.JenaKeyValue;
import org.dice.factcheckbenchmark.system.api.FactCheckHobbitResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;


public class SystemAdapter extends AbstractSystemAdapter {
    private static final Logger logger = LoggerFactory.getLogger(SystemAdapter.class);
    private static JenaKeyValue parameters;
    private String databaseContainer;
    private String factcheckContainer;
    private String factcheckContainerUrl;

    @Override
    public void init() throws Exception {
        super.init();
        logger.debug("Init()");
        // Your initialization code comes here...

        parameters = new JenaKeyValue.Builder().buildFrom(systemParamModel);
        logger.debug("SystemModel: " + parameters.encodeToString());
        // You can access the RDF model this.systemParamModel to retrieve meta data about this system adapter

        //Create factcheck-database container
        databaseContainer = createContainer(BenchmarkConstants.FACTCHECK_DATABASE_IMAGE_NAME, Constants.CONTAINER_TYPE_DATABASE,
                new String[]{});

        if (databaseContainer.isEmpty()) {
            logger.debug("Error while creating database container {}", databaseContainer);
            throw new Exception("Database container not created");
        } else
            logger.debug("Database container created {}", databaseContainer);

        //Create factcheck-service container
        factcheckContainer = createContainer(BenchmarkConstants.FACTCHECK_SERVICE_IMAGE_NAME, Constants.CONTAINER_TYPE_SYSTEM,
                new String[]{});

        if (factcheckContainer.isEmpty()) {
            logger.debug("Error while creating factcheck-service container {}", factcheckContainer);
            throw new Exception("Service container not created");
        } else {
            logger.debug("factcheck-service container created {}", factcheckContainer);
            factcheckContainerUrl = "http://" + factcheckContainer + ":8080/api/hobbitTask/";

            logger.debug("factcheck-service container accessible from {}", factcheckContainerUrl);
        }
    }

    @Override
    public void receiveGeneratedData(byte[] data) {
        // handle the incoming data as described in the benchmark description
        String dataStr = new String(data);
        logger.trace("receiveGeneratedData(" + new String(data) + "): " + dataStr);

    }

    @Override
    public void receiveGeneratedTask(String taskId, byte[] data) {
        // handle the incoming task and create a result
        logger.debug("receiveGeneratedTask({})->{}", taskId, new String(data));

        final String REGEX_SEPARATOR = ":\\*:";
        String[] split = taskId.split(REGEX_SEPARATOR);
        String urlTaskId = split[0];
        String fileTrace = split[1];

        //String url = "http://localhost:8080/api/hobbitTask/" + urlTaskId;
        String url = factcheckContainerUrl + urlTaskId;
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("dataISWC", data);
        map.add("fileTrace", fileTrace);

        Client client = new Client(map, MediaType.MULTIPART_FORM_DATA, url);
        ResponseEntity<FactCheckHobbitResponse> response = client.getResponse(HttpMethod.POST);

        if (response.getStatusCode().equals(HttpStatus.OK)) {

            FactCheckHobbitResponse result = response.getBody();

            try {
                logger.debug("sendResultToEvalStorage({})->{}", taskId, result.getTruthValue());
                sendResultToEvalStorage(taskId, String.valueOf(result.getTruthValue()).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            try {

                logger.error("{} received for Task {}", response.getStatusCode(), taskId);
                sendResultToEvalStorage(taskId, String.valueOf(0.0).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public void close() throws IOException {
        // Free the resources you requested here
        logger.debug("close()");

        if (!factcheckContainer.isEmpty())
            stopContainer(factcheckContainer);

        if (!databaseContainer.isEmpty())
            stopContainer(databaseContainer);
        // Always close the super class after yours!
        super.close();
    }

}

