package org.hobbit.sdk.examples.examplebenchmark.system;

import org.hobbit.core.components.AbstractSystemAdapter;
import org.hobbit.sdk.JenaKeyValue;
import org.hobbit.sdk.examples.examplebenchmark.system.api.Client;
import org.hobbit.sdk.examples.examplebenchmark.system.api.FactCheckHobbitResponse;
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

    @Override
    public void init() throws Exception {
        super.init();
        logger.debug("Init()");
        // Your initialization code comes here...

        parameters = new JenaKeyValue.Builder().buildFrom(systemParamModel);
        logger.debug("SystemModel: " + parameters.encodeToString());

        /*/Create factcheck-database container
        databaseContainer = createContainer("git.project-hobbit.eu:4567/oshando/factcheck-benchmark/factcheck-mysql", org.hobbit.core.Constants.CONTAINER_TYPE_DATABASE,
                new String[]{"HOBBIT_CONTAINER_NAME=dbpedia"});

        if (databaseContainer.isEmpty()) {
            logger.debug("Error while creating database container {}", databaseContainer);
            throw new Exception("Database container not created");
        } else
            logger.debug("Database container created {}", databaseContainer);

        //Create factcheck-api container
        factcheckContainer = createContainer("git.project-hobbit.eu:4567/oshando/factcheck-benchmark/factcheck-api", Constants.CONTAINER_TYPE_SYSTEM,
                new String[]{
                        "HOBBIT_CONTAINER_NAME=dbpedia"});

        if (factcheckContainer.isEmpty()) {
            logger.debug("Error while creating API container {}", factcheckContainer);
            throw new Exception("API container not created");
        } else
            logger.debug("factcheck-api container created {}", factcheckContainer);
*/
        // You can access the RDF model this.systemParamModel to retrieve meta data about this system adapter
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

        String url = "http://localhost:8080/api/hobbitTask/" + urlTaskId;
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("dataISWC", data);
        map.add("fileTrace", fileTrace);

        Client client = new Client(map, MediaType.MULTIPART_FORM_DATA, url);
        ResponseEntity<FactCheckHobbitResponse> response = client.getResponse(HttpMethod.POST);
        FactCheckHobbitResponse result = response.getBody();

        //TODO use threshold to assign true or false value based on  score returned by FactCheck

        try {
            logger.debug("sendResultToEvalStorage({})->{}", taskId, result.getTruthValue());
            sendResultToEvalStorage(taskId, ("correct-"+String.valueOf(result.getTruthValue())).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
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

