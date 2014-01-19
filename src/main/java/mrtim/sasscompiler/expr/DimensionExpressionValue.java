package mrtim.sasscompiler.expr;

public class DimensionExpressionValue extends AbstractExpressionValue {

    private final NumberExpressionValue value;
    private final String unit;

    public DimensionExpressionValue(NumberExpressionValue value, String unit) {
        this.value = value;
        this.unit = unit;
    }

    @Override
    protected ExpressionValue operateOnDimension(Operator operator, DimensionExpressionValue other) {
        //should probably check/convert units
        switch (operator) {
            case DIVIDE:
                return value.operate(operator, other.value);
            default:
                return new DimensionExpressionValue((NumberExpressionValue)value.operate(operator, other.value), other.unit);
        }
    }

    @Override
    protected ExpressionValue operateOnNumber(Operator operator, NumberExpressionValue other) {
        return new DimensionExpressionValue((NumberExpressionValue)value.operate(operator, other), unit);
    }

    @Override
    public String stringValue() {
        return value.stringValue()+unit;
    }

    public NumberExpressionValue value() {
        return value;
    }

    public String unit() {
        return unit;
    }
}
