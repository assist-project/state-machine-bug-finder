package se.uu.it.smbugfinder.encoding;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomParsingContext extends ParsingContext {

    public CustomParsingContext(OcamlValues params) {
        super(new CustomFields(params), new CustomFunctions(params));
    }

    // these static classes could be declared each in its own file
    static class CustomFields extends Fields {
        private static final long serialVersionUID = 1L;
        public CustomFields(OcamlValues params) {
            super();

            Map<String, List<String>> messageMap = params.getMessageMap();
            List<Field> fields = new ArrayList<>();
            for (Map.Entry<String, List<String>> e : messageMap.entrySet()) {
                fields.add(CustomField.create(e.getValue(), e.getKey()));
            }
            initialize(fields.toArray(new Field[fields.size()]));
        }
    }

    static class CustomFunctions extends Functions {
        private static final long serialVersionUID = 1L;
        public CustomFunctions(OcamlValues params) {
            super();

            List<List<Object>> functions = params.getFunctionsList();
            List<CustomFunction> functions2 = new ArrayList<>();
            for (List<Object> l : functions) {
                @SuppressWarnings("unchecked")
                Map<String, String> mapping = (Map<String, String>) l.get(1); // unchecked cast :(
                functions2.add(CustomFunction.create(l.get(0).toString(), mapping));
            }
            initialize(functions2.toArray(new Function[functions2.size()]));
        }
    }
}
