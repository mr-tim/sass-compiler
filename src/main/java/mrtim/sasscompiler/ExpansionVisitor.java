package mrtim.sasscompiler;

import mrtim.sasscompiler.grammar.SassBaseVisitor;
import mrtim.sasscompiler.grammar.SassParser;
import mrtim.sasscompiler.grammar.SassParser.Sass_fileContext;
import mrtim.sasscompiler.grammar.SassParser.ValueContext;
import mrtim.sasscompiler.grammar.SassParser.Variable_defContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.Stack;

public class ExpansionVisitor extends SassBaseVisitor<Void> {

    private final ParseTreeProperty<String> expandedSelectors;
    private final ParseTreeProperty<String> variableValues;
    private Stack<String> selectorStack = new Stack<>();
    private Stack<Scope> scopeStack = new Stack<>();

    public ExpansionVisitor(ParseTreeProperty<String> expandedSelectors, ParseTreeProperty<String> variableValues) {
        this.expandedSelectors = expandedSelectors;
        this.variableValues = variableValues;
    }

    @Override
    public Void visitSass_file(Sass_fileContext ctx) {
        //create the global scope
        visitChildrenWithScope(ctx, new Scope());
        return null;
    }

    private void visitChildrenWithScope(Sass_fileContext ctx, Scope scope) {
        scopeStack.push(scope);
        visitChildren(ctx);
        scopeStack.pop();
    }

    @Override
    public Void visitRuleset(SassParser.RulesetContext ctx) {
        SassParser.Selector_listContext selector = ctx.selector_list();
        selectorStack.push(selector.getText());
        expandedSelectors.put(selector, StringUtils.join(selectorStack, " "));
        visitChildren(ctx);
        selectorStack.pop();
        return null;
    }

    @Override
    public Void visitVariable_def(Variable_defContext ctx) {
        //assign the variable in the scope;
        String variableName = ctx.VARIABLE().getSymbol().getText();
        ExpressionVisitor visitor = new ExpressionVisitor(currentScope());
        visitor.visit(ctx.value_list());
        currentScope().define(variableName, visitor.getValue());
        return null;
    }

    @Override
    public Void visitValue(ValueContext ctx) {
        if (ctx.VARIABLE() != null) {
            variableValues.put(ctx, currentScope().get(ctx.getText()));
            return null;
        }
        else {
            return super.visitValue(ctx);
        }
    }

    private Scope currentScope() {
        return scopeStack.peek();
    }
}
