package mrtim.sasscompiler;

import com.google.common.base.Objects;
import mrtim.sasscompiler.expr.ExpressionValue.Operator;

public class Color {

    private final int r;
    private final int g;
    private final int b;
    private final double alpha;

    public Color(int r, int g, int b) {
        this(r, g, b, 1.0);
    }

    public Color(int r, int g, int b, double alpha) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.alpha = alpha;
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    public double getAlpha() {
        return alpha;
    }

    public boolean hasAlpha() {
        return alpha != 1.0;
    }

    public String asHex() {
        if (!hasAlpha()) {
            return "#" + hex(r) + hex(g) + hex(b);
        }
        else {
            return String.format("rgba(%s, %s, %s, %s)", r, g, b, alpha);
        }
    }

    private String hex(int value) {
        String s = Integer.toHexString(value);
        if (s.length() < 2) {
            s = "0" + s;
        }
        return s;
    }

    public Color addToComponents(Operator operator, int i) {
        return new Color(operateOnComponent(operator, r, i), operateOnComponent(operator, g, i), operateOnComponent(operator, b, i));
    }

    private int operateOnComponent(Operator operator, int component, int operand) {
        switch (operator) {
            case MULTIPLY:
                return truncate(component*operand);
            case DIVIDE:
                return truncate(component/operand);
            case ADD:
                return truncate(component+operand);
            case SUBTRACT:
                return truncate(component-operand);
            default:
                throw new IllegalArgumentException("Unsupported color component operation: " + operator);
        }
    }

    public Color addToComponents(Operator operator, Color o) {
        int scale = operator == Operator.SUBTRACT ? -1 : 1;
        double alpha = (hasAlpha() || o.hasAlpha())? truncateA(this.alpha+scale*o.getAlpha()) : 1.0;
        return new Color(truncate(r+scale*o.getR()), truncate(g+scale*o.getG()), truncate(b+scale*o.getB()), alpha);
    }

    private int truncate(int i) {
        if (i < 0) {
            i = 0;
        }
        else if (i > 255) {
            i = 255;
        }
        return i;
    }

    private double truncateA(double a) {
        if (a < 0.0) {
            a = 0.0;
        }
        else if (a > 1.0) {
            a = 1.0;
        }
        return a;
    }

    public String toString() {
        return Objects.toStringHelper(this)
                .add("r", r)
                .add("g", g)
                .add("b", b)
                .add("a", alpha)
                .toString();
    }
}
