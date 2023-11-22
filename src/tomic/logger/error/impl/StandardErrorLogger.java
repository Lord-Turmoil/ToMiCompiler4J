package tomic.logger.error.impl;

import lib.twio.ITwioWriter;
import tomic.logger.error.ErrorTypes;
import tomic.logger.error.IErrorLogger;
import tomic.logger.error.IErrorMapper;

import java.util.ArrayList;

public class StandardErrorLogger implements IErrorLogger {
    private final IErrorMapper mapper;
    private final ArrayList<Entry> entries;

    public StandardErrorLogger(IErrorMapper mapper) {
        this.mapper = mapper;
        entries = new ArrayList<>();
    }

    @Override
    public void log(int line, int column, ErrorTypes type, String message) {
        entries.add(new Entry(line, type));
    }

    @Override
    public void dumps(ITwioWriter writer) {
        entries.sort((x, y) -> {
            if (x.line != y.line) {
                return x.line - y.line;
            } else {
                return x.type.ordinal() - y.type.ordinal();
            }
        });

        for (Entry entry : entries.stream().distinct().toList()) {
            if (entry.type != ErrorTypes.UNKNOWN) {
                writer.writeLine(entry.line + " " + mapper.description(entry.type));
            }
        }
    }

    @Override
    public int count() {
        return entries.size();
    }

    private record Entry(int line, ErrorTypes type) {}
}
