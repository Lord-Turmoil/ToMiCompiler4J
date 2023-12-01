/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.parser.table;

import java.util.ArrayList;
import java.util.List;

public class ConstantEntry extends SymbolTableEntry {
    private final SymbolValueTypes type;
    private final ArrayList<Integer> dimensions;
    private final ArrayList<Integer> values;

    private ConstantEntry(String name, SymbolValueTypes type, ArrayList<Integer> dimensions, ArrayList<Integer> values) {
        super(name);
        this.type = type;
        this.dimensions = dimensions;
        this.values = values;
    }

    @Override
    public boolean isConstant() {
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


    public int getValue() {
        return values.get(0);
    }

    public int getValue(List<Integer> dims) {
        if (dims.size() != dimensions.size()) {
            throw new IllegalArgumentException("Dimension mismatch");
        }

        int index = 0;
        int size = 1;
        for (int i = dims.size() - 1; i >= 0; i--) {
            index += dims.get(i) * size;
            size *= getSize(i);
        }

        return values.get(index);
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
        private final ArrayList<Integer> values = new ArrayList<>();

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

        public Builder setValue(int value) {
            values.add(value);
            return this;
        }

        public Builder setValues(List<Integer> values) {
            this.values.addAll(values);
            return this;
        }

        public ConstantEntry build() {
            return new ConstantEntry(name, type, dimensions, values);
        }
    }
}
