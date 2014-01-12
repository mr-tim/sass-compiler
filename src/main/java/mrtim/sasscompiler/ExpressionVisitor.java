package mrtim.sasscompiler;

import mrtim.sasscompiler.grammar.SassParser.ValueContext;
import mrtim.sasscompiler.grammar.SassParser.Expression_listContext;

public class ExpressionVisitor extends BaseVisitor<Void> {

    private Scope scope;
    private StringBuffer buffer = new StringBuffer();

    public ExpressionVisitor(Scope scope) {
        this.scope = scope;
    }

    @Override
    public Void visitExpression_list(Expression_listContext ctx) {
        visitAsList(buffer, ctx.expression(), " ", "");
        return null;
    }

    @Override
    public Void visitValue(ValueContext ctx) {
        if (ctx.VARIABLE() != null) {
            buffer.append(scope.get(ctx.VARIABLE().getText()));
        }
        else {
            buffer.append(ctx.getText());
        }
        return null;
    }

    public String getValue() {
        return buffer.toString();
    }

}
