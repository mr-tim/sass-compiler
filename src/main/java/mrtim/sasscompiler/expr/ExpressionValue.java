package mrtim.sasscompiler.expr;

public interface ExpressionValue {

    enum Operator {
        ADD("+"),
        SUBTRACT("-"),
        MULTIPLY("*"),
        DIVIDE("/");

        private String operator;

        private Operator(String operator) {
            this.operator = operator;
        }

        public static Operator fromString(String operator) {
            switch (operator) {
                case "+": return ADD;
                case "-": return SUBTRACT;
                case "*": return MULTIPLY;
                case "/": return DIVIDE;
                default:
                    throw new IllegalArgumentException("Unsupported operator: '" + operator + "'");
            }
        }

        public String operator() {
            return operator;
        }
    }

    ExpressionValue operate(Operator operator, ExpressionValue other);
    String stringValue();

}
