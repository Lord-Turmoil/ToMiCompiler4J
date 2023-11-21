package tomic.parser.table;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private static int nextId;
    private final Map<Integer, SymbolTableBlock> blocks;

    public SymbolTable() {
        blocks = new HashMap<>();
    }

    public SymbolTableBlock newRoot() {
        var block = new SymbolTableBlock(nextId++, this, null);
        blocks.put(block.getId(), block);
        return block;
    }

    public SymbolTableBlock newBlock(SymbolTableBlock parent) {
        var block = new SymbolTableBlock(nextId++, this, parent);
        blocks.put(block.getId(), block);
        return block;
    }

    public SymbolTableBlock get(int id) {
        return blocks.getOrDefault(id, null);
    }
}
