/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.motorph.repository;

import com.motorph.domain.models.LogEntry;

/**
 *
 * @author ACER
 */
public interface LogRepository {

    boolean save(LogEntry entry);
}
