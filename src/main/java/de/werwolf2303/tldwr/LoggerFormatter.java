package de.werwolf2303.tldwr;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LoggerFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        return String.format("[%s] (%s) %s\n",
                record.getLoggerName(),
                record.getLevel().getName(),
                formatMessage(record));
    }
}