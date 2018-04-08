package config;

import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author DANISH AHMED on 3/27/2018
 */
public class IniConfig {
    public static IniConfig configInstance;

    static {
        try {
            configInstance = new IniConfig();
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public String testCorrectDirectory;
    public String testWrongDirectory;
    public String trainCorrectDirectory;
    public String trainWrongDirectory;

    /**
     * reading configuration from factcheckBenchmark.ini
     * and set variables that are globally required
     * @throws IOException
     */
    private IniConfig() throws IOException, URISyntaxException {
        String resourceDirectory = "src/main/resources/";
        String configFile = "factcheckBenchmark.ini";
        Ini configIni = new Ini(new File(resourceDirectory + configFile));

        testCorrectDirectory = configIni.get("dataTest", "correct");
        testWrongDirectory = configIni.get("dataTest", "wrong");

        trainCorrectDirectory = configIni.get("dataTrain", "correct");
        trainWrongDirectory = configIni.get("dataTrain", "wrong");
    }

    public static void main(String[] args) {
        System.out.println(configInstance.testCorrectDirectory);
    }
}
