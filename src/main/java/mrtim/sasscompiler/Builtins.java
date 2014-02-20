package mrtim.sasscompiler;

import mrtim.sasscompiler.builtins.Parameters;
import mrtim.sasscompiler.expr.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Builtins {

    public static ExpressionValue call(String function, Parameters parameters) {
        Map<String, String> paramValues;
        switch (function) {
            case "rgb":
                return rgb(getParamMap(parameters, "red", "green", "blue"));
            case "rgba":
                if (parameters.count() == 2) {
                    paramValues = getParamMap(parameters, "color", "alpha");
                }
                else {
                    paramValues = getParamMap(parameters, "red", "green", "blue", "alpha");
                }
                return rgba(paramValues);
            case "red":
                return red(getParamMap(parameters, "color"));
            case "green":
                return green(getParamMap(parameters, "color"));
            case "blue":
                return blue(getParamMap(parameters, "color"));
            case "invert":
                return invert(getParamMap(parameters, "color"));
            case "mix":
                if (parameters.count() == 2) {
                    paramValues = new HashMap<>(getParamMap(parameters, "color1", "color2"));
                    paramValues.put("weight", "0.5");
                    paramValues = Collections.unmodifiableMap(paramValues);
                }
                else {
                    paramValues = getParamMap(parameters, "color1", "color2", "weight");
                }
                return mix(paramValues);

            default:
                throw new IllegalArgumentException("Unsupported builtin function: '" + function + "'");
        }
    }

    private static Map<String, String> getParamMap(Parameters parameters, String... paramNames) {
        return parameters.getParamMap(paramNames);
    }

    private static ExpressionValue rgba(Map<String, String> paramValues) {
        Color color;
        if (paramValues.containsKey("color")) {
            color = Colors.resolve(paramValues.get("color"), Double.valueOf(paramValues.get("alpha")));
        }
        else {
            color = extractColor(paramValues);
        }
        return new ColourExpressionValue(color);
    }

    private static ExpressionValue rgb(Map<String, String> paramValues) {
        return new ColourExpressionValue(extractColor(paramValues).asHex(), true);
    }

    private static ExpressionValue red(Map<String, String> paramValues) {
        return new NumberExpressionValue(BigDecimal.valueOf(Colors.resolve(paramValues.get("color")).getR()), true);
    }

    private static ExpressionValue green(Map<String, String> paramValues) {
        return new NumberExpressionValue(BigDecimal.valueOf(Colors.resolve(paramValues.get("color")).getG()), true);
    }

    private static ExpressionValue blue(Map<String, String> paramValues) {
        return new NumberExpressionValue(BigDecimal.valueOf(Colors.resolve(paramValues.get("color")).getB()), true);
    }

    private static ExpressionValue invert(Map<String, String> paramValues) {
        Color color = Colors.resolve(paramValues.get("color"));
        Color result = new Color(255-color.getR(), 255-color.getG(), 255-color.getB(), color.getAlpha());
        return new ColourExpressionValue(result);
    }

    private static ExpressionValue mix(Map<String, String> paramValues) {
        Color color1 = Colors.resolve(paramValues.get("color1"));
        Color color2 = Colors.resolve(paramValues.get("color2"));

        double percentage = Double.valueOf(paramValues.get("weight"));
        double w = 2*percentage - 1;
        double a = color1.getAlpha()-color2.getAlpha();

        double weight1 = (((w * a == -1) ? w : (w + a)/(1 + w*a)) + 1)/2.0;
        double weight2 = 1 - weight1;

        Color result = new Color((int)(weight1*color1.getR()+weight2*color2.getR()),
                                (int)(weight1*color1.getG()+weight2*color2.getG()),
                                (int)(weight1*color1.getB()+weight2*color2.getB()),
                                color1.getAlpha()*percentage + color2.getAlpha()*percentage);
        return new ColourExpressionValue(result);
    }

    private static Color extractColor(Map<String, String> paramValues) {
        int r = asInt(paramValues.get("red"));
        int g = asInt(paramValues.get("green"));
        int b = asInt(paramValues.get("blue"));
        double alpha = paramValues.containsKey("alpha")? Double.valueOf(paramValues.get("alpha")) : 1.0;
        return new Color(r, g, b, alpha);
    }

    private static int asInt(String value) {
        return value == null? 0 : Integer.valueOf(value);
    }

}
