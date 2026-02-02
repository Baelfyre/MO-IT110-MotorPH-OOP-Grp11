/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.motorph.repository;

import com.motorph.domain.models.Employee;
import java.util.List;

/**
 * The CONTRACT. Defines WHAT we can do, but not HOW we do it.
 */
public interface EmployeeRepository {

    List<Employee> findAll();

    Employee findById(int id);

    void create(Employee emp);

    boolean update(Employee emp);

    boolean delete(int empId);

}
