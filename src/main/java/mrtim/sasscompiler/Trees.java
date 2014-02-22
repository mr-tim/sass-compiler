package mrtim.sasscompiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class Trees {

    public static void replace(ParserRuleContext node, ParseTree replacement) {
        List<ParseTree> siblings = node.getParent().children;
        int childIndex = siblings.indexOf(node);
        siblings.set(childIndex, replacement);
    }

    public static ParseTree deepCopy(ParseTree parseTree) {
        if (parseTree instanceof ParserRuleContext) {
            return deepCopyRuleContext((ParserRuleContext) parseTree);
        }
        else if (parseTree instanceof TerminalNodeImpl) {
            //instantiate and return a copy
            return new TerminalNodeImpl(((TerminalNodeImpl) parseTree).getSymbol());
        }
        else {
            throw new IllegalArgumentException("Cannot create deep copy of unsupported ParseTree: " + parseTree);
        }
    }

    private static ParserRuleContext deepCopyRuleContext(ParserRuleContext node) {
        List<ParseTree> clonedChildren = new ArrayList<>();
        for (ParseTree child: node.children) {
            clonedChildren.add(deepCopy(child));
        }

        ParserRuleContext clonedNode = cloneNode(node);
        clonedNode.children = clonedChildren;

        return clonedNode;
    }

    private static ParserRuleContext cloneNode(ParserRuleContext node) {
        ParserRuleContext clonedNode;
        try {
            if (node.getClass().getSuperclass().equals(ParserRuleContext.class)) {
                clonedNode = cloneParserRuleContextNode(node, node.getClass());
            }
            else {
                ParserRuleContext parent = cloneParserRuleContextNode(node, (Class<? extends ParserRuleContext>)node.getClass().getSuperclass());
                clonedNode = node.getClass().getConstructor(node.getClass().getSuperclass()).newInstance(parent);
            }
        }
        catch (InstantiationException e) {
            throw new IllegalArgumentException("Could not create new instance of ParserRuleContext " + node.getClass(), e);
        }
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not create new instance of ParserRuleContext " + node.getClass(), e);
        }
        catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Could not create new instance of ParserRuleContext " + node.getClass(), e);
        }
        catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Could not create new instance of ParserRuleContext " + node.getClass(), e);
        }
        clonedNode.copyFrom(node);
        return clonedNode;
    }

    private static ParserRuleContext cloneParserRuleContextNode(ParserRuleContext node, Class<? extends ParserRuleContext> ruleContextClass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return ruleContextClass.getConstructor(ParserRuleContext.class, int.class).newInstance(node.parent, node.invokingState);
    }

}
