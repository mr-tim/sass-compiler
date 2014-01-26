package mrtim.sasscompiler;

import mrtim.sasscompiler.expr.DimensionExpressionValue;
import mrtim.sasscompiler.expr.DivisionExpression;
import mrtim.sasscompiler.expr.ExpressionValue;
import mrtim.sasscompiler.expr.ExpressionValue.Operator;
import mrtim.sasscompiler.expr.ListExpressionValue;
import mrtim.sasscompiler.expr.NumberExpressionValue;
import mrtim.sasscompiler.expr.PercentageExpressionValue;
import mrtim.sasscompiler.expr.StringExpressionValue;
import mrtim.sasscompiler.grammar.SassParser.DimensionContext;
import mrtim.sasscompiler.grammar.SassParser.DivideExpressionContext;
import mrtim.sasscompiler.grammar.SassParser.Expression_listContext;
import mrtim.sasscompiler.grammar.SassParser.ListExpressionContext;
import mrtim.sasscompiler.grammar.SassParser.MinusExpressionContext;
import mrtim.sasscompiler.grammar.SassParser.MultiplyExpressionContext;
import mrtim.sasscompiler.grammar.SassParser.NumberContext;
import mrtim.sasscompiler.grammar.SassParser.ParenExpressionContext;
import mrtim.sasscompiler.grammar.SassParser.PercentageContext;
import mrtim.sasscompiler.grammar.SassParser.PlusExpressionContext;
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
    public ExpressionValue visitMultiplyExpression(@NotNull MultiplyExpressionContext ctx) {
        return visit(ctx.expression(0)).operate(Operator.MULTIPLY, visit(ctx.expression(1)));
    }

    @Override
    public ExpressionValue visitDivideExpression(@NotNull DivideExpressionContext ctx) {
        ExpressionValue left = visit(ctx.expression(0));
        ExpressionValue right = visit(ctx.expression(1));
        DivisionExpression result = new DivisionExpression(left, right);
        if (shouldEvaluateDivision(left, right)) {
            return result.evaluate();
        }
        else {
            return result;
        }
    }

    private boolean shouldEvaluateDivision(ExpressionValue left, ExpressionValue right) {
        return isEvaluatedNumberExpression(left) || isEvaluatedNumberExpression(right);
    }

    private boolean isEvaluatedNumberExpression(ExpressionValue value) {
        return value instanceof NumberExpressionValue && ((NumberExpressionValue) value).isEvaluated();
    }

    @Override
    public ExpressionValue visitPlusExpression(@NotNull PlusExpressionContext ctx) {
        return visit(ctx.expression(0)).operate(Operator.ADD, visit(ctx.expression(1)));
    }

    @Override
    public ExpressionValue visitMinusExpression(@NotNull MinusExpressionContext ctx) {
        return visit(ctx.expression(0)).operate(Operator.SUBTRACT, visit(ctx.expression(1)));
    }

    @Override
    public ExpressionValue visitParenExpression(@NotNull ParenExpressionContext ctx) {
        ExpressionValue result = visit(ctx.expression());
        if (result instanceof DivisionExpression) {
            return ((DivisionExpression) result).evaluate();
        }
        else {
            return result;
        }
    }

    @Override
    public ExpressionValue visitListExpression(@NotNull ListExpressionContext ctx) {
        return visit(ctx.expression_list());
    }

    @Override
    public ExpressionValue visitValue(ValueContext ctx) {
        if (ctx.VARIABLE() != null) {
            return markAsEvaluated(scope.get(ctx.VARIABLE().getText()));
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

    private ExpressionValue markAsEvaluated(ExpressionValue result) {
        if (result instanceof NumberExpressionValue) {
            return new NumberExpressionValue(((NumberExpressionValue) result).bigDecimalValue(), true);
        }
        else {
            return result;
        }
    }

    private NumberExpressionValue numberExpression(NumberContext number) {
        return new NumberExpressionValue(new BigDecimal(number.getText()), false);
    }

}
