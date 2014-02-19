package mrtim.sasscompiler.builtins;

import java.util.*;

public class Parameters {

    private Map<String, String> defaultValues = new HashMap<>();
    private Map<String, String> namedParamValues = new HashMap<>();
    private Deque<String> positionalParamValues = new LinkedList<>();

    public Parameters() {
        this(Collections.<String, String>emptyMap());
    }

    public Parameters(Map<String, String> defaults) {
        defaultValues = defaults;
    }

    public void setNamedParamValue(String paramName, String value) {
        namedParamValues.put(paramName, value);
    }

    public void addPositionalParamValue(String value) {
        positionalParamValues.addLast(value);
    }

    public Map<String, String> getParamMap(String[] paramNames) {
        Map<String, String> result = new HashMap<>();
        result.putAll(defaultValues);
        result.putAll(namedParamValues);

        String[] posValues = positionalParamValues.toArray(new String[0]);

        int index = 0;

        for (String paramName: paramNames) {
            if (!namedParamValues.containsKey(paramName)) {
                result.put(paramName, posValues[index++]);
            }
        }

        return Collections.unmodifiableMap(result);
    }

    public int count() {
        return namedParamValues.size()+positionalParamValues.size();
    }
}
