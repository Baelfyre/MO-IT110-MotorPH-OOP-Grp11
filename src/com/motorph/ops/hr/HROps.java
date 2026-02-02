/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.motorph.ops.hr;

import com.motorph.domain.models.Employee;
import java.util.List;

/**
 *
 * @author ACER
 */
public interface HROps {

    List<Employee> listEmployees(boolean includeArchived);

    Employee getEmployee(int empId);

    boolean createEmployee(Employee emp, int performedByUserId);

    boolean updateEmployee(Employee emp, int performedByUserId);

    boolean deleteEmployee(int empId, int performedByUserId);

}
