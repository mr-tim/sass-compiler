package mrtim.sasscompiler.expr;

import mrtim.sasscompiler.Color;
import mrtim.sasscompiler.Colors;

public class ColourExpressionValue extends AbstractExpressionValue {

    private final String original;
    private final boolean evaluate;
    private final Color color;

    public ColourExpressionValue(String colour) {
        original = colour;
        evaluate = false;
        this.color = Colors.resolve(original);
    }

    public ColourExpressionValue(String colour, boolean evaluate) {
        original = colour;
        this.evaluate = evaluate;
        this.color = Colors.resolve(original);
    }

    public ColourExpressionValue(Color color) {
        original = color.asHex();
        evaluate = true;
        this.color = color;
    }

    @Override
    protected ExpressionValue operateOnString(Operator operator, StringExpressionValue other) {
        return new StringExpressionValue(color.asHex()).operate(operator, other);
    }

    @Override
    protected ExpressionValue operateOnNumber(Operator operator, NumberExpressionValue other) {
        Color updatedColor = color.addToComponents(operator, other.bigDecimalValue().intValue());
        return new ColourExpressionValue(updatedColor);
    }

    @Override
    protected ExpressionValue operateOnColour(Operator operator, ColourExpressionValue other) {
        Color o = Colors.resolve(other.stringValue());
        Color updatedColor = color.addToComponents(operator, o);
        return new ColourExpressionValue(updatedColor);
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
        String colourName = Colors.colourName(color);
        if (colourName != null) {
            return colourName;
        }
        else {
            return color.asHex();
        }
    }
}
