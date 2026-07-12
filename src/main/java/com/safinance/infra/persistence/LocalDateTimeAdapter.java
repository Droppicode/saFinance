package com.safinance.infra.persistence;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Serializes and deserializes LocalDateTime values using ISO-8601.
 */
public final class LocalDateTimeAdapter
        extends TypeAdapter<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void write(
            JsonWriter writer,
            LocalDateTime value
    ) throws IOException {
        if (value == null) {
            writer.nullValue();
            return;
        }

        writer.value(value.format(FORMATTER));
    }

    @Override
    public LocalDateTime read(
            JsonReader reader
    ) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }

        return LocalDateTime.parse(
                reader.nextString(),
                FORMATTER
        );
    }
}