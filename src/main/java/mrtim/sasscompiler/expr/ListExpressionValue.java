package mrtim.sasscompiler.expr;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

public class ListExpressionValue extends AbstractExpressionValue {

    private final ImmutableList<ExpressionValue> expressions;

    public ListExpressionValue(List<ExpressionValue> expressions) {
        this.expressions = ImmutableList.copyOf(expressions);
    }

    @Override
    public String stringValue() {
        return StringUtils.join(FluentIterable.from(expressions).transform(new Function<ExpressionValue, String>() {
            @Override
            public String apply(ExpressionValue input) {
                return input.stringValue();
            }
        }), " ");
    }

    public ExpressionValue prepend(ExpressionValue numberExpressionValue) {
        ExpressionValue head = expressions.get(0);
        if (head instanceof DivisionExpression) {
            head = ((DivisionExpression) head).evaluate();
        }
        List<ExpressionValue> newList = new LinkedList<>();
        newList.add(new StringExpressionValue(numberExpressionValue.stringValue() + head.stringValue()));
        newList.addAll(expressions.subList(1, expressions.size()));
        return new ListExpressionValue(newList);
    }

    public List<ExpressionValue> expressions() {
        return expressions;
    }
}
