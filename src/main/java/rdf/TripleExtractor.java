package rdf;

import org.apache.jena.rdf.model.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author DANISH AHMED on 3/22/2018
 */
public class TripleExtractor {

    private Model model;     // provides model after file has been read
    private RDFResource subject;
    private Property predicate;
    private RDFResource object;

    private String subjectUri;
    private String predicateUri;
    private String objectUri;

    public TripleExtractor(String fileName) throws FileNotFoundException {
        Model model = ModelFactory.createDefaultModel();
        model.read(new FileInputStream(fileName), null, "TTL");

        setModel(model);
        parseStatements();
        setUris();
    }

    private void setUris() {
        subjectUri = getResourceUri(subject);
        objectUri = getResourceUri(object);
        predicateUri = predicate.getURI();
    }

    private void setModel(Model model) {
        this.model = model;
    }

    private void parseStatements() {
        StmtIterator stmtIterator = this.model.listStatements();
        Resource subjectNode = null;
        RDFNode objectNode;

        while (stmtIterator.hasNext()) {
            Statement statement = stmtIterator.next();

            // look for starting node
            if (statement.getSubject().getURI().matches("^.*__[0-9]*$")) {
                if (statement.getObject().isResource()) {
                    subjectNode = statement.getSubject();
                    this.predicate = statement.getPredicate();
                    objectNode = statement.getObject();

                    // check if object is resource and has edges, parse until you get Literal
                    getObject(statement, objectNode);

                    // now find if current statement subject node is part of object node
                    // then make that node as subject
                    if (subjectNode == null)
                        continue;
                    getSubject(subjectNode);
                }
            }
        }
    }

    /**
     * retrieve subject node
     * @param subjectNode subject initial node
     */
    private void getSubject(Resource subjectNode) {
        StmtIterator stmtIterator = this.model.listStatements();
        while (stmtIterator.hasNext()) {
            Statement statement = stmtIterator.next();

            if (statement.getObject().isResource()
                    && statement.getObject().asResource().getURI().equals(subjectNode.getURI())) {
                subjectNode = statement.getSubject();
                this.subject = new RDFResource(subjectNode.asResource(), model);
            }
        }
    }

    /**
     * retrieve object node
     * @param statement node statement
     */
    private RDFNode getObject(Statement statement, RDFNode objectNode) {
        if (objectNode.isLiteral()) {
            this.object = new RDFResource(statement.getSubject().asResource(), this.model);
            return objectNode;
        }

        if (objectNode.isResource()) {
            // parse all statements again
            // if object URI becomes subject URI, it will either have literal or resource
            // if it's a literal, return object
            // else call this function again

            StmtIterator stmtIterator = this.model.listStatements();
            while (stmtIterator.hasNext()) {
                Statement stmt = stmtIterator.next();

                if (stmt.getSubject().getURI().equals(objectNode.asResource().getURI())) {
                    RDFNode objNode = stmt.getObject();
                    this.object = new RDFResource(stmt.getSubject().asResource(), this.model);

                    return getObject(stmt, objNode);
                }
            }
        }
        return null;
    }

    private String getResourceUri(RDFResource resource) {
        if (resource.owlSameAsList.isEmpty())
            return resource.uri;
        else
            return RDFResource.getDBpediaUri(resource);
    }

    public static void main(String[] args) throws FileNotFoundException {
        TripleExtractor tripleExtractor = new TripleExtractor("Einstein.ttl");
        System.out.println(tripleExtractor.subjectUri);
        System.out.println(tripleExtractor.predicate);
        System.out.println(tripleExtractor.objectUri);
    }
}
