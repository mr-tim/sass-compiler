package mrtim.sasscompiler.expr;

public class StringExpressionValue extends AbstractExpressionValue {

    final private String value;

    public StringExpressionValue(String value) {
        this.value = value;
    }

    @Override
    protected ExpressionValue operateOnString(Operator operator, StringExpressionValue other) {
        switch (operator) {
            case ADD:
                return new StringExpressionValue(value + other.stringValue());
            default:
                return throwUnsupportedOperation(operator, other);
        }
    }

    @Override
    public String stringValue() {
        return value;
    }
}
