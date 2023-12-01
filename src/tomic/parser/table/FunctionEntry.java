/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.parser.table;

import java.util.ArrayList;
import java.util.List;

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
        public final ArrayList<Integer> dimensions;

        public ParamEntry(SymbolValueTypes type, String name, ArrayList<Integer> dimensions) {
            this.type = type;
            this.name = name;
            this.dimensions = dimensions;
        }

        public int getDimension() {
            return dimensions.size();
        }

        public List<Integer> getSizes() {
            return dimensions;
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

        public Builder addParam(SymbolValueTypes type, String name, List<Integer> dims) {
            ArrayList<Integer> dimensions = new ArrayList<>(dims);
            params.add(new ParamEntry(type, name, dimensions));
            return this;
        }

        public Builder addParam(SymbolValueTypes type, String name) {
            params.add(new ParamEntry(type, name, new ArrayList<>()));
            return this;
        }

        public FunctionEntry build() {
            return new FunctionEntry(name, type, params);
        }
    }
}
