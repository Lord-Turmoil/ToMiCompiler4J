package tomic.logger.error.impl;

import lib.twio.ITwioWriter;
import tomic.logger.error.ErrorTypes;
import tomic.logger.error.IErrorLogger;
import tomic.logger.error.IErrorMapper;

import java.util.ArrayList;

public class VerboseErrorLogger implements IErrorLogger {
    private final ArrayList<Entry> entries;
    private final IErrorMapper mapper;

    public VerboseErrorLogger(IErrorMapper mapper) {
        this.mapper = mapper;
        entries = new ArrayList<>();
    }

    @Override
    public void log(int line, int column, ErrorTypes type, String message) {
        entries.add(new Entry(line, column, type, message));
    }

    @Override
    public void dumps(ITwioWriter writer) {
        entries.sort((x, y) -> {
            if (x.line != y.line) {
                return x.line - y.line;
            } else if (x.column != y.column) {
                return x.column - y.column;
            } else {
                return x.type.ordinal() - y.type.ordinal();
            }
        });

        for (Entry entry : entries.stream().distinct().toList()) {
            writer.write("Line " + entry.line + ", Column " + entry.column);
            writer.writeLine(": " + mapper.description(entry.type));
            writer.writeLine("    " + entry.message);
        }
    }

    @Override
    public int count() {
        return entries.size();
    }

    private record Entry(int line, int column, ErrorTypes type, String message) {}
}
