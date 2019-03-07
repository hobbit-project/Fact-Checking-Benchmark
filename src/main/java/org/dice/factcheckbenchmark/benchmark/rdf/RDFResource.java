package org.dice.factcheckbenchmark.benchmark.rdf;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;

import java.util.*;

/**
 * @author DANISH AHMED on 3/22/2018
 */
public class RDFResource {

    protected Resource resource;
    protected Model model;
    protected String label;
    public String uri;

    private Property owlSameAsProperty = OWL.sameAs;

    public HashSet<Resource> owlSameAsList = new HashSet<Resource>();     // only considering english for now
    public Map<String, String> langLabelsMap = new HashMap<String, String>();

    /**
     * give me FactCheck resource model
     * @param resource given jena resource
     * @param model jena model
     */
    RDFResource(Resource resource, Model model) {
        this.resource = resource;
        this.uri = resource.getURI();
        this.model = model;

        // set labels w.r.t language
        Property labelProperty = RDFS.label;
        NodeIterator labelNodeIterator = this.model.listObjectsOfProperty(this.resource, labelProperty);

        // set sameAs resource list
        getResourceLabel(labelNodeIterator);
        setOwlSameAsList();
    }

    private void getResourceLabel(NodeIterator nodeIterator) {
        while (nodeIterator.hasNext()) {
            RDFNode rdfNode = nodeIterator.nextNode();
            String lang = rdfNode.asLiteral().getLanguage();
            String label = rdfNode.asLiteral().getLexicalForm();

            langLabelsMap.put(lang, label);
            if (lang.equals("en")) {
                this.label = label;
            }
        }
    }

    /**
     * get sameAs owl property
     */
    private void setOwlSameAsList() {
        NodeIterator nodeIterator = model.listObjectsOfProperty(this.resource, this.owlSameAsProperty);
        while (nodeIterator.hasNext()) {
            RDFNode rdfNode = nodeIterator.nextNode();
            if (rdfNode.isURIResource())
                owlSameAsList.add(rdfNode.asResource());
        }
    }

    /**
     * filter out only dbpedia resource that is not inter-lang
     * @param resource RDFResource
     * @return uri of resource
     */
    public static String getDBpediaUri(RDFResource resource) {
        String uri = resource.uri;
        if (uri.startsWith(Constants.DBPEDIA_URI))
            return uri;

        HashSet<Resource> subjectSameAsList = resource.owlSameAsList;
        String subjectUri = "";

        for (Resource rsc : subjectSameAsList) {
            if (rsc.getURI().startsWith(Constants.DBPEDIA_URI)) {
                return rsc.toString();
            }
        }
        return subjectUri;
    }
}
