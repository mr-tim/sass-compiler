package mrtim.sasscompiler.expr;

public class StringExpressionValue extends AbstractExpressionValue {

    private String value;

    public StringExpressionValue(String value) {
        this.value = value;
    }

    @Override
    public ExpressionValue operate(Operator operator, ExpressionValue other) {
        return throwUnsupportedOperation(operator, other);
    }

    @Override
    public String stringValue() {
        return value;
    }
}
