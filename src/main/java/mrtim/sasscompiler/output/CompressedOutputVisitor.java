package mrtim.sasscompiler.output;

import com.google.common.base.Predicate;
import mrtim.sasscompiler.BaseVisitor;
import mrtim.sasscompiler.expr.ExpressionValue;
import mrtim.sasscompiler.grammar.SassLexer;
import mrtim.sasscompiler.grammar.SassParser;
import mrtim.sasscompiler.grammar.SassParser.AssignmentContext;
import mrtim.sasscompiler.grammar.SassParser.DefinitionContext;
import mrtim.sasscompiler.grammar.SassParser.ExpressionListContext;
import mrtim.sasscompiler.grammar.SassParser.Import_statementContext;
import mrtim.sasscompiler.grammar.SassParser.MultiExpressionListContext;
import mrtim.sasscompiler.grammar.SassParser.RulesetContext;
import mrtim.sasscompiler.grammar.SassParser.Selector_listContext;
import mrtim.sasscompiler.grammar.SassParser.VariableContext;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

public class CompressedOutputVisitor extends BaseVisitor<Void> {

    private StringBuffer buffer = new StringBuffer();
    private int indent = 0;
    private static final String INDENTATION = "  ";

    private final ParseTreeProperty<String> expandedSelectors;
    private final ParseTreeProperty<ExpressionValue> expressionValues;

    public CompressedOutputVisitor(ParseTreeProperty<String> expandedSelectors,
                                   ParseTreeProperty<ExpressionValue> expressionValues) {
        this.expandedSelectors = expandedSelectors;
        this.expressionValues = expressionValues;
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
        visit(ctx.expression_list());
        buffer.append(";");
        return null;
    }

    @Override
    public Void visitDefinition(DefinitionContext ctx) {
        if (ctx.MIXIN_KW() != null) {
            //skip definition of mixins
            return null;
        }
        else {
            return super.visitDefinition(ctx);
        }
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

    @Override
    public Void visitExpressionList(@NotNull ExpressionListContext ctx) {
        return outputExpressionValue(ctx);
    }

    @Override
    public Void visitMultiExpressionList(@NotNull MultiExpressionListContext ctx) {
        return outputExpressionValue(ctx);
    }

    public Void outputExpressionValue(ParseTree ctx) {
        buffer.append(expressionValues.get(ctx).stringValue());
        return null;
    }
}
