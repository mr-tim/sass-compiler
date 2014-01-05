package mrtim.sasscompiler;

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.LinkedList;
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
        LinkedList<List<String>> selectors = new LinkedList<>();
        selectors.addAll(selectorStack);

        List<String> expanded = new ArrayList<>();

        for (List<String> thisLevel: selectors) {
            if (expanded.isEmpty()) {
                expanded = thisLevel;
            }
            else {
                List<String> newExpanded = new ArrayList<>();
                for (String parentSelector: expanded) {
                    combineSelectorsWithParent(newExpanded, thisLevel, parentSelector);
                }
                expanded = newExpanded;
            }
        }
        return expanded;
    }

    private void combineSelectorsWithParent(List<String> newExpanded, List<String> thisLevel, String parentSelector) {
        for (String selector: thisLevel) {
            newExpanded.add(combineSelectorWithParent(parentSelector, selector));
        }
    }

    private String combineSelectorWithParent(String parentSelector, String selector) {
        if (isBackRef(selector)) {
            return selector.replace("&", parentSelector);
        }
        else {
            return parentSelector + " " + selector;
        }
    }

    private boolean isBackRef(String selector) {
        return selector.contains("&");
    }

    public String expandAndJoin() {
        return Joiner.on(", ").join(expandSelectorStack());
    }

}
