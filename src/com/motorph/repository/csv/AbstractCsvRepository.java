/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

import java.util.List;

/**
 * Abstract base repository for handling generic CSV file operations.
 * Demonstrates OOP Abstraction by defining template methods for data access.
 *
 * @param <T> The domain model entity type.
 */
public abstract class AbstractCsvRepository<T> {
    
    protected String filePath;

    protected AbstractCsvRepository(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Retrieves all records from the CSV file.
     * @return A list of parsed entities.
     */
    public abstract List<T> findAll();

    /**
     * Saves an entity to the CSV file.
     * @param entity The entity to save.
     * @return true if successful.
     */
    public abstract boolean save(T entity);
    
}
