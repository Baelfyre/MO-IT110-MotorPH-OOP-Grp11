/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.motorph.ops.it;

import com.motorph.domain.models.User;
import java.util.List;

public interface ItOps {

    List<User> listUsers();

    boolean resetPasswordToDefault(String username, User currentUser);

    boolean resetPassword(String username, String newPassword, User currentUser);

    boolean lockAccount(String username, User currentUser);

    boolean unlockAccount(String username, User currentUser);

    boolean setLockStatus(String username, boolean locked, User currentUser);
}
