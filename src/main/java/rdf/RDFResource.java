package rdf;

import org.apache.jena.rdf.model.*;

import java.util.*;

/**
 * @author DANISH AHMED on 3/22/2018
 */
public class RDFResource {

    protected Resource resource;
    protected Model model;
    public String uri;

    private Property owlSameAsProperty = ResourceFactory.createProperty(Constants.OWL_NAMESPACE + "sameAs");

    public List<Resource> owlSameAsList = new ArrayList<Resource>();     // only considering english for now

    /**
     * give me FactCheck resource model
     * @param resource given jena resource
     * @param model jena model
     */
    RDFResource(Resource resource, Model model) {
        this.resource = resource;
        this.uri = resource.getURI();
        this.model = model;

        // set sameAs resource list
        setOwlSameAsList();
    }

    /**
     * get sameAs owl property
     */
    private void setOwlSameAsList() {
        NodeIterator nodeIterator = model.listObjectsOfProperty(this.resource, this.owlSameAsProperty);
        while (nodeIterator.hasNext()) {
            RDFNode rdfNode = nodeIterator.nextNode();
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
        if (uri.contains(Constants.DBPEDIA_URI))
            return uri;

        List<Resource> subjectSameAsList = resource.owlSameAsList;
        String subjectUri = "";

        for (Resource rsc : subjectSameAsList) {
            if (rsc.toString().contains(Constants.DBPEDIA_URI)) {
                subjectUri = rsc.toString();
            }
        }
        return subjectUri;
    }
}
