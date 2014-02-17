package mrtim.sasscompiler.expr;

public class DivisionExpression extends AbstractExpressionValue {

    private final ExpressionValue numerator;
    private final ExpressionValue denominator;

    public DivisionExpression(ExpressionValue numerator, ExpressionValue denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    public ExpressionValue evaluate() {
        return numerator.operate(Operator.DIVIDE, denominator);
    }

    @Override
    public ExpressionValue operate(Operator operator, ExpressionValue other) {
        return evaluate().operate(operator, other);
    }

    @Override
    public String stringValue() {
        return numerator.stringValue() + "/" + denominator.stringValue();
    }
}
