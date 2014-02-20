package mrtim.sasscompiler;

import mrtim.sasscompiler.builtins.Parameters;
import mrtim.sasscompiler.grammar.SassBaseVisitor;
import mrtim.sasscompiler.grammar.SassParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

public class ParameterExtractionVisitor extends SassBaseVisitor<Parameters> {

    private ExpressionVisitor expressionVisitor;
    private Parameters params;

    public ParameterExtractionVisitor(ExpressionVisitor expressionVisitor) {
        this.expressionVisitor = expressionVisitor;
        params = new Parameters();
    }

    @Override
    public Parameters visitParameter_list(@NotNull SassParser.Parameter_listContext ctx) {
        visitChildren(ctx);
        return params;
    }

    @Override
    public Parameters visitParameter(@NotNull SassParser.ParameterContext ctx) {
        if (ctx.named_parameter() != null) {
            //named parameter
            params.setNamedParamValue(getParameterName(ctx), evaluateParameter(ctx.named_parameter().expression_list()));
        }
        else {
            //positional parameter
            params.addPositionalParamValue(evaluateParameter(ctx.value()));
        }
        return null;
    }

    private String getParameterName(SassParser.ParameterContext ctx) {
        return ctx.named_parameter().VARIABLE().getText().substring(1);
    }

    private String evaluateParameter(ParserRuleContext ctx) {
        return expressionVisitor.visit(ctx).stringValue();
    }
}
