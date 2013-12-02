package mrtim.sasscompiler.output;

import com.google.common.base.Predicate;
import mrtim.sasscompiler.grammar.SassBaseVisitor;
import mrtim.sasscompiler.grammar.SassParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import java.util.List;

public class CompressedOutputVisitor extends SassBaseVisitor<Void> {

    private StringBuffer buffer = new StringBuffer();
    private int indent = 0;
    private static final String INDENTATION = "  ";

    private final ParseTreeProperty<String> expandedSelectors;

    public CompressedOutputVisitor(ParseTreeProperty<String> expandedSelectors) {
        this.expandedSelectors = expandedSelectors;
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
            visitChildrenWhere(isNonHoistable, ctx.block_body());
            buffer.append("}");
        }

        if (hasHoistable(ctx.block_body())) {
            visitChildrenWhere(isHoistable, ctx.block_body());
        }
        indent = indentBefore;
        return null;
    }

    private Void visitChildrenWhere(Predicate<ParseTree> predicate, ParseTree tree) {
        for (int i=0; i<tree.getChildCount(); i++) {
            ParseTree child = tree.getChild(i);
            if (predicate.apply(child)) {
                visit(child);
            }
        }
        return null;
    }

    private boolean hasNonHoistable(ParseTree tree) {
        return containsChildrenSatisfying(isNonHoistable, tree);
    }

    private boolean hasHoistable(ParseTree tree) {
        return containsChildrenSatisfying(isHoistable, tree);
    }

    private boolean containsChildrenSatisfying(Predicate<ParseTree> predicate, ParseTree tree) {
        for (int i=0; i<tree.getChildCount(); i++) {
            if (predicate.apply(tree.getChild(i))) {
                return true;
            }
        }
        return false;
    }

    //should really be storing this in ParseTreeProperty that we populate with a seperate visitor
    private static final Predicate<ParseTree> isNonHoistable = new Predicate<ParseTree>() {
        @Override
        public boolean apply(ParseTree tree) {
            return tree instanceof SassParser.AssignmentContext;
        }
    };

    private static final Predicate<ParseTree> isHoistable = new Predicate<ParseTree>() {
        @Override
        public boolean apply(ParseTree tree) {
            return (tree instanceof SassParser.RulesetContext || tree instanceof SassParser.Selector_listContext);
        }
    };

    @Override
    public Void visitAssignment(SassParser.AssignmentContext ctx) {
        newLine();
        buffer.append(ctx.css_identifier().getText());
        buffer.append(": ");
        appendAsList(ctx.value_list().value(), " ", "; ");
        return null;
    }

    private <T extends ParserRuleContext> void appendAsList(List<T> items, String seperator, String terminal) {
        if (!items.isEmpty()) {
            buffer.append(items.get(0).getText());
            for (int i=1; i<items.size(); i++) {
                buffer.append(seperator);
                buffer.append(items.get(i).getText());
            }
            buffer.append(terminal);
        }
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
