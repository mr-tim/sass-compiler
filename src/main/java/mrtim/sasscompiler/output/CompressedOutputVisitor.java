package mrtim.sasscompiler.output;

import com.google.common.base.Predicate;
import mrtim.sasscompiler.BaseVisitor;
import mrtim.sasscompiler.grammar.SassParser;
import mrtim.sasscompiler.grammar.SassParser.ValueContext;
import mrtim.sasscompiler.grammar.SassParser.VariableContext;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

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

    @Override
    public Void visitRuleset(SassParser.RulesetContext ctx) {
        int indentBefore = indent;
        if (hasNonHoistable(ctx.block_body())) {
            newLine();
            buffer.append(expandedSelectors.get(ctx.selector_list()));
            buffer.append(" ");
            buffer.append("{");
            indent();
            visitChildrenWhere(new Predicate<ParseTree>() {
                @Override
                public boolean apply(ParseTree tree) {
                    return tree instanceof SassParser.AssignmentContext;
                }
            }, ctx.block_body());
            buffer.append("}");
        }

        if (hasHoistable(ctx.block_body())) {
            visitChildrenWhere(new Predicate<ParseTree>() {
                @Override
                public boolean apply(ParseTree tree) {
                    return (tree instanceof SassParser.RulesetContext || tree instanceof SassParser.Selector_listContext);
                }
            }, ctx.block_body());
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
        return containsChildrenSatisfying(new Predicate<ParseTree>() {
            @Override
            public boolean apply(ParseTree tree) {
                return tree instanceof SassParser.AssignmentContext;
            }
        }, tree);
    }

    private boolean hasHoistable(ParseTree tree) {
        return containsChildrenSatisfying(new Predicate<ParseTree>() {
            @Override
            public boolean apply(ParseTree tree) {
                return (tree instanceof SassParser.RulesetContext || tree instanceof SassParser.Selector_listContext);
            }
        }, tree);
    }

    @Override
    public Void visitAssignment(SassParser.AssignmentContext ctx) {
        newLine();
        buffer.append(ctx.css_identifier().getText());
        buffer.append(": ");
        visitAsList(ctx.value_list().value(), " ", "; ");
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
}
