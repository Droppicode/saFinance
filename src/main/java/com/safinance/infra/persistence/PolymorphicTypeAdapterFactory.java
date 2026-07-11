package com.safinance.infra.persistence;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Uma fábrica de adaptadores para o Gson, inspirada no RuntimeTypeAdapterFactory do Google.
 * Ela resolve o problema do Aberto/Fechado (OCP) permitindo registrar subclasses de uma interface.
 */
public class PolymorphicTypeAdapterFactory<T> implements TypeAdapterFactory {
    private final Class<T> baseType;
    private final String typeFieldName;
    private final Map<String, Class<?>> labelToSubtype = new LinkedHashMap<>();
    private final Map<Class<?>, String> subtypeToLabel = new LinkedHashMap<>();

    private PolymorphicTypeAdapterFactory(Class<T> baseType, String typeFieldName) {
        this.baseType = baseType;
        this.typeFieldName = typeFieldName;
    }

    public static <T> PolymorphicTypeAdapterFactory<T> of(Class<T> baseType, String typeFieldName) {
        return new PolymorphicTypeAdapterFactory<>(baseType, typeFieldName);
    }

    public static <T> PolymorphicTypeAdapterFactory<T> of(Class<T> baseType) {
        return new PolymorphicTypeAdapterFactory<>(baseType, "type");
    }

    public PolymorphicTypeAdapterFactory<T> registerSubtype(Class<? extends T> type, String label) {
        labelToSubtype.put(label, type);
        subtypeToLabel.put(type, label);
        return this;
    }

    public PolymorphicTypeAdapterFactory<T> registerSubtype(Class<? extends T> type) {
        return registerSubtype(type, type.getSimpleName());
    }

    @Override
    public <R> TypeAdapter<R> create(Gson gson, TypeToken<R> type) {
        if (type.getRawType() != baseType) {
            return null;
        }

        final Map<String, TypeAdapter<?>> labelToDelegate = new LinkedHashMap<>();
        final Map<Class<?>, TypeAdapter<?>> subtypeToDelegate = new LinkedHashMap<>();

        for (Map.Entry<String, Class<?>> entry : labelToSubtype.entrySet()) {
            TypeAdapter<?> delegate = gson.getDelegateAdapter(this, TypeToken.get(entry.getValue()));
            labelToDelegate.put(entry.getKey(), delegate);
            subtypeToDelegate.put(entry.getValue(), delegate);
        }

        return new TypeAdapter<R>() {
            @Override
            public R read(JsonReader in) throws IOException {
                JsonElement jsonElement = JsonParser.parseReader(in);
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                
                if (!jsonObject.has(typeFieldName)) {
                    throw new JsonParseException("Não é possível desserializar a interface " + baseType 
                        + " porque o campo obrigatório '" + typeFieldName + "' não está presente.");
                }
                String label = jsonObject.get(typeFieldName).getAsString();
                
                @SuppressWarnings("unchecked")
                TypeAdapter<R> delegate = (TypeAdapter<R>) labelToDelegate.get(label);
                if (delegate == null) {
                    throw new JsonParseException("Não foi possível desserializar o subtipo: " + label);
                }
                return delegate.fromJsonTree(jsonElement);
            }

            @Override
            public void write(JsonWriter out, R value) throws IOException {
                Class<?> srcType = value.getClass();
                String label = subtypeToLabel.get(srcType);
                @SuppressWarnings("unchecked")
                TypeAdapter<R> delegate = (TypeAdapter<R>) subtypeToDelegate.get(srcType);
                if (delegate == null) {
                    throw new JsonParseException("Classe não registrada para serialização: " + srcType.getName());
                }
                JsonObject jsonObject = delegate.toJsonTree(value).getAsJsonObject();
                
                JsonObject clone = new JsonObject();
                clone.addProperty(typeFieldName, label);
                for (Map.Entry<String, JsonElement> e : jsonObject.entrySet()) {
                    clone.add(e.getKey(), e.getValue());
                }
                gson.toJson(clone, out);
            }
        }.nullSafe();
    }
}
