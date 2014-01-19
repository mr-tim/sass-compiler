package mrtim.sasscompiler.expr;

public class StringExpressionValue extends AbstractExpressionValue {

    final private String value;

    public StringExpressionValue(String value) {
        this.value = value;
    }

    @Override
    public String stringValue() {
        return value;
    }
}
