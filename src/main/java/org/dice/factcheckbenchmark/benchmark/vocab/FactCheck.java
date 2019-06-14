package org.dice.factcheckbenchmark.benchmark.vocab;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class FactCheck {

    /**
     * The namespace of the vocabulary as a string
     */
    public static final String uri = "http://dice.cs.uni-paderborn.de/factcheck-benchmark/";

//    public static final Property accuracy = property("accuracy");
    public static final Property auc = property("auc");
    public static final Property confidenceThreshold = property("confidenceThreshold");
    public static final Property dataSet = property("dataSet");
    public static final Property fmeasure = property("fmeasure");
    public static final Property precision = property("precision");
    public static final Property recall = property("recall");
    public static final Property rocCurve = property("rocCurve");
    public static final Property runTime = property("runTime");
    public static final Property avgRunTime = property("avgRunTime");
    public static final Property avgRunTimeStdDev = property("avgRunTimeStdDev");

    /**
     * returns the URI for this schema
     * 
     * @return the URI for this schema
     */
    public static String getURI() {
        return uri;
    }

    protected static final Resource resource(String local) {
        return ResourceFactory.createResource(uri + local);
    }

    protected static final Property property(String local) {
        return ResourceFactory.createProperty(uri, local);
    }
}
