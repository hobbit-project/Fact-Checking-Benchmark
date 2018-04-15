package org.hobbit.sdk.examples.examplebenchmark.system.preprocessing;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import java.util.*;

/**
 * @author DANISH AHMED on 4/15/2018
 */
public class DefactoResource {
    private Resource resource;
    private Model model;
    public Map<String,String> labels = new HashMap<String,String>();
    public Map<String,Set<String>> altLabels = new HashMap<String,Set<String>>();
    public List<Resource> owlSameAs = new ArrayList<Resource>();
    private String uri;

    public  DefactoResource(Resource resource, Model model, String uri){
        this.resource = resource;
        this.model = model;
        this.uri = uri;
    }
}
