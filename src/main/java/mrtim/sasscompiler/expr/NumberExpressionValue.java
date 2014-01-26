package mrtim.sasscompiler.expr;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class NumberExpressionValue extends AbstractExpressionValue {

    private final BigDecimal value;
    private final boolean evaluated;

    public NumberExpressionValue(BigDecimal value, boolean evaluated) {
        this.value = value;
        this.evaluated = evaluated;
    }

    @Override
    protected ExpressionValue operateOnNumber(Operator operator, NumberExpressionValue other) {
        switch (operator) {
            case ADD: return new NumberExpressionValue(value.add(other.value), true);
            case SUBTRACT: return new NumberExpressionValue(value.subtract(other.value), true);
            case MULTIPLY: return new NumberExpressionValue(value.multiply(other.value), true);
            case DIVIDE: return new NumberExpressionValue(value.divide(other.value, 10, RoundingMode.HALF_UP), true);
            default:
                return throwUnsupportedOperation(operator, other);
        }
    }

    @Override
    protected ExpressionValue operateOnList(Operator operator, ListExpressionValue other) {
        switch (operator) {
            case ADD: return other.prepend(this);
            default:
                return throwUnsupportedOperation(operator, other);
        }
    }

    @Override
    protected ExpressionValue operateOnDimension(Operator operator, DimensionExpressionValue other) {
        return new DimensionExpressionValue((NumberExpressionValue)operate(operator, other.value()), other.unit());
    }

    @Override
    protected ExpressionValue operateOnString(Operator operator, StringExpressionValue other) {
        return new StringExpressionValue(stringValue()).operate(operator, other);
    }

    @Override
    public String stringValue() {
        DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(4);
        format.setMinimumFractionDigits(0);
        format.setGroupingUsed(false);
        return format.format(value);
    }

    public BigDecimal bigDecimalValue() {
        return value;
    }

    public boolean isEvaluated() {
        return evaluated;
    }
}
