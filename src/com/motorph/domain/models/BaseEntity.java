/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.domain.models;

/**
 *
 * @author OngoJ 
 *
 */

public abstract class BaseEntity {

    protected int id;

    protected BaseEntity() { }

    protected BaseEntity(int id) {
        this.id = id;
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public abstract String toCsvRow();
}
