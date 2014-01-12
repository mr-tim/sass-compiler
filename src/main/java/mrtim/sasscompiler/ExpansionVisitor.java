package mrtim.sasscompiler;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import mrtim.sasscompiler.grammar.SassBaseVisitor;
import mrtim.sasscompiler.grammar.SassParser;
import mrtim.sasscompiler.grammar.SassParser.Sass_fileContext;
import mrtim.sasscompiler.grammar.SassParser.Selector_combinationContext;
import mrtim.sasscompiler.grammar.SassParser.ValueContext;
import mrtim.sasscompiler.grammar.SassParser.Variable_defContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import java.util.List;
import java.util.Stack;

public class ExpansionVisitor extends SassBaseVisitor<Void> {

    private final ParseTreeProperty<String> expandedSelectors;
    private final ParseTreeProperty<String> variableValues;
    private SelectorStack selectorStack = new SelectorStack();
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
        selectorStack.push(extractSelectors(ctx.selector_list().selector_combination()));
        expandedSelectors.put(selector, selectorStack.expandAndJoin());
        visitChildren(ctx);
        selectorStack.pop();
        return null;
    }

    private List<String> extractSelectors(List<Selector_combinationContext> selectorCombinations) {
        return FluentIterable.from(selectorCombinations).transform(new Function<Selector_combinationContext, String>() {
            @Override
            public String apply(Selector_combinationContext input) {
                return new SelectorCombinationVisitor().visit(input);
            }
        }).toList();
    }

    @Override
    public Void visitVariable_def(Variable_defContext ctx) {
        //assign the variable in the scope;
        String variableName = ctx.VARIABLE().getSymbol().getText();
        ExpressionVisitor visitor = new ExpressionVisitor(currentScope());
        visitor.visit(ctx.expression_list());
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
