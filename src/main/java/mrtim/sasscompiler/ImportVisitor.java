package mrtim.sasscompiler;

import mrtim.sasscompiler.grammar.SassParser.Import_statementContext;
import mrtim.sasscompiler.grammar.SassParser.Import_targetContext;
import mrtim.sasscompiler.grammar.SassParser.Sass_fileContext;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

public class ImportVisitor extends BaseVisitor<Void> {

    private Context context;

    public ImportVisitor(Context context) {
        this.context = context;
    }

    @Override
    public Void visitImport_statement(Import_statementContext ctx) {
        List<ParseTree> contextsToInsert = collectParserRuleContextsFromImports(ctx);
        emptyContext(ctx);
        addChildrenToContext(contextsToInsert, ctx);
        visitChildren(ctx);
        return null;
    }

    private List<ParseTree> collectParserRuleContextsFromImports(Import_statementContext ctx) {
        List<ParseTree> contextsToInsert = new ArrayList<>();
        for (Import_targetContext importTarget: ctx.import_target()) {
            Sass_fileContext sassFileContext = context.resolveAndParseImport(importTarget.getText());
            for (int i=0; i<sassFileContext.getChildCount(); i++) {
                contextsToInsert.add(sassFileContext.getChild(i));
            }
        }
        return contextsToInsert;
    }

    private void emptyContext(Import_statementContext ctx) {
        while (ctx.getChildCount() > 0) {
            ctx.removeLastChild();
        }
    }

    private void addChildrenToContext(List<ParseTree> children, ParserRuleContext parentContext) {
        for (int i=0; i<children.size(); i++) {
            ParseTree child = children.get(i);
            if (child instanceof TerminalNode) {
                parentContext.addChild((TerminalNode)child);
            }
            else if (child instanceof RuleContext) {
                parentContext.addChild((RuleContext)child);
            }
            else if (child instanceof Token) {
                parentContext.addChild((Token)child);
            }
        }
    }
}
