/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.parser.table;

import java.util.ArrayList;

public class VariableEntry extends SymbolTableEntry {
    private SymbolValueTypes type;
    private final ArrayList<Integer> dimensions;

    private VariableEntry(String name, SymbolValueTypes type, ArrayList<Integer> dimensions) {
        super(name);
        this.type = type;
        this.dimensions = dimensions;
    }

    @Override
    public boolean isVariable() {
        return true;
    }

    public SymbolValueTypes getType() {
        return type;
    }

    public int getDimension() {
        return dimensions.size();
    }

    public boolean isInteger() {
        return getDimension() == 0;
    }

    public int getSize(int dim) {
        return dimensions.get(dim);
    }

    public ArrayList<Integer> getSizes() {
        return dimensions;
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private final String name;
        private SymbolValueTypes type;
        private final ArrayList<Integer> dimensions = new ArrayList<>();

        public Builder(String name) {
            this.name = name;
        }

        public Builder setType(SymbolValueTypes type) {
            this.type = type;
            return this;
        }

        public Builder addDimension(int size) {
            dimensions.add(size);
            return this;
        }

        public VariableEntry build() {
            return new VariableEntry(name, type, dimensions);
        }
    }
}
