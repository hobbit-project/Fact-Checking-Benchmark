package org.dice.factcheckbenchmark.benchmark;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.dice.factcheckbenchmark.BenchmarkConstants;
import org.hobbit.core.components.AbstractDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dice.factcheckbenchmark.benchmark.rdf.TripleExtractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

public class DataGenerator extends AbstractDataGenerator {
    private static final Logger logger = LoggerFactory.getLogger(DataGenerator.class);
    private String FACTBENCH_DIRECTORY;

    @Override
    public void init() throws Exception {
        // Always init the super class first!
        super.init();
        logger.debug("Init()");

        //Extract data set configuration from environment variables
        final String DATA_GENERATOR_DIR = BenchmarkConstants.SDK_WORK_DIR_PATH + "data/";
        String factcbenchPath = System.getenv(BenchmarkConstants.ENV_FACTBENCH_DATA_SET);

        factcbenchPath = (factcbenchPath.substring(factcbenchPath.lastIndexOf("/") + 1)).replace("-", "/");
        FACTBENCH_DIRECTORY = DATA_GENERATOR_DIR + factcbenchPath + File.separator;
        logger.debug("FactBench directory {}", FACTBENCH_DIRECTORY);
        // Your initialization code comes here...
    }

    @Override
    protected void generateData() throws Exception {
        // Create your data inside this method. You might want to use the
        // id of this data generator [getGeneratorId()] and the number of all data generators [getNumberOfGenerators()]
        // running in parallel.
        logger.debug("generateData()");
        logger.info("Walking across {}", FACTBENCH_DIRECTORY);
        sendDataFromDirectory(Paths.get(FACTBENCH_DIRECTORY));
    }

    private void sendDataFromDirectory(final Path factbenchPath) throws IOException {
        if (Files.isDirectory(factbenchPath)) {
            Files.walkFileTree(factbenchPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {

                    if (filePath.toString().endsWith("ttl")) {

                        String fileIdentifier = setFileIdentifier(
                                String.valueOf(factbenchPath.toAbsolutePath()).replace("/", "\\"),
                                String.valueOf(filePath.toAbsolutePath()));

                        TripleExtractor tripleExtractor = new TripleExtractor(String.valueOf(filePath.toAbsolutePath()));
                        String data = String.format("%s:*:%s", fileIdentifier, tripleExtractor.getSimplifiedData());

                        logger.debug("sendDataToTaskGenerator({})", filePath.getFileName().toString());
                        sendDataToTaskGenerator(data.getBytes());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            logger.debug("No files found in {}", factbenchPath);
        }
    }

    private void sendDataFromFile(final Path factbenchPath) throws IOException {

        if (Files.isDirectory(factbenchPath)) {
            Files.walkFileTree(factbenchPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {

                    if (filePath.toString().endsWith("nt")) {

                        Model model = ModelFactory.createDefaultModel();
                        model.read(new FileInputStream(new File(filePath.toString())), null, "N-TRIPLES");
                        ArrayList<String> list = new ArrayList<String>();
                        StmtIterator stmtIterator = model.listStatements(null, RDF.type, RDF.Statement);
                        while (stmtIterator.hasNext()){
                            String s = stmtIterator.nextStatement().getSubject().toString();
                            list.add(s);
                        }

                        Collections.sort(list);
                        Collections.shuffle(list, new Random(5));

                        Iterator<String> listIterator = list.iterator();

                        while(listIterator.hasNext()){
                            String statement = listIterator.next();
                            String data = returnStatementGraph(ResourceFactory.createResource(statement), model);
                            sendDataToTaskGenerator(data.getBytes());
                        }

                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        else {
            logger.debug("No files found in {}", factbenchPath);
        }
    }

    public String returnStatementGraph(Resource statement, Model model){

        Statement subject = model.listStatements(statement, RDF.subject, (RDFNode)null).nextStatement();
        Statement object = model.listStatements(statement, RDF.object, (RDFNode)null).nextStatement();
        Statement property = model.listStatements(statement, RDF.predicate, (RDFNode)null).nextStatement();

        Statement subjectLabel = model.listStatements(subject.getObject().asResource(), RDFS.label, (RDFNode)null).nextStatement();
        Statement objectLabel = model.listStatements(object.getObject().asResource(), RDFS.label, (RDFNode)null).nextStatement();

        Statement truthValue = model.listStatements(statement, ResourceFactory.createProperty("http://swc2017.aksw.org/hasTruthValue"), (RDFNode)null).nextStatement();

        String truth = "";

        if(truthValue.getObject().asLiteral().getDouble()==1.0)
            truth = "/correct/";
        else
            truth = "/wrong/";

        String data = String.format("%s:*:"+"<%s> " + "<%s> " + "<%s> " +".\n"+
                        "<%s> " + "<%s> " + "<%s> " +".\n"+
                        "<%s> " + "<%s> " + "<%s> " +".\n"+
                        "<%s> " + "<%s> " + "<%s> " +".\n"+
                        "<%s> " + "<%s> " + "\"%s\"@en " +".\n"+
                        "<%s> " + "<%s> " + "\"%s\"@en " +".",
                truth, statement.toString(), RDF.type.toString(), RDF.Statement.toString(),
                subject.getSubject(), subject.getPredicate(), subject.getObject(),
                object.getSubject(), object.getPredicate(), object.getObject(),
                property.getSubject(), property.getPredicate(), property.getObject(),
                subjectLabel.getSubject(), subjectLabel.getPredicate(), subjectLabel.getObject(),
                objectLabel.getSubject(), objectLabel.getPredicate(), objectLabel.getObject());


        return data;
    }

    private String setFileIdentifier(String rootDir, String directory) {
        directory = directory
                .replace(rootDir, "")
                .replace(".ttl", "")
                .replace("\\", "-");
        return directory.substring(1);
    }

    @Override
    public void close() throws IOException {
        // Free the resources you requested here
        logger.debug("close()");
        // Always close the super class after yours!
        super.close();
    }
}