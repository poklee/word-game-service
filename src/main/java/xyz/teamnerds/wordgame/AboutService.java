package xyz.teamnerds.wordgame;

import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AboutService
{

    @GetMapping(path = "/", produces = "text/plain")
    public String getServerInfo()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("About Microservice \r\n");
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF"))
        {
            if (is != null)
            {
                final String[] manifestKeysToExpose = new String[] { "Implementation-Title", "Implementation-Version",
                        "Build-Jdk-Spec" };
                Manifest manifest = new Manifest(is);
                Attributes attribute = manifest.getMainAttributes();
                for (String key : manifestKeysToExpose)
                {
                    sb.append(key).append(": ").append(attribute.getValue(key)).append("\r\n");
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return sb.toString();
    }

}
