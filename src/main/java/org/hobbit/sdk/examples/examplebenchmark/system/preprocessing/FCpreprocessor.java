package org.hobbit.sdk.examples.examplebenchmark.system.preprocessing;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.jena.rdf.model.*;
import org.hobbit.sdk.examples.examplebenchmark.system.FactCheckHobbitResponse;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author DANISH AHMED on 4/15/2018
 */
public class FCpreprocessor implements Serializable {
    private String data;
    private String fileTrace;
    private DefactoModel defactoModel;
    private Model modelFC;
    private Model modelISWC;

    public FCpreprocessor(String data, String taskId, String fileTrace) {
        this.data = data;
        this.fileTrace = fileTrace;
        Model modelISWC = createModel(data);
        setModelISWC(modelISWC);
        init(modelISWC, taskId);
    }

    public String getData() {
        return this.data;
    }

    public String getFileTrace() {
        return fileTrace;
    }

    private void setModelISWC(Model model) {
        this.modelISWC = model;
    }

    private void setModelFC(Model model) {
        this.modelFC = model;
    }

    private Model createModel(String data) {
        Model model = ModelFactory.createDefaultModel();
        model.read(new ByteArrayInputStream(data.getBytes()), null, "TTL");
        return model;
    }

    private Resource getResource(Model model, String propertyUri) {
        Property property = ResourceFactory.createProperty(propertyUri);
        NodeIterator nodeIterator = model.listObjectsOfProperty(property);
        if (nodeIterator.hasNext()) {
            RDFNode rdfNode = nodeIterator.nextNode();
            return  rdfNode.asResource();
        }
        return null;
    }

    private Literal getLabel(Model model, Resource resource) {
        Property labelProperty = ResourceFactory.createProperty(Constants.RDF_SCHEMA_NAMESPACE + "label");
        NodeIterator nodeIterator = model.listObjectsOfProperty(resource, labelProperty);
        if (nodeIterator.hasNext()) {
            RDFNode rdfNode = nodeIterator.nextNode();
            return rdfNode.asLiteral();
        }
        return null;
    }

    private void setDefactoModel(Model model, DefactoResource subject, DefactoResource object, RDFNode pred, String taskId) {
        Property predicate = ResourceFactory.createProperty(pred.toString());
        this.defactoModel = new DefactoModel(model, subject, object, predicate, taskId);
    }

    public DefactoModel getDefactoModel() {
        return this.defactoModel;
    }

    public Model getModelISWC() {
        return this.modelISWC;
    }

    public Model getModelFC() {
        return this.modelFC;
    }

    private DefactoResource setDefactoResource (Model modelISWC, Model modelFC, Resource resourceNode) {
        DefactoResource defactoResource = new DefactoResource(resourceNode, modelFC, resourceNode.getURI());
        Literal literal = getLabel(modelISWC, resourceNode);
        defactoResource.labels.put(literal.getLanguage(), literal.getLexicalForm());

        return defactoResource;
    }

    private void init(Model modelISWC, String taskId) {
        Resource subNode = getResource(modelISWC, Constants.RDF_SYNTAX_NAMESPACE + "subject");
        RDFNode predNode = getResource(modelISWC, Constants.RDF_SYNTAX_NAMESPACE + "predicate");
        Resource objNode = getResource(modelISWC, Constants.RDF_SYNTAX_NAMESPACE + "object");

        String dataFC = String.format("<%s> <%s> <%s> .", subNode, predNode, objNode);
        Model model = createModel(dataFC);
        setModelFC(model);

        DefactoResource subject = setDefactoResource(modelISWC, model, subNode);
        DefactoResource object = setDefactoResource(modelISWC, model, objNode);

        setDefactoModel(model, subject, object, predNode, taskId);
    }

    public static void main(String[] args) throws IOException {
        String taskId = "task1";
        String fileTrace = "any/path/";
        String dataISWC = "" +
                "<http://swc2017.aksw.org/task/dataset/s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> .\n" +
                "<http://swc2017.aksw.org/task/dataset/s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://dbpedia.org/resource/Albert_Einstein> .\n" +
                "<http://swc2017.aksw.org/task/dataset/s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <http://dbpedia.org/ontology/award> .\n" +
                "<http://swc2017.aksw.org/task/dataset/s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> <http://dbpedia.org/resource/Nobel_Prize_in_Physics> .\n" +
                "<http://dbpedia.org/resource/Albert_Einstein> <http://www.w3.org/2000/01/rdf-schema#label> \"Albert Einstein\"@en .\n" +
                "<http://dbpedia.org/resource/Nobel_Prize_in_Physics> <http://www.w3.org/2000/01/rdf-schema#label> \"Nobel Prize in Physics\"@en .";

        /*FCpreprocessor fCpreprocessor = new FCpreprocessor(dataISWC, taskId, fileTrace);
//        byte[] data = SerializationUtils.serialize(fCpreprocessor);

        System.out.println(fCpreprocessor.getData());

        RestTemplate rest = new RestTemplate();
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("taskId", taskId);
//        map.add("data", data);
        map.add("fCpreprocessor", fCpreprocessor);


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<MultiValueMap<String, Object>>(map, headers);

        ResponseEntity<FactCheckHobbitResponse> response =
                rest.exchange("http://localhost:8080/api/hobbitTask/",
                        HttpMethod.POST, request, FactCheckHobbitResponse.class);

        FactCheckHobbitResponse result = response.getBody();

        System.out.println("Truth value:  " + result.getTruthValue());*/

        /*FCpreprocessor fCpreprocessor = new FCpreprocessor(dataISWC, taskId, fileTrace);

        Map<String, String> vars = new HashMap<String, String>();
        vars.put("taskId", taskId);

        String uri = "http://localhost:8080/api/benchmarkTask/{taskId}";

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.writeValueAsBytes(fCpreprocessor);

        RestTemplate rt = new RestTemplate();
        rt.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        rt.getMessageConverters().add(new StringHttpMessageConverter());

//        FactCheckHobbitResponse returns = rt.postForObject(uri, fcPreprocessor, FactCheckHobbitResponse.class, vars);*/


        RestTemplate rest = new RestTemplate();
        FCpreprocessor fCpreprocessor = new FCpreprocessor(dataISWC, taskId, fileTrace);
        HttpEntity<byte[]> entity = new HttpEntity<>(SerializationUtils.serialize(fCpreprocessor));
        rest.postForEntity("http://localhost:8080/api/hobbitTask/{taskId}", entity, String.class);

    }
}
