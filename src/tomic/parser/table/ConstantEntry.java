package tomic.parser.table;

import java.util.ArrayList;

public class ConstantEntry extends SymbolTableEntry {
    private final SymbolValueTypes type;
    private final int dimension;
    private final int value;
    ArrayList<ArrayList<Integer>> values;

    private ConstantEntry(String name, SymbolValueTypes type, int value) {
        super(name);
        this.type = type;
        this.dimension = 0;
        this.value = value;
        this.values = null;
    }

    private ConstantEntry(String name, SymbolValueTypes type, int dimension, ArrayList<ArrayList<Integer>> values) {
        super(name);
        this.type = type;
        this.dimension = dimension;
        this.value = 0;
        this.values = values;
    }

    public SymbolValueTypes getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    public int getValue(int d1) {
        return values.get(0).get(d1);
    }

    public int getValue(int d1, int d2) {
        return values.get(d1).get(d2);
    }

    public int getSize(int dim) {
        if (dim == 0) {
            if (dimension == 1) {
                return values.get(0).size();
            } else {
                return values.size();
            }
        } else {
            return values.size();
        }
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private final String name;
        private SymbolValueTypes type;
        private int dimension;
        private int value;
        ArrayList<ArrayList<Integer>> values;

        public Builder(String name) {
            this.name = name;
        }

        public Builder setType(SymbolValueTypes type) {
            this.type = type;
            return this;
        }

        public Builder setDimension(int dimension) {
            this.dimension = dimension;
            return this;
        }

        public Builder setValue(int value) {
            this.dimension = 0;
            this.value = value;
            return this;
        }

        public Builder setValues(ArrayList<ArrayList<Integer>> values) {
            this.values = values;
            return this;
        }

        public ConstantEntry build() {
            if (dimension == 0) {
                return new ConstantEntry(name, type, value);
            } else {
                return new ConstantEntry(name, type, dimension, values);
            }
        }
    }
}
