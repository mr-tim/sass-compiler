package mrtim.sasscompiler.expr;

import java.math.BigDecimal;

public class PercentageExpressionValue extends AbstractExpressionValue {

    private NumberExpressionValue value;

    public PercentageExpressionValue(BigDecimal bigDecimalValue) {
        this.value = new NumberExpressionValue(bigDecimalValue, false);
    }

    public PercentageExpressionValue(NumberExpressionValue value) {
        this.value = value;
    }

    @Override
    protected ExpressionValue operateOnPercentage(Operator operator, PercentageExpressionValue other) {
        if (operator == Operator.ADD) {
            return new PercentageExpressionValue(bigDecimalValue().add(other.bigDecimalValue()));
        }
        else if (operator == Operator.SUBTRACT) {
            return new PercentageExpressionValue(bigDecimalValue().subtract(other.bigDecimalValue()));
        }
        else if (operator == Operator.DIVIDE) {
            return new NumberExpressionValue(bigDecimalValue().divide(other.bigDecimalValue()), true);
        }
        else {
            return throwUnsupportedOperation(operator, other);
        }
    }

    @Override
    protected ExpressionValue operateOnNumber(Operator operator, NumberExpressionValue other) {
        if (operator == Operator.ADD) {
            return new PercentageExpressionValue(bigDecimalValue().add(other.bigDecimalValue()));
        }
        else if (operator == Operator.SUBTRACT) {
            return new PercentageExpressionValue(bigDecimalValue().subtract(other.bigDecimalValue()));
        }
        else if (operator == Operator.DIVIDE) {
            return new PercentageExpressionValue(bigDecimalValue().divide(other.bigDecimalValue()));
        }
        else if (operator == Operator.MULTIPLY) {
            return new PercentageExpressionValue(bigDecimalValue().multiply(other.bigDecimalValue()));
        }
        else {
            return throwUnsupportedOperation(operator, other);
        }
    }

    public BigDecimal bigDecimalValue() {
        return value.bigDecimalValue();
    }

    @Override
    public String stringValue() {
        return value.stringValue() + "%";
    }
}
