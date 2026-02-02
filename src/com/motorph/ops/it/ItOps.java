/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.motorph.ops.it;

/**
 *
 * @author ACER
 */
public interface ItOps {

    boolean resetPasswordToDefault(String username, int performedByUserId);

    boolean resetPassword(String username, String newPassword, int performedByUserId);

    boolean lockAccount(String username, int performedByUserId);

    boolean unlockAccount(String username, int performedByUserId);

    boolean setLockStatus(String username, boolean locked, int performedByUserId);
}
