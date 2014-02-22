package mrtim.sasscompiler;

import mrtim.sasscompiler.expr.StringExpressionValue;
import mrtim.sasscompiler.grammar.SassParser;

public class MixinScopeInitialiser {

    private final SassParser.Parameter_def_listContext mixinDefParameters;
    private final SassParser.Parameter_listContext includeParameters;

    public MixinScopeInitialiser(SassParser.Parameter_def_listContext mixinDefParameters,
                                 SassParser.Parameter_listContext includeParameters) {
        this.mixinDefParameters = mixinDefParameters;
        this.includeParameters = includeParameters;
    }

    public void initialiseScope(Scope scope) {
        for (SassParser.Variable_defContext variableDef: mixinDefParameters.variable_def()) {
            scope.define(variableDef.VARIABLE().getText(), new StringExpressionValue("xxx"));
        }
    }
}
