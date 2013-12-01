package mrtim.sasscompiler;

import com.google.common.base.Objects;

public class Color {

    private final String path;
    private final int line;
    private final int r;
    private final int g;
    private final int b;
    private final double alpha;

    public Color(String path, int line, int r, int g, int b) {
        this(path, line, r, g, b, 1.0);
    }

    public Color(String path, int line, int r, int g, int b, double alpha) {
        this.path = path;
        this.line = line;
        this.r = r;
        this.g = g;
        this.b = b;
        this.alpha = alpha;
    }

    public String getPath() {
        return path;
    }

    public int getLine() {
        return line;
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

    public String toString() {
        return Objects.toStringHelper(this)
                .add("path", path)
                .add("line", line)
                .add("r", r)
                .add("g", g)
                .add("b", b)
                .add("a", alpha)
                .toString();
    }
}
