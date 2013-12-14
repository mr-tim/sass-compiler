package mrtim.sasscompiler;

import mrtim.sasscompiler.grammar.SassParser.Selector_combinationContext;
import mrtim.sasscompiler.grammar.SassParser.Simple_selectorContext;
import org.antlr.v4.runtime.tree.TerminalNode;

public class SelectorCombinationVisitor extends BaseVisitor<String> {

    @Override
    public String visitSelector_combination(Selector_combinationContext ctx) {
        StringBuffer buffer = new StringBuffer();
        for (int i=0; i<ctx.getChildCount(); i++) {
            if (i > 0) {
                buffer.append(" ");
            }
            buffer.append(visit(ctx.getChild(i)));
        }
        return buffer.toString();
    }

    @Override
    public String visitSimple_selector(Simple_selectorContext ctx) {
        return ctx.getText();
    }

    @Override
    public String visitTerminal(TerminalNode node) {
        return node.getText();
    }
}
