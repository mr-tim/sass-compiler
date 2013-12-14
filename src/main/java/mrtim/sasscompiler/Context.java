package mrtim.sasscompiler;

import mrtim.sasscompiler.grammar.SassLexer;
import mrtim.sasscompiler.grammar.SassParser;
import mrtim.sasscompiler.output.CompressedOutputVisitor;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class Context {

    private Map<File,ParseTree> parsedSources;

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
    private Queue<File> toCompile = new ArrayDeque<>();
    private ParseTreeProperty<String> expandedSelectors = new ParseTreeProperty<>();
    private ParseTreeProperty<String> variableValues = new ParseTreeProperty<>();

    private Context(Builder builder) throws SassCompilationError {
        this.entryPoint = builder.entryPoint;
        addFile(entryPoint);
    }

    public void addFile(String filename) throws SassCompilationError {
        //resolve the filename to a File, and add it to the queue of files to process
        addFile(resolveFromSearchPath(filename));
    }

    private void addFile(File file) {
        toCompile.add(file);
    }

    private File resolveFromSearchPath(String filename) throws SassCompilationError {
        File resolved = null;
        File f = new File(filename);
        if (f.exists()) {
            resolved = f;
        }
        if (resolved == null) {
            throw new SassCompilationError("Unable to resolve import: " + filename);
        }
        return resolved;
    }

    public String compileFile() throws IOException {
        String result = null;
        parsedSources = collectAndParseSources();
        expandSources();
        outputCompiledSources(entryPoint);
        return result;
    }

    private Map<File, ParseTree> collectAndParseSources() throws IOException {
        Map<File, ParseTree> parsedSources = new HashMap<>();
        while (!toCompile.isEmpty()) {
            File f = toCompile.remove();
            ParseTree tree = buildParseTree(f);
            parsedSources.put(f, tree);
            collectImports(tree);
        }
        return parsedSources;
    }

    private void expandSources() {
        for (ParseTree tree: parsedSources.values()) {
            new ExpansionVisitor(expandedSelectors, variableValues).visit(tree);
        }
    }

    private void collectImports(ParseTree tree) {
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new ImportListener(this), tree);
    }

    private ParseTree buildParseTree(File f) throws IOException {
        ANTLRFileStream in = new ANTLRFileStream(f.getAbsolutePath());
        SassLexer lexer = new SassLexer(in);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SassParser parser = new SassParser(tokens);
        parser.setErrorHandler(new BailErrorStrategy());
        parser.setBuildParseTree(true);
        return parser.sass_file();
    }

    private void outputCompiledSources(String entryPoint) throws IOException {
        File entryFile = new File(entryPoint);

        String outputFilename = entryPoint.replaceFirst("\\.scss$", ".css");
        FileWriter fw = new FileWriter(outputFilename);
        PrintWriter out = new PrintWriter(fw);
        ParseTree parseTree = parsedSources.get(entryFile);
        String output = getCompiledOutput(parseTree);
        out.print(output);
        out.close();
        fw.close();
    }

    private String getCompiledOutput(ParseTree parseTree) {
        CompressedOutputVisitor visitor = new CompressedOutputVisitor(expandedSelectors, variableValues);
        visitor.visit(parseTree);
        return visitor.getOutput();
    }
}
