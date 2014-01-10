package mrtim.sasscompiler.output;

import com.google.common.base.Predicate;
import mrtim.sasscompiler.BaseVisitor;
import mrtim.sasscompiler.grammar.SassLexer;
import mrtim.sasscompiler.grammar.SassParser;
import mrtim.sasscompiler.grammar.SassParser.AssignmentContext;
import mrtim.sasscompiler.grammar.SassParser.Import_statementContext;
import mrtim.sasscompiler.grammar.SassParser.RulesetContext;
import mrtim.sasscompiler.grammar.SassParser.Selector_listContext;
import mrtim.sasscompiler.grammar.SassParser.ValueContext;
import mrtim.sasscompiler.grammar.SassParser.VariableContext;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

public class CompressedOutputVisitor extends BaseVisitor<Void> {

    private StringBuffer buffer = new StringBuffer();
    private int indent = 0;
    private static final String INDENTATION = "  ";

    private final ParseTreeProperty<String> expandedSelectors;
    private final ParseTreeProperty<String> variableValues;

    public CompressedOutputVisitor(ParseTreeProperty<String> expandedSelectors,
                                   ParseTreeProperty<String> variableValues) {
        this.expandedSelectors = expandedSelectors;
        this.variableValues = variableValues;
    }

    private static final Predicate<ParseTree> HOISTABLE = new Predicate<ParseTree>() {
        @Override
        public boolean apply(ParseTree tree) {
            return (tree instanceof RulesetContext || tree instanceof Selector_listContext|| tree instanceof Import_statementContext);
        }
    };

    private static final Predicate<ParseTree> NON_HOISTABLE = new Predicate<ParseTree>() {
        @Override
        public boolean apply(ParseTree tree) {
            return tree instanceof AssignmentContext
                    || ((tree instanceof TerminalNode) && ((TerminalNode)tree).getSymbol().getType() == SassLexer.COMMENT);
        }
    };

    @Override
    public Void visitRuleset(SassParser.RulesetContext ctx) {
        int indentBefore = indent;
        if (hasNonHoistable(ctx.block_body())) {
            newLine();
            buffer.append(expandedSelectors.get(ctx.selector_list()));
            buffer.append(" ");
            buffer.append("{");
            indent();
            visitChildrenWhere(NON_HOISTABLE, ctx.block_body());
            buffer.append(" }");
        }

        if (hasHoistable(ctx.block_body())) {
            visitChildrenWhere(HOISTABLE, ctx.block_body());
        }

        indent = indentBefore;
        return null;
    }

    @Override
    public Void visitVariable(VariableContext ctx) {
        //do nothing
        return null;
    }

    @Override
    public Void visitValue(ValueContext ctx) {
        if (ctx.VARIABLE() != null) {
            buffer.append(variableValues.get(ctx));
            return null;
        }
        else {
            buffer.append(ctx.getText());
            return null;
        }
    }

    private boolean hasNonHoistable(ParseTree tree) {
        return containsChildrenSatisfying(NON_HOISTABLE, tree);
    }

    private boolean hasHoistable(ParseTree tree) {
        return containsChildrenSatisfying(HOISTABLE, tree);
    }

    @Override
    public Void visitAssignment(SassParser.AssignmentContext ctx) {
        newLine();
        buffer.append(ctx.css_identifier().getText());
        buffer.append(": ");
        visitAsList(ctx.value_list().value(), " ", ";");
        return null;
    }

    private <T extends ParserRuleContext> void visitAsList(List<T> items, String seperator, String terminal) {
        visitAsList(buffer, items, seperator, terminal);
    }

    private void indent() {
        indent++;
    }

    private void dedent() {
        indent--;
    }

    private void newLine() {
        //avoid newlines at the start of the file
        if (!(indent == 0 && buffer.length() == 0)) {
            buffer.append("\n");
            for (int i=0; i<indent; i++) {
                buffer.append(INDENTATION);
            }
        }
    }

    public String getOutput() {
        return buffer.toString();
    }

    @Override
    public Void visitTerminal(TerminalNode node) {
        if (node.getSymbol().getType() == SassLexer.COMMENT) {
            newLine();
            buffer.append(node.getText());
        }
        return null;
    }
}
