package mrtim.sasscompiler;

import mrtim.sasscompiler.grammar.SassBaseListener;
import mrtim.sasscompiler.grammar.SassParser;

public class ImportListener extends SassBaseListener {

    private Context context;

    public ImportListener(Context context) {
        this.context = context;
    }

    @Override
    public void enterImport_statement(SassParser.Import_statementContext ctx) {
        for (SassParser.Import_targetContext i: ctx.import_target()) {
            System.out.println("import: " + i.getText());
            context.addFile(i.getText());
        }
    }
}
