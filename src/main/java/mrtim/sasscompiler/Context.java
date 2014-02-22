package mrtim.sasscompiler;

import mrtim.sasscompiler.expr.ExpressionValue;
import mrtim.sasscompiler.grammar.SassLexer;
import mrtim.sasscompiler.grammar.SassParser;
import mrtim.sasscompiler.grammar.SassParser.Sass_fileContext;
import mrtim.sasscompiler.output.CompressedOutputVisitor;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Context {

    public static class Builder {
        private String entryPoint;

        public Builder entryPoint(String entryPoint) {
            this.entryPoint = entryPoint;
            return this;
        }

        public Context build() throws SassCompilationError {
            return new Context(this);
        }
    }

    private String entryPoint;
    private ParseTreeProperty<String> expandedSelectors = new ParseTreeProperty<>();
    private ParseTreeProperty<ExpressionValue> evaluatedExpressions = new ParseTreeProperty<>();
    private ParseTreeProperty<MixinScopeInitialiser> mixinScopeInitialisers = new ParseTreeProperty<>();

    private Context(Builder builder) throws SassCompilationError {
        this.entryPoint = builder.entryPoint;
    }

    private File resolveFromSearchPath(String filename) throws SassCompilationError {
        File resolved = null;
        if (filename.startsWith("url")) {
            //TODO: handle url imports
        }
        else {
            if (filename.startsWith("\"") && filename.endsWith("\"")) {
                filename = filename.substring(1, filename.length()-1);
            }
            File f = new File(filename);
            if (f.exists()) {
                resolved = f;
            }
            else {
                File parent = new File(entryPoint).getParentFile();
                if (!(filename.endsWith(".scss") || filename.endsWith(".css"))) {
                    //TODO: handle relative paths to partials properly
                    filename = "_" + filename + ".scss";
                }
                f = new File(parent, filename);
                if (f.exists()) {
                    resolved = f;
                }
            }
        }
        if (resolved == null) {
            throw new SassCompilationError("Unable to resolve import: " + filename);
        }
        return resolved;
    }

    public Sass_fileContext resolveAndParseImport(String importFilename) {
        File importFile = resolveFromSearchPath(importFilename);
        try {
            return buildParseTree(importFile);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String compileFile() throws IOException {
        String result = null;
        ParseTree parsedSources = collectAndParseSources(resolveFromSearchPath(entryPoint));
        preprocess(parsedSources);
        expandSources(parsedSources);
        outputCompiledSources(entryPoint, parsedSources);
        return result;
    }

    private ParseTree collectAndParseSources(File f) throws IOException {
        ParseTree tree = buildParseTree(f);
        new ImportVisitor(this).visit(tree);
        return tree;
    }

    private void preprocess(ParseTree parseTree) {
        new MixinPreprocessVisitor(mixinScopeInitialisers).visit(parseTree);
    }

    private void expandSources(ParseTree tree) {
        new ExpansionVisitor(expandedSelectors, evaluatedExpressions, mixinScopeInitialisers).visit(tree);
    }

    private Sass_fileContext buildParseTree(File f) throws IOException {
        ANTLRFileStream in = new ANTLRFileStream(f.getAbsolutePath());
        SassLexer lexer = new SassLexer(in);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SassParser parser = new SassParser(tokens);
        parser.setErrorHandler(new BailErrorStrategy());
        parser.setBuildParseTree(true);
        return parser.sass_file();
    }

    private void outputCompiledSources(String entryPoint, ParseTree parseTree) throws IOException {
        String outputFilename = entryPoint.replaceFirst("\\.scss$", ".css");
        FileWriter fw = new FileWriter(outputFilename);
        PrintWriter out = new PrintWriter(fw);
        String output = getCompiledOutput(parseTree);
        out.print(output);
        out.close();
        fw.close();
    }

    private String getCompiledOutput(ParseTree parseTree) {
        CompressedOutputVisitor visitor = new CompressedOutputVisitor(expandedSelectors, evaluatedExpressions);
        visitor.visit(parseTree);
        return visitor.getOutput();
    }
}
