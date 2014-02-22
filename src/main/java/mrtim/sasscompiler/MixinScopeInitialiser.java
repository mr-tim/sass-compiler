package mrtim.sasscompiler;

import mrtim.sasscompiler.expr.ExpressionValue;
import mrtim.sasscompiler.expr.StringExpressionValue;
import mrtim.sasscompiler.grammar.SassParser;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.*;

public class MixinScopeInitialiser {

    private final SassParser.Parameter_def_listContext mixinDefParameters;
    private final SassParser.Parameter_listContext includeParameters;

    public MixinScopeInitialiser(SassParser.Parameter_def_listContext mixinDefParameters,
                                 SassParser.Parameter_listContext includeParameters) {
        this.mixinDefParameters = mixinDefParameters;
        this.includeParameters = includeParameters;
    }

    public void initialiseScope(Scope scope) {
        List<String> paramNames = positionalParamNames(mixinDefParameters.variable_def());

        int positionalIndex = 0;

        Map<String, ExpressionValue> evaluatedParams = new HashMap<>();
        if (includeParameters != null) {
            for (SassParser.ParameterContext includeParam: includeParameters.parameter()) {
                if (isKeywordParam(includeParam)) {
                    String paramName = includeParam.named_parameter().VARIABLE().getText();
                    paramNames.remove(paramName);
                    evaluatedParams.put(paramName, extractExpressionValue(includeParam, scope));
                }
            }
            for (SassParser.ParameterContext includeParam: includeParameters.parameter()) {
                if (!isKeywordParam(includeParam)) {
                    evaluatedParams.put(paramNames.get(positionalIndex++), extractExpressionValue(includeParam, scope));
                }
            }
        }

        for (SassParser.Variable_defContext variableDef: mixinDefParameters.variable_def()) {
            String variableName = variableDef.VARIABLE().getText();
            if (!evaluatedParams.containsKey(variableName)) {
                evaluatedParams.put(variableName, evaluateInScope(scope, variableDef.expression_list()));
            }
        }

        for (Map.Entry<String, ExpressionValue> e: evaluatedParams.entrySet()) {
            scope.define(e.getKey(), e.getValue());
        }
    }

    private boolean isKeywordParam(SassParser.ParameterContext includeParam) {
        return includeParam.named_parameter() != null;
    }

    private List<String> positionalParamNames(List<SassParser.Variable_defContext> variableDefs) {
        List<String> paramNames = new ArrayList<>();
        for (SassParser.Variable_defContext variableDef: variableDefs) {
            paramNames.add(variableDef.VARIABLE().getText());
        }
        return paramNames;
    }

    private ExpressionValue extractExpressionValue(SassParser.ParameterContext includeParam, Scope scope) {
        if (includeParam.IDENTIFIER() != null) {
            return new StringExpressionValue(includeParam.IDENTIFIER().getText());
        }
        else if (includeParam.value() != null) {
            return evaluateInScope(scope, includeParam.value());
        }
        else if (includeParam.named_parameter() != null) {
            return evaluateInScope(scope, includeParam.named_parameter().expression_list());
        }
        else {
            throw new IllegalArgumentException("Could not extract parameter value from ParameterContext: " + includeParam.getText());
        }
    }

    private ExpressionValue evaluateInScope(Scope scope, ParseTree expression) {
        return new ExpressionVisitor(scope).visit(expression);
    }
}
