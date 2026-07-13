package com.safinance.infra.persistence;

import com.google.gson.Gson;
import com.safinance.core.domain.Entity;
import com.safinance.core.ports.Repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A repository implementation that uses JSONL (JSON Lines) files for append-only persistence.
 * Each modification generates a new line. When initialized, it rehydrates the state chronologically.
 * 
 * @param <T> the entity type
 */
public class JsonlRepository<T extends Entity> implements Repository<T, String> {
    private final Map<String, T> memoryCache = new HashMap<>();
    private final String filePath;
    private final Class<T> type;
    private final Gson gson;

    /**
     * Constructs a JsonlRepository.
     * 
     * @param filePath the path to the .jsonl file
     * @param type the Class of the entity
     * @param gson the Gson instance to use for serialization
     */
    public JsonlRepository(String filePath, Class<T> type, Gson gson) {
        this.filePath = filePath;
        this.type = type;
        this.gson = gson;
        
        // Ensure parent directories exist
        File file = new File(filePath);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        
        this.rehydrate();
    }

    /**
     * Rehydrates the in-memory cache by reading the JSONL file sequentially.
     * Newer entries overwrite older ones with the same ID, ensuring the cache
     * holds the latest state of each entity.
     */
    private void rehydrate() {
        File file = new File(filePath);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                T entity = gson.fromJson(line, type);
                if (entity != null && entity.getId() != null) {
                    memoryCache.put(entity.getId(), entity);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error rehydrating repository: " + filePath, e);
        }
    }

    @Override
    public Optional<T> findById(String id) {
        return Optional.ofNullable(memoryCache.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(memoryCache.values());
    }

    @Override
    public void save(T entity) {
        String json = gson.toJson(entity, type);
        try (FileWriter fw = new FileWriter(filePath, true); // true enables append-only mode
             PrintWriter out = new PrintWriter(new BufferedWriter(fw))) {
            
            out.println(json); 
            memoryCache.put(entity.getId(), entity);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist entity: " + entity.getId(), e);
        }
    }
    
    @Override
    public void saveAll(List<T> entities) {
        if (entities == null || entities.isEmpty()) return;
        
        try (FileWriter fw = new FileWriter(filePath, true);
             PrintWriter out = new PrintWriter(new BufferedWriter(fw))) {
            
            for (T entity : entities) {
                String json = gson.toJson(entity, type);
                out.println(json);
                memoryCache.put(entity.getId(), entity);
            }
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist entities batch in: " + filePath, e);
        }
    }
}
