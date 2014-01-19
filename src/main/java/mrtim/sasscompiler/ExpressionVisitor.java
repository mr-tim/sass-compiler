package mrtim.sasscompiler;

import mrtim.sasscompiler.expr.ExpressionValue;
import mrtim.sasscompiler.expr.ListExpressionValue;
import mrtim.sasscompiler.expr.StringExpressionValue;
import mrtim.sasscompiler.grammar.SassParser.Expression_listContext;
import mrtim.sasscompiler.grammar.SassParser.ValueContext;

import java.util.ArrayList;
import java.util.List;

public class ExpressionVisitor extends BaseVisitor<ExpressionValue> {

    private Scope scope;

    public ExpressionVisitor(Scope scope) {
        this.scope = scope;
    }

    @Override
    public ExpressionValue visitExpression_list(Expression_listContext ctx) {
        if (ctx.expression().size() == 1) {
            return visit(ctx.expression(0));
        }
        else {
            List<ExpressionValue> expressions = new ArrayList<>();
            for (int i=0; i<ctx.expression().size(); i++) {
                expressions.add(visit(ctx.expression(i)));
            }
            return new ListExpressionValue(expressions);
        }
    }

    @Override
    public ExpressionValue visitValue(ValueContext ctx) {
        if (ctx.VARIABLE() != null) {
            return scope.get(ctx.VARIABLE().getText());
        }
        else {
            return new StringExpressionValue(ctx.getText());
        }
    }

}
