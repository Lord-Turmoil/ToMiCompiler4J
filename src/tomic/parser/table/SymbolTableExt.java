package tomic.parser.table;

import tomic.parser.ast.AstExt;
import tomic.parser.ast.SyntaxNode;
import tomic.parser.ast.SyntaxTypes;

import java.util.ArrayList;
import java.util.List;

public class SymbolTableExt {
    private SymbolTableExt() {}

    public record ParamEntryPair(SyntaxNode node, VariableEntry entry) {}

    public static List<ParamEntryPair> buildParamVariableEntries(SyntaxNode funcParams) {
        if (funcParams == null) {
            return new ArrayList<>();
        }

        List<ParamEntryPair> entries = new ArrayList<>();

        var params = AstExt.getDirectChildNodes(funcParams, SyntaxTypes.FUNC_FPARAM);
        for (var param : params) {
            int dim = param.getIntAttribute("dim");
            var type = SymbolValueTypes.values()[param.getIntAttribute("type")];
            var builder = VariableEntry.builder(param.getAttribute("name")).setType(type);

            if (dim == 1) {
                builder.setSizes(0);
            } else if (dim == 2) {
                builder.setSizes(0, param.getIntAttribute("size"));
            }
            entries.add(new ParamEntryPair(param, builder.build()));
        }

        return entries;
    }
}
