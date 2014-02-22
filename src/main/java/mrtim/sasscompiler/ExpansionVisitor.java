package mrtim.sasscompiler;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import mrtim.sasscompiler.expr.ExpressionValue;
import mrtim.sasscompiler.grammar.SassBaseVisitor;
import mrtim.sasscompiler.grammar.SassParser;
import mrtim.sasscompiler.grammar.SassParser.DefinitionContext;
import mrtim.sasscompiler.grammar.SassParser.ExpressionListContext;
import mrtim.sasscompiler.grammar.SassParser.MultiExpressionListContext;
import mrtim.sasscompiler.grammar.SassParser.Sass_fileContext;
import mrtim.sasscompiler.grammar.SassParser.Selector_combinationContext;
import mrtim.sasscompiler.grammar.SassParser.Variable_defContext;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import java.util.List;
import java.util.Stack;

public class ExpansionVisitor extends SassBaseVisitor<Void> {

    private final ParseTreeProperty<String> expandedSelectors;
    private final ParseTreeProperty<ExpressionValue> evaluatedExpressions;
    private ParseTreeProperty<MixinScopeInitialiser> mixinScopeInitialisers;

    private SelectorStack selectorStack = new SelectorStack();
    private Stack<Scope> scopeStack = new Stack<>();

    public ExpansionVisitor(ParseTreeProperty<String> expandedSelectors, ParseTreeProperty<ExpressionValue> evaluatedExpressions, ParseTreeProperty<MixinScopeInitialiser> mixinScopeInitialisers) {
        this.expandedSelectors = expandedSelectors;
        this.evaluatedExpressions = evaluatedExpressions;
        this.mixinScopeInitialisers = mixinScopeInitialisers;
    }

    @Override
    public Void visitSass_file(Sass_fileContext ctx) {
        //create the global scope
        visitChildrenWithScope(ctx, new Scope());
        return null;
    }

    @Override
    public Void visitBlock_body(@NotNull SassParser.Block_bodyContext ctx) {
        MixinScopeInitialiser scopeInitialiser = mixinScopeInitialisers.get(ctx);
        Scope scope = currentScope();
        if (scopeInitialiser != null) {
            scope = new Scope(scope);
            scopeInitialiser.initialiseScope(scope);

        }
        visitChildrenWithScope(ctx, scope);

        return null;
    }

    private void visitChildrenWithScope(ParserRuleContext ctx, Scope scope) {
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
    public Void visitDefinition(DefinitionContext ctx) {
        if (ctx.MIXIN_KW() != null) {
            //skip definition of mixins
            return null;
        }
        else {
            return super.visitDefinition(ctx);
        }
    }

    @Override
    public Void visitVariable_def(Variable_defContext ctx) {
        //assign the variable in the scope;
        String variableName = ctx.VARIABLE().getSymbol().getText();
        ExpressionVisitor visitor = new ExpressionVisitor(currentScope());
        currentScope().define(variableName, visitor.visit(ctx.expression_list()));
        return null;
    }

    @Override
    public Void visitExpressionList(@NotNull ExpressionListContext ctx) {
        return evaluate(ctx);
    }

    @Override
    public Void visitMultiExpressionList(@NotNull MultiExpressionListContext ctx) {
        return evaluate(ctx);
    }

    private Void evaluate(ParseTree ctx) {
        evaluatedExpressions.put(ctx, new ExpressionVisitor(currentScope()).visit(ctx));
        return null;
    }

    private Scope currentScope() {
        return scopeStack.peek();
    }
}
