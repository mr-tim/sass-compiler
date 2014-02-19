package mrtim.sasscompiler;

import mrtim.sasscompiler.expr.ColourExpressionValue;
import mrtim.sasscompiler.expr.ExpressionValue;
import mrtim.sasscompiler.expr.ListExpressionValue;
import mrtim.sasscompiler.expr.NumberExpressionValue;

public class Builtins {

    public static ExpressionValue call(String function, ListExpressionValue parameters) {
        switch (function) {
            case "rgb":
                return rgb(parameters);
            default:
                throw new IllegalArgumentException("Unsupported builtin function");
        }
    }

    private static ExpressionValue rgb(ListExpressionValue parameters) {
        int r = asInt(parameters.expressions().get(0));
        int g = asInt(parameters.expressions().get(1));
        int b = asInt(parameters.expressions().get(2));
        return new ColourExpressionValue(new Color(r, g, b).asHex());
    }

    private static int asInt(ExpressionValue expressionValue) {
        NumberExpressionValue num = (NumberExpressionValue)expressionValue;
        return num.bigDecimalValue().intValue();
    }

}
