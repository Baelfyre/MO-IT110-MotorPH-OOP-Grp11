/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.motorph.ops.hr;

import com.motorph.domain.models.Employee;
import com.motorph.domain.models.User;
import java.util.List;

public interface HROps {
    List<Employee> listEmployees(boolean includeArchived);
    Employee getEmployee(int empId);
    
    // Pass the User object instead of int performedByUserId
    boolean createEmployee(Employee emp, User currentUser);
    boolean updateEmployee(Employee emp, User currentUser);
    boolean deleteEmployee(int empId, User currentUser);
    boolean isEmployeeIdDuplicate(int empId);
}
