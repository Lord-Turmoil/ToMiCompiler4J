package tomic.parser.table;

public class VariableEntry extends SymbolTableEntry {
    private SymbolValueTypes type;
    private final int dimension;
    private final int[] sizes;

    private VariableEntry(String name, SymbolValueTypes type, int dimension, int[] sizes) {
        super(name);
        this.dimension = dimension;
        this.sizes = sizes;
    }

    public SymbolValueTypes getType() {
        return type;
    }

    public int getDimension() {
        return dimension;
    }

    public int getSize(int dim) {
        return sizes[dim];
    }

    @Override
    public boolean isVariable() {
        return true;
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private final String name;
        private int dimension;
        private final int[] sizes = { 0, 0 };
        SymbolValueTypes type;

        public Builder(String name) {
            this.name = name;
        }

        public Builder setType(SymbolValueTypes type) {
            this.type = type;
            return this;
        }

        public Builder setSizes(int d1) {
            dimension = 1;
            sizes[0] = d1;
            return this;
        }

        public Builder setSizes(int d1, int d2) {
            dimension = 2;
            sizes[0] = d1;
            sizes[1] = d2;
            return this;
        }

        public VariableEntry build() {
            return new VariableEntry(name, type, dimension, sizes);
        }
    }
}
