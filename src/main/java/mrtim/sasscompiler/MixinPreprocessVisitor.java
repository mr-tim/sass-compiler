package mrtim.sasscompiler;

import mrtim.sasscompiler.grammar.SassBaseVisitor;
import mrtim.sasscompiler.grammar.SassParser;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import java.util.HashMap;
import java.util.Map;

public class MixinPreprocessVisitor extends SassBaseVisitor<Void> {

    private Map<String, SassParser.DefinitionContext> mixins = new HashMap<>();

    ParseTreeProperty<MixinScopeInitialiser> scopeInitialisers;

    public MixinPreprocessVisitor(ParseTreeProperty<MixinScopeInitialiser> scopeInitialisers) {
        this.scopeInitialisers = scopeInitialisers;
    }

    @Override
    public Void visitDefinition(@NotNull SassParser.DefinitionContext ctx) {
        if (ctx.MIXIN_KW() != null) {
            mixins.put(ctx.IDENTIFIER().getText(), ctx);
        }
        return super.visitDefinition(ctx);
    }

    @Override
    public Void visitInclude_statement(@NotNull SassParser.Include_statementContext ctx) {
        SassParser.DefinitionContext mixinDefinition = mixins.get(ctx.IDENTIFIER().getText());
        ParseTree clonedMixinBody = Trees.deepCopy(mixinDefinition.block_body());
        scopeInitialisers.put(clonedMixinBody, new MixinScopeInitialiser(mixinDefinition.parameter_def_list(), ctx.parameter_list()));
        Trees.replace(ctx, clonedMixinBody);
        return null;
    }
}
