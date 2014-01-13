package mrtim.sasscompiler;

import mrtim.sasscompiler.grammar.SassLexer;
import mrtim.sasscompiler.grammar.SassParser.Selector_combinationContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

public class SelectorCombinationVisitor extends BaseVisitor<String> {

    @Override
    public String visitSelector_combination(Selector_combinationContext ctx) {
        StringBuffer buffer = new StringBuffer();
        for (int i=0; i<ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child.getChildCount() > 0) {
                if (i > 0) {
                    buffer.append(" ");
                }
                buffer.append(visit(child));
            }
        }
        return buffer.toString();
    }

    @Override
    public String visitTerminal(TerminalNode node) {
        switch (node.getSymbol().getType()) {
            case SassLexer.COMMENT:
                return "";
            case SassLexer.COMMA:
                return ", ";
            default:
                return node.getText();
        }
    }

    @Override
    protected String aggregateResult(String aggregate, String nextResult) {
        if (aggregate == null) {
            aggregate = "";
        }
        if (nextResult == null) {
            nextResult = "";
        }
        return aggregate + nextResult;
    }
}
