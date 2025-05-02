package se.uu.it.smbugfinder.encoding;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.google.common.base.Ascii;

import lombok.Getter;

public class OcamlValues {
    public static native Map<String, List<String>> getFieldsOcaml(String path);
    public static native Map<String, List<String>> getMessageMapOcaml(String path);
    public static native List<List<Object>> getFunctionsOcaml(String path); // each sub-list contains a function name, a type and a mapping

    static {
      // System.loadLibrary("stubs"); // looks for libraries in java.library.path
      String os = Ascii.toLowerCase(System.getProperty("os.name"));
      String libName = os.contains("mac") ? "libstubs.dylib" : "libstubs.so";
      System.load(new File("target/classes/" + libName).getAbsolutePath());
    }

    @Getter private Map<String, List<String>> fieldsMap;
    @Getter private Map<String, List<String>> messageMap;
    @Getter private List<List<Object>> functionsList;

    public OcamlValues(String lang_file) {
      fieldsMap = getFieldsOcaml(lang_file);
      messageMap = getMessageMapOcaml(lang_file);
      functionsList = getFunctionsOcaml(lang_file);
    }
}
