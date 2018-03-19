package org.hobbit.sdk.examples.examplebenchmark.system;

import org.hobbit.core.components.AbstractSystemAdapter;
import org.hobbit.sdk.JenaKeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.concurrent.ThreadLocalRandom;
import java.io.IOException;


public class SystemAdapter extends AbstractSystemAdapter {
    private static final Logger logger = LoggerFactory.getLogger(SystemAdapter.class);
    private static JenaKeyValue parameters;


    @Override
    public void init() throws Exception {
        super.init();
        logger.debug("Init()");
        // Your initialization code comes here...

        parameters = new JenaKeyValue.Builder().buildFrom(systemParamModel);
        logger.debug("SystemModel: " + parameters.encodeToString());

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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("taskId", taskId);
        map.add("data", data);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<MultiValueMap<String, Object>>(map, headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<FCApi> response =
                restTemplate.exchange("http://127.0.0.1:8080/api/execTask/" + taskId,
                        HttpMethod.POST, request, FCApi.class);

        FCApi apiResult = response.getBody();
        //TODO send default exception values when no response is received

        try {
            logger.debug("sendResultToEvalStorage({})->{}", taskId, apiResult.getDefactoScore());
            sendResultToEvalStorage(taskId, String.valueOf(apiResult.getDefactoScore()).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

       /*int randomNum = ThreadLocalRandom.current().nextInt(0, 100 + 1);
        double confidence = randomNum * 0.01;
        String value = "";


            if(confidence>0.05)
                value = "false:*:"+String.valueOf(confidence);
            else
                value = "true:*:"+String.valueOf(confidence);

        try {
            logger.info("sendResultToEvalStorage({})->{}", taskId, value);
            sendResultToEvalStorage(taskId, value.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void close() throws IOException {
        // Free the resources you requested here
        logger.debug("close()");

        // Always close the super class after yours!
        super.close();
    }

}

