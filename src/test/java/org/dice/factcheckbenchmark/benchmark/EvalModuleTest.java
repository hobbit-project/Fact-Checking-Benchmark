package org.dice.factcheckbenchmark.benchmark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.dice.factcheckbenchmark.benchmark.vocab.FactCheck;
import org.hobbit.core.Constants;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.utils.test.ModelComparisonHelper;
import org.hobbit.vocab.HOBBIT;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class EvalModuleTest {
    
    private static final String EXPERIMENT_URI = "http://ex.test.org/experiment/1"; 

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Parameters
    public static Collection<Object[]> data() {
        List<Object[]> testCases = new ArrayList<>();
        Model expectedResult;
        Resource experiment = ResourceFactory.createResource(EXPERIMENT_URI);
        List<ResponseTuple> responses;

        byte[] expectedTrue = RabbitMQUtils.writeString(EvalModule.TRUE_RESPONSE);
        byte[] expectedFalse = RabbitMQUtils.writeString(EvalModule.FALSE_RESPONSE);

        responses = Arrays.asList(new ResponseTuple(expectedTrue, RabbitMQUtils.writeString(
                "<http://ex.org/1> <http://swc2017.aksw.org/hasTruthValue> \"0.9\"^^<http://www.w3.org/2001/XMLSchema#double>"),
                100, 101), new ResponseTuple(expectedFalse, RabbitMQUtils.writeString(
                "<http://ex.org/2> <http://swc2017.aksw.org/hasTruthValue> \"0.1\"^^<http://www.w3.org/2001/XMLSchema#double>"),
                101, 102));
        expectedResult = ModelFactory.createDefaultModel();
        expectedResult.add(experiment, RDF.type, HOBBIT.Experiment);
        expectedResult.add(experiment, FactCheck.auc, expectedResult.createTypedLiteral(1.0));
        expectedResult.add(experiment, FactCheck.avgRunTime, expectedResult.createTypedLiteral(1.0));
        expectedResult.add(experiment, FactCheck.avgRunTimeStdDev, expectedResult.createTypedLiteral(0.0));
        expectedResult.add(experiment, FactCheck.confidenceThreshold, expectedResult.createTypedLiteral(0.9));
        expectedResult.add(experiment, FactCheck.fmeasure, expectedResult.createTypedLiteral(1.0));
        expectedResult.add(experiment, FactCheck.precision, expectedResult.createTypedLiteral(1.0));
        expectedResult.add(experiment, FactCheck.recall, expectedResult.createTypedLiteral(1.0));
        expectedResult.add(experiment, FactCheck.runTime, expectedResult.createTypedLiteral(2L));
        
        testCases.add(new Object[] {responses, expectedResult});

        return testCases;
    }

    private Model expectedResult;
    private List<ResponseTuple> responses;

    public EvalModuleTest(List<ResponseTuple> responses, Model expectedResult) {
        this.expectedResult = expectedResult;
        this.responses = responses;
    }

    @Test
    public void test() throws Exception {
        // Get the experiment URI
        environmentVariables.set(Constants.HOBBIT_EXPERIMENT_URI_KEY, EXPERIMENT_URI);
        EvalModule module = new EvalModule();
        try {
            for (ResponseTuple response : responses) {
                module.evaluateResponse(response.expectedData, response.receivedData, response.taskSentTimestamp,
                        response.responseReceivedTimestamp);
            }
            compareModels(module);
        } finally {
            module.close();
        }
    }

//    @Test
//    public void testOldFormat() throws Exception {
//        environmentVariables.set(Constants.HOBBIT_EXPERIMENT_URI_KEY, EXPERIMENT_URI);
//        EvalModule module = new EvalModule();
//        try {
//            byte[] receivedData = null;
//            for (ResponseTuple response : responses) {
//                if (response.receivedData == null) {
//                    receivedData = null;
//                } else {
//                    receivedData = RabbitMQUtils.writeString(Double.toString(module.parseResponse(response.receivedData)));
//                }
//                module.evaluateResponse(response.expectedData, receivedData, response.taskSentTimestamp,
//                        response.responseReceivedTimestamp);
//            }
//            compareModels(module);
//        } finally {
//            module.close();
//        }
//    }

    protected void compareModels(EvalModule module) throws Exception {
        Model result = module.summarizeEvaluation();

        // Compare the models
        String expectedModelString = expectedResult.toString();
        String resultModelString = result.toString();
        // Check the precision and recall
        Set<Statement> missingStatements = ModelComparisonHelper.getMissingStatements(expectedResult, result);
        Set<Statement> unexpectedStatements = ModelComparisonHelper.getMissingStatements(result, expectedResult);

        StringBuilder builder = new StringBuilder();
        if (unexpectedStatements.size() != 0) {
            builder.append("The result contains the unexpected statements:\n\n"
                    + unexpectedStatements.stream().map(Object::toString).collect(Collectors.joining("\n"))
                    + "\n\nExpected model:\n\n" + expectedModelString + "\nResult model:\n\n" + resultModelString
                    + "\n");
        }
        if (missingStatements.size() != 0) {
            builder.append("The result does not contain the expected statements:\n\n"
                    + missingStatements.stream().map(Object::toString).collect(Collectors.joining("\n"))
                    + "\n\nExpected model:\n\n" + expectedModelString + "\n\nResult model:\n\n" + resultModelString
                    + "\n");
        }

        Assert.assertTrue(builder.toString(), missingStatements.size() == 0 && unexpectedStatements.size() == 0);
    }

    public static class ResponseTuple {
        public byte[] expectedData;
        public byte[] receivedData;
        public long taskSentTimestamp;
        public long responseReceivedTimestamp;

        public ResponseTuple() {
        }

        public ResponseTuple(byte[] expectedData, byte[] receivedData, long taskSentTimestamp,
                long responseReceivedTimestamp) {
            super();
            this.expectedData = expectedData;
            this.receivedData = receivedData;
            this.taskSentTimestamp = taskSentTimestamp;
            this.responseReceivedTimestamp = responseReceivedTimestamp;
        }
    }
}
