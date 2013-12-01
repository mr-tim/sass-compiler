package mrtim.sasscompiler;

public class SassCompilationError extends RuntimeException {
    public SassCompilationError(String message) {
        super(message);
    }
}
