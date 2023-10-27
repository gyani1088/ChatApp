import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Constants {
    private static Constants constants = null;
    private static Properties properties;

    private Constants() throws IOException {
        properties = new Properties();
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        InputStream stream = new FileInputStream(rootPath+"application.properties");
        properties.load(stream);
    }

    public static Constants getInstance() {
        try {
            if (constants == null) constants = new Constants();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
        return constants;
    }
    public static Properties getProperties() {
        return properties;
    }
}
