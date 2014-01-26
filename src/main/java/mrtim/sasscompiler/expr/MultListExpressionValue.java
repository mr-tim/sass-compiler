package mrtim.sasscompiler.expr;

public class MultListExpressionValue extends AbstractExpressionValue {

    private ExpressionValue left;
    private ExpressionValue right;

    public MultListExpressionValue(ExpressionValue left, ExpressionValue right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String stringValue() {
        return left.stringValue() + ", " + right.stringValue();
    }
}
