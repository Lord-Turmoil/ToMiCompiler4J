/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.parser.table;

import java.util.ArrayList;

public class FunctionEntry extends SymbolTableEntry {
    private final SymbolValueTypes type;
    private final ArrayList<ParamEntry> params;

    private FunctionEntry(String name, SymbolValueTypes type, ArrayList<ParamEntry> params) {
        super(name);
        this.type = type;
        this.params = params;
    }

    @Override
    public boolean isFunction() {
        return true;
    }

    public SymbolValueTypes getType() {
        return type;
    }

    public int getParamCount() {
        return params.size();
    }

    public ParamEntry getParam(int index) {
        return params.get(index);
    }

    public ArrayList<ParamEntry> getParams() {
        return params;
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class ParamEntry {
        public SymbolValueTypes type;
        public String name;
        public int dimension;
        public int[] sizes = { 0, 0 };

        public ParamEntry(SymbolValueTypes type, String name, int dimension, int s1, int s2) {
            this.type = type;
            this.name = name;
            this.dimension = dimension;
            this.sizes[0] = s1;
            this.sizes[1] = s2;
        }
    }

    public static class Builder {
        private final String name;
        private SymbolValueTypes type;
        private final ArrayList<ParamEntry> params = new ArrayList<>();

        public Builder(String name) {
            this.name = name;
        }

        public Builder setType(SymbolValueTypes type) {
            this.type = type;
            return this;
        }

        public Builder addParam(SymbolValueTypes type, String name, int dim, int size) {
            params.add(new ParamEntry(type, name, dim, 0, size));
            return this;
        }

        public FunctionEntry build() {
            return new FunctionEntry(name, type, params);
        }
    }
}
