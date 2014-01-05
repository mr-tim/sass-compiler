package mrtim.sasscompiler;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class SelectorStack {

    private Stack<List<String>> selectorStack = new Stack<>();

    public void push(List<String> selectors) {
        selectorStack.push(selectors);
    }

    public void pop() {
        selectorStack.pop();
    }

    private List<String> expandSelectorStack() {
        Stack<List<String>> selectors = new Stack<>();
        selectors.addAll(selectorStack);

        List<String> expanded = new ArrayList<>();

        while (!selectors.empty()) {
            List<String> newPrefixes = selectors.pop();
            if (expanded.isEmpty()) {
                expanded = newPrefixes;
            }
            else {
                expanded = addPrefixes(newPrefixes, expanded);
            }
        }
        return expanded;
    }

    public String expandAndJoin() {
        return StringUtils.join(expandSelectorStack(), ", ");
    }

    private List<String> addPrefixes(List<String> newPrefixes, List<String> items) {
        List<String> prefixed = new ArrayList<>();
        for (String newPrefix: newPrefixes) {
            for (String item: items) {
                prefixed.add(newPrefix + " " + item);
            }
        }
        return prefixed;
    }

}
