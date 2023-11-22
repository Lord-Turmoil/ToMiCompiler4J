package tomic.parser.table;

import java.util.HashMap;
import java.util.Map;

public class SymbolTableBlock {
    private final int id;
    private final SymbolTable table;
    private final SymbolTableBlock parent;
    private final Map<String, SymbolTableEntry> entries;

    // Can only be instantiated by SymbolTable
    SymbolTableBlock(int id, SymbolTable table, SymbolTableBlock parent) {
        this.id = id;
        this.table = table;
        this.parent = parent;
        this.entries = new HashMap<>();
    }

    public int getId() {
        return id;
    }

    public SymbolTableBlock getParent() {
        return parent;
    }

    public SymbolTableBlock addEntry(SymbolTableEntry entry) {
        entries.put(entry.getName(), entry);
        return this;
    }

    public SymbolTableEntry findEntry(String name) {
        var entry = findLocalEntry(name);
        if (entry != null) {
            return entry;
        }
        if (parent != null) {
            return parent.findEntry(name);
        }
        return null;
    }

    public SymbolTableEntry findLocalEntry(String name) {
        return entries.getOrDefault(name, null);
    }

    public SymbolTableBlock newChild() {
        return table.newBlock(this);
    }
}
