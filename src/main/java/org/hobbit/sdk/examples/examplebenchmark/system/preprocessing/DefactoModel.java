package org.hobbit.sdk.examples.examplebenchmark.system.preprocessing;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;

import java.util.ArrayList;
import java.util.List;

/**
 * @author DANISH AHMED on 4/15/2018
 */
public class DefactoModel {
    public Model model;
    public String name;
    public boolean correct;
    public DefactoResource subject;
    public Property predicate;
    public String predicateUri;
    public DefactoResource object;
//    public DefactoTimePeriod timePeriod = new DefactoTimePeriod("", "");
    public List<String> languages = new ArrayList<String>();
//    public StanfordCoreNLP pipeline;
//    public StanfordCoreNLP pipeline1;

    public DefactoModel(Model model, DefactoResource subject, DefactoResource object, Property predicate, String name) {
        this.model = model;
        this.subject = subject;
        this.object = object;
        this.name = name;
        this.predicate = predicate;

        this.predicateUri = predicate.getURI();
        this.correct = false;
    }
}
