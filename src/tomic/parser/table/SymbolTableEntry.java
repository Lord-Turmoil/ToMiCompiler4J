package tomic.parser.table;

public abstract class SymbolTableEntry {
    private final String name;

    public String getName() {
        return name;
    }

    protected SymbolTableEntry(String name) {
        this.name = name;
    }

    public boolean isVariable() {return false;}

    public boolean isConstant() {return false;}

    public boolean isFunction() {return false;}
}
