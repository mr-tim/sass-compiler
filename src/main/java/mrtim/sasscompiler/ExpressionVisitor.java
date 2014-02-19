package mrtim.sasscompiler;

import mrtim.sasscompiler.builtins.Parameters;
import mrtim.sasscompiler.expr.ColourExpressionValue;
import mrtim.sasscompiler.expr.DimensionExpressionValue;
import mrtim.sasscompiler.expr.DivisionExpression;
import mrtim.sasscompiler.expr.ExpressionValue;
import mrtim.sasscompiler.expr.ExpressionValue.Operator;
import mrtim.sasscompiler.expr.ListExpressionValue;
import mrtim.sasscompiler.expr.MultListExpressionValue;
import mrtim.sasscompiler.expr.NumberExpressionValue;
import mrtim.sasscompiler.expr.PercentageExpressionValue;
import mrtim.sasscompiler.expr.StringExpressionValue;
import mrtim.sasscompiler.grammar.SassParser;
import mrtim.sasscompiler.grammar.SassParser.Builtin_callContext;
import mrtim.sasscompiler.grammar.SassParser.DivideExpressionContext;
import mrtim.sasscompiler.grammar.SassParser.ExpressionListContext;
import mrtim.sasscompiler.grammar.SassParser.ListExpressionContext;
import mrtim.sasscompiler.grammar.SassParser.MinusExpressionContext;
import mrtim.sasscompiler.grammar.SassParser.MultiExpressionListContext;
import mrtim.sasscompiler.grammar.SassParser.MultiplyExpressionContext;
import mrtim.sasscompiler.grammar.SassParser.ParameterContext;
import mrtim.sasscompiler.grammar.SassParser.Parameter_listContext;
import mrtim.sasscompiler.grammar.SassParser.ParenExpressionContext;
import mrtim.sasscompiler.grammar.SassParser.PlusExpressionContext;
import mrtim.sasscompiler.grammar.SassParser.ValueContext;
import org.antlr.v4.runtime.ParserRuleContext;
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
    public ExpressionValue visitExpressionList(ExpressionListContext ctx) {
        return visitListOfExpressions(ctx.expression());
    }

    private ExpressionValue visitListOfExpressions(List<? extends ParserRuleContext> nodes) {
        if (nodes.size() == 1) {
            return visit(nodes.get(0));
        }
        else {
            List<ExpressionValue> expressions = new ArrayList<>();
            for (int i=0; i< nodes.size(); i++) {
                expressions.add(visit(nodes.get(i)));
            }
            return new ListExpressionValue(expressions);
        }
    }

    @Override
    public ExpressionValue visitMultiExpressionList(@NotNull MultiExpressionListContext ctx) {
        return new MultListExpressionValue(visit(ctx.expression_list(0)), visit(ctx.expression_list(1)));
    }

    @Override
    public ExpressionValue visitParameter_list(@NotNull Parameter_listContext ctx) {
        return visitListOfExpressions(ctx.parameter());
    }

    @Override
    public ExpressionValue visitParameter(@NotNull ParameterContext ctx) {
        return visit(ctx.value());
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
        else if (ctx.NUMBER() != null) {
            return numberExpression(ctx.getText());
        }
        else if (ctx.DIMENSION() != null) {
            return dimensionExpression(ctx.getText());
        }
        else if (ctx.PERCENTAGE() != null) {
            return percentageExpression(ctx.getText());
        }
        else if (ctx.colour() != null) {
            return new ColourExpressionValue(ctx.getText());
        }
        else if (ctx.builtin_call() != null) {
            Builtin_callContext callContext = ctx.builtin_call();
            String function = callContext.IDENTIFIER().getText();
            Parameters params = extractParameters(callContext.parameter_list());
            return Builtins.call(function, params);
        }
        else {
            return new StringExpressionValue(ctx.getText());
        }
    }

    private Parameters extractParameters(Parameter_listContext parameter_listContext) {
        return new ParameterExtractionVisitor(this).visit(parameter_listContext);
    }

    private ExpressionValue markAsEvaluated(ExpressionValue result) {
        if (result instanceof NumberExpressionValue) {
            return new NumberExpressionValue(((NumberExpressionValue) result).bigDecimalValue(), true);
        }
        else {
            return result;
        }
    }

    private NumberExpressionValue numberExpression(String number) {
        return new NumberExpressionValue(new BigDecimal(number), false);
    }

    private PercentageExpressionValue percentageExpression(String percentage) {
        return new PercentageExpressionValue(numberExpression(percentage.substring(0, percentage.length()-1)));
    }

    private DimensionExpressionValue dimensionExpression(String dimension) {
        return new DimensionExpressionValue(numberExpression(dimension.substring(0, dimension.length()-2)), dimension.substring(dimension.length()-2));
    }

}
