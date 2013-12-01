package mrtim.sasscompiler;

import mrtim.sasscompiler.grammar.SassBaseVisitor;
import mrtim.sasscompiler.grammar.SassParser;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.Stack;

public class ExpansionVisitor extends SassBaseVisitor {

    private final ParseTreeProperty<String> expandedSelectors;
    private Stack<String> selectorStack = new Stack<>();

    public ExpansionVisitor(ParseTreeProperty<String> expandedSelectors) {
        this.expandedSelectors = expandedSelectors;
    }

    @Override
    public Object visitRuleset(SassParser.RulesetContext ctx) {
        SassParser.Selector_listContext selector = ctx.selector_list();
        selectorStack.push(selector.getText());
        expandedSelectors.put(selector, StringUtils.join(selectorStack, " "));
        visitChildren(ctx);
        selectorStack.pop();
        return null;
    }

}
