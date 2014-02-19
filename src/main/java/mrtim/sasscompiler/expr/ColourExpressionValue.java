package mrtim.sasscompiler.expr;

import mrtim.sasscompiler.Color;
import mrtim.sasscompiler.Colors;

public class ColourExpressionValue extends AbstractExpressionValue {

    private final String original;
    private final boolean evaluate;

    public ColourExpressionValue(String colour) {
        original = colour;
        evaluate = false;
    }

    public ColourExpressionValue(String colour, boolean evaluate) {
        original = colour;
        this.evaluate = evaluate;
    }

    @Override
    protected ExpressionValue operateOnString(Operator operator, StringExpressionValue other) {
        Color c = Colors.resolve(original);
        return new StringExpressionValue(c.asHex()).operate(operator, other);
    }

    @Override
    protected ExpressionValue operateOnNumber(Operator operator, NumberExpressionValue other) {
        Color c = Colors.resolve(original);
        c = c.addToComponents(operator, other.bigDecimalValue().intValue());
        return new ColourExpressionValue(c.asHex(), true);
    }

    @Override
    protected ExpressionValue operateOnColour(Operator operator, ColourExpressionValue other) {
        Color c = Colors.resolve(original);
        Color o = Colors.resolve(other.stringValue());
        c = c.addToComponents(operator, o);
        return new ColourExpressionValue(c.asHex(), true);
    }

    @Override
    public String stringValue() {
        if (evaluate) {
            return evaluateColour();
        }
        else {
            return original;
        }
    }

    private String evaluateColour() {
        Color color = Colors.resolve(original);
        String colourName = Colors.colourName(color);
        if (colourName != null) {
            return colourName;
        }
        else {
            return color.asHex();
        }
    }
}
