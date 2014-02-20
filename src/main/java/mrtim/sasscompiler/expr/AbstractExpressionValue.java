package mrtim.sasscompiler.expr;

import com.google.common.base.Objects;

public abstract class AbstractExpressionValue implements ExpressionValue {

    @Override
    public ExpressionValue operate(Operator operator, ExpressionValue other) {
        if (other instanceof NumberExpressionValue) {
            return operateOnNumber(operator, (NumberExpressionValue) other);
        }
        else if (other instanceof StringExpressionValue) {
            return operateOnString(operator, (StringExpressionValue) other);
        }
        else if (other instanceof ListExpressionValue) {
            return operateOnList(operator, (ListExpressionValue) other);
        }
        else if (other instanceof DimensionExpressionValue) {
            return operateOnDimension(operator, (DimensionExpressionValue) other);
        }
        else if (other instanceof DivisionExpression) {
            return operateOnDivision(operator, (DivisionExpression) other);
        }
        else if (other instanceof ColourExpressionValue) {
            return operateOnColour(operator, (ColourExpressionValue) other);
        }
        else if (other instanceof PercentageExpressionValue) {
            return operateOnPercentage(operator, (PercentageExpressionValue) other);
        }
        return throwUnsupportedOperation(operator, other);
    }

    protected ExpressionValue operateOnList(Operator operator, ListExpressionValue other) {
        return throwUnsupportedOperation(operator, other);
    }

    protected ExpressionValue operateOnString(Operator operator, StringExpressionValue other) {
        return throwUnsupportedOperation(operator, other);
    }

    protected ExpressionValue operateOnNumber(Operator operator, NumberExpressionValue other) {
        return throwUnsupportedOperation(operator, other);
    }

    protected ExpressionValue operateOnDimension(Operator operator, DimensionExpressionValue other) {
        return throwUnsupportedOperation(operator, other);
    }

    protected ExpressionValue operateOnDivision(Operator operator, DivisionExpression other) {
        return operate(operator, other.evaluate());
    }

    protected ExpressionValue operateOnColour(Operator operator, ColourExpressionValue other) {
        return throwUnsupportedOperation(operator, other);
    }

    protected ExpressionValue operateOnPercentage(Operator operator, PercentageExpressionValue other) {
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
