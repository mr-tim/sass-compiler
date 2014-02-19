package mrtim.sasscompiler;

import mrtim.sasscompiler.builtins.Parameters;
import mrtim.sasscompiler.expr.ColourExpressionValue;
import mrtim.sasscompiler.expr.ExpressionValue;
import mrtim.sasscompiler.expr.ListExpressionValue;
import mrtim.sasscompiler.expr.NumberExpressionValue;

import java.util.Map;

public class Builtins {

    public static ExpressionValue call(String function, Parameters parameters) {
        switch (function) {
            case "rgb":
                return rgb(parameters.getParamMap(new String[] {"red", "green", "blue"}));
            case "rgba":
                String[] paramNames;
                if (parameters.count() == 2) {
                    paramNames = new String[] {"color", "alpha"};
                }
                else {
                    paramNames = new String[] {"red", "green", "blue", "alpha"};
                }
                Map<String, String> paramValues = parameters.getParamMap(paramNames);
                return rgba(paramValues);
            default:
                throw new IllegalArgumentException("Unsupported builtin function: '" + function + "'");
        }
    }

    private static ExpressionValue rgba(Map<String, String> paramValues) {
        Color color;
        if (paramValues.containsKey("color")) {
            color = Colors.resolve(paramValues.get("color"));
        }
        else {
            color = extractColor(paramValues);
        }
        return new ColourExpressionValue(extractColor(paramValues).asHex(), true);
    }

    private static ExpressionValue rgb(Map<String, String> paramValues) {
        return new ColourExpressionValue(extractColor(paramValues).asHex(), true);
    }

    private static Color extractColor(Map<String, String> paramValues) {
        int r = asInt(paramValues.get("red"));
        int g = asInt(paramValues.get("green"));
        int b = asInt(paramValues.get("blue"));
        return new Color(r, g, b);
    }

    private static int asInt(String value) {
        return value == null? 0 : Integer.valueOf(value);
    }

}
