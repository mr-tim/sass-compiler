package mrtim.sasscompiler.expr;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;

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
}
