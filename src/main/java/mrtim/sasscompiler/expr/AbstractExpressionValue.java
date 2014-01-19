package mrtim.sasscompiler.expr;

import com.google.common.base.Objects;

public abstract class AbstractExpressionValue implements ExpressionValue {

    @Override
    public ExpressionValue operate(Operator operator, ExpressionValue other) {
        return throwUnsupportedOperation(operator, other);
    }

    protected ExpressionValue throwUnsupportedOperation(Operator operator, ExpressionValue other) {
        throw new UnsupportedOperationException(operationDescription(operator, other));
    }

    protected String operationDescription(Operator operator, ExpressionValue other) {
        return toString() + " " + operator.operator() + " " + other.toString();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue(stringValue()).toString();
    }
}
