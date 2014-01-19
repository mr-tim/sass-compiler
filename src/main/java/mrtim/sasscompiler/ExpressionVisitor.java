package mrtim.sasscompiler;

import mrtim.sasscompiler.expr.DimensionExpressionValue;
import mrtim.sasscompiler.expr.ExpressionValue;
import mrtim.sasscompiler.expr.ExpressionValue.Operator;
import mrtim.sasscompiler.expr.ListExpressionValue;
import mrtim.sasscompiler.expr.NumberExpressionValue;
import mrtim.sasscompiler.expr.PercentageExpressionValue;
import mrtim.sasscompiler.expr.StringExpressionValue;
import mrtim.sasscompiler.grammar.SassParser.DimensionContext;
import mrtim.sasscompiler.grammar.SassParser.ExpressionContext;
import mrtim.sasscompiler.grammar.SassParser.Expression_listContext;
import mrtim.sasscompiler.grammar.SassParser.NumberContext;
import mrtim.sasscompiler.grammar.SassParser.PercentageContext;
import mrtim.sasscompiler.grammar.SassParser.ValueContext;
import org.antlr.v4.runtime.misc.NotNull;

import java.math.BigDecimal;
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
    public ExpressionValue visitExpression(@NotNull ExpressionContext ctx) {
        if (ctx.value() != null) {
            return visit(ctx.value());
        }
        else if (ctx.expression_list() != null) {
            return visit(ctx.expression_list());
        }
        else {
            ExpressionValue left = visit(ctx.expression(0));
            ExpressionValue right = visit(ctx.expression(1));
            Operator op = Operator.fromString(ctx.getChild(1).getText());
            return left.operate(op, right);
        }
    }

    @Override
    public ExpressionValue visitValue(ValueContext ctx) {
        if (ctx.VARIABLE() != null) {
            return scope.get(ctx.VARIABLE().getText());
        }
        else if (ctx.number() != null) {
            return numberExpression(ctx.number());
        }
        else if (ctx.dimension() != null) {
            DimensionContext dim = ctx.dimension();
            return new DimensionExpressionValue(numberExpression(dim.number()), dim.DIMENSION().getText());
        }
        else if (ctx.percentage() != null) {
            PercentageContext per = ctx.percentage();
            return new PercentageExpressionValue((numberExpression(per.number())));
        }
        else {
            return new StringExpressionValue(ctx.getText());
        }
    }

    private NumberExpressionValue numberExpression(NumberContext number) {
        return new NumberExpressionValue(new BigDecimal(number.getText()));
    }

}
