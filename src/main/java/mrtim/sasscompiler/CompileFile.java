package mrtim.sasscompiler;

import java.io.IOException;

public class CompileFile {

    public static void main(String[] args) throws IOException {
        String filename = args[0];
        Context c = new Context.Builder().entryPoint(filename).build();
        c.compileFile();
    }

}
