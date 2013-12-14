package mrtim.sasscompiler;

import com.google.common.base.Predicate;
import mrtim.sasscompiler.grammar.SassBaseVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;

public class BaseVisitor<T> extends SassBaseVisitor<T> {

    protected <U extends ParserRuleContext> void visitAsList(StringBuffer buffer, List<U> items, String seperator, String terminal) {
        if (!items.isEmpty()) {
            visit(items.get(0));
            for (int i=1; i<items.size(); i++) {
                buffer.append(seperator);
                visit(items.get(i));
            }
            buffer.append(terminal);
        }
    }

    protected Void visitChildrenWhere(Predicate<ParseTree> predicate, ParseTree tree) {
        for (int i=0; i<tree.getChildCount(); i++) {
            ParseTree child = tree.getChild(i);
            if (predicate.apply(child)) {
                visit(child);
            }
        }
        return null;
    }

    protected boolean containsChildrenSatisfying(Predicate<ParseTree> predicate, ParseTree tree) {
        for (int i=0; i<tree.getChildCount(); i++) {
            if (predicate.apply(tree.getChild(i))) {
                return true;
            }
        }
        return false;
    }


}
