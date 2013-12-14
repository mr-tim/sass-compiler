package mrtim.sasscompiler;

import java.util.HashMap;
import java.util.Map;

public class Scope {

    private final Scope parentScope;

    private Map<String, String> locals = new HashMap<>();

    public Scope() {
        parentScope = null;
    }

    public Scope(Scope parentScope) {
        this.parentScope = parentScope;
    }

    public boolean isDefined(String variableName) {
        return (parentScope != null && parentScope.isDefined(variableName)) || locals.containsKey(variableName);
    }

    public void define(String name, String value) {
        locals.put(name, value);
    }

    public String get(String name) {
        if (isDefined(name)) {
            if (locals.containsKey(name)) {
                return locals.get(name);
            }
            else {
                return parentScope.get(name);
            }
        }
        return null;
    }
}
