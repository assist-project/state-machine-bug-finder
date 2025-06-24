package se.uu.it.smbugfinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

/**
 * A class used to conveniently fetch resources at a given path which it does by:
 *  (1) using the system class loader, e.g., looking in src/test/java and src/main/java
 *  (2) if the first method was unsuccessful, it then uses the absolute path of the resource, throwing an instance of
 *  {@link ResourceLoadingException} if it fails.
 */
public class ResourceManager {

    public static final InputStream getResourceAsStream(String resourcePath) {
        InputStream encodedDfaStream = StateMachineBugFinder.class.getResourceAsStream(resourcePath);
        if (encodedDfaStream == null) {
            File file = new File(resourcePath);
            if (file.exists()) {
                try {
                    encodedDfaStream = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    throw new ResourceLoadingException("Failed to load resource at path " + resourcePath, e);
                }
            }
        }
        if (encodedDfaStream == null) {
            throw new ResourceLoadingException("Could not find resource at path " + resourcePath);
        }
        return encodedDfaStream;
    }

    // TODO: loading resources should be looked at again, there is duplication between the two methods
    public static final String getResourceAsAbsolutePathString(String resourcePath) {
        URL res = StateMachineBugFinder.class.getResource(resourcePath);
        String absolutePath = null;
        if (res == null ) {
            File file = new File(resourcePath);
            if (file.exists()) {
                absolutePath = file.getAbsolutePath();
            }
        } else {
             if (res.getFile() != null) {
                 File file = new File(res.getFile());
                 if (file.exists()) {
                    absolutePath = file.getAbsolutePath();
                }
             }
        }
        return absolutePath;
    }
}
