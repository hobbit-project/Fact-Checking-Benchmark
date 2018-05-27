package config;

import org.ini4j.Ini;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author DANISH AHMED on 3/27/2018
 */
public class FactbenchConfig {


    private static Ini factbenchConfig;

    private String testCorrectDirectory;
    private String testWrongDirectory;
    private String trainCorrectDirectory;
    private String trainWrongDirectory;
    private String trainDirectory;
    private String testDirectory;

    /**
     * reading configuration from factbench.ini
     * and set variables that are globally required
     * @throws IOException
     */
    public FactbenchConfig(Ini config) throws IOException, URISyntaxException {

        factbenchConfig = config;

        testDirectory = factbenchConfig.get("data", "test");
        trainDirectory = factbenchConfig.get("data", "train");

        testCorrectDirectory = factbenchConfig.get("dataTest", "correct");
        testWrongDirectory = factbenchConfig.get("dataTest", "wrong");

        trainCorrectDirectory = factbenchConfig.get("dataTrain", "correct");
        trainWrongDirectory = factbenchConfig.get("dataTrain", "wrong");
    }

    public String getTrainDirectory() {
        return trainDirectory;
    }

    public String getTestDirectory() {
        return testDirectory;
    }
}
