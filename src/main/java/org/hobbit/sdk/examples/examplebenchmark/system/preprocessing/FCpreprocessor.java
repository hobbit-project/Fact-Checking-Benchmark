package org.hobbit.sdk.examples.examplebenchmark.system.preprocessing;

import org.apache.jena.rdf.model.*;
import rdf.Constants;

import java.io.ByteArrayInputStream;

/**
 * @author DANISH AHMED on 4/15/2018
 */
public class FCpreprocessor {
    private DefactoModel defactoModel;

    public FCpreprocessor(String data) {
        Model modelISWC = createModel(data);
        init(modelISWC);
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

    private DefactoResource setDefactoResource (Model modelISWC, Model modelFC, Resource resourceNode) {
        DefactoResource defactoResource = new DefactoResource(resourceNode, modelFC, resourceNode.getURI());
        Literal literal = getLabel(modelISWC, resourceNode);
        defactoResource.labels.put(literal.getLanguage(), literal.getLexicalForm());

        return defactoResource;
    }

    private void init(Model modelISWC) {
        Resource subNode = getResource(modelISWC, Constants.RDF_SYNTAX_NAMESPACE + "subject");
        RDFNode predNode = getResource(modelISWC, Constants.RDF_SYNTAX_NAMESPACE + "predicate");
        Resource objNode = getResource(modelISWC, Constants.RDF_SYNTAX_NAMESPACE + "object");

        String dataFC = String.format("<%s> <%s> <%s> .", subNode, predNode, objNode);
        Model model = createModel(dataFC);

        DefactoResource subject = setDefactoResource(modelISWC, model, subNode);
        DefactoResource object = setDefactoResource(modelISWC, model, objNode);

        setDefactoModel(model, subject, object, predNode, "t1");
    }

    public static void main(String[] args) {
        String data = "" +
                "<http://swc2017.aksw.org/task/dataset/s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> .\n" +
                "<http://swc2017.aksw.org/task/dataset/s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://dbpedia.org/resource/Albert_Einstein> .\n" +
                "<http://swc2017.aksw.org/task/dataset/s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <http://dbpedia.org/ontology/award> .\n" +
                "<http://swc2017.aksw.org/task/dataset/s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> <http://dbpedia.org/resource/Nobel_Prize_in_Physics> .\n" +
                "<http://dbpedia.org/resource/Albert_Einstein> <http://www.w3.org/2000/01/rdf-schema#label> \"Albert Einstein\"@en .\n" +
                "<http://dbpedia.org/resource/Nobel_Prize_in_Physics> <http://www.w3.org/2000/01/rdf-schema#label> \"Nobel Prize in Physics\"@en .";

        new FCpreprocessor(data);
    }
}
