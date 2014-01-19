package mrtim.sasscompiler.expr;

public class PercentageExpressionValue extends AbstractExpressionValue {

    private NumberExpressionValue value;

    public PercentageExpressionValue(NumberExpressionValue value) {
        this.value = value;
    }

    @Override
    public String stringValue() {
        return value.stringValue() + "%";
    }
}
