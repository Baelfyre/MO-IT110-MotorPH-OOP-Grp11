/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.ops.it;

import com.motorph.domain.models.User;
import com.motorph.repository.UserRepository;
import com.motorph.repository.csv.DataPaths;
import com.motorph.service.LogService;

/**
 *
 * @author ACER
 */
public class ItOpsImpl implements ItOps {

    private final UserRepository userRepo;
    private final LogService logService;

    public ItOpsImpl(UserRepository userRepo, LogService logService) {
        this.userRepo = userRepo;
        this.logService = logService;
    }

    @Override
    public boolean resetPasswordToDefault(String username, int performedByUserId) {
        return resetPasswordInternal(username, DataPaths.DEFAULT_PASSWORD, performedByUserId, true);
    }

    @Override
    public boolean resetPassword(String username, String newPassword, int performedByUserId) {
        return resetPasswordInternal(username, newPassword, performedByUserId, false);
    }

    private boolean resetPasswordInternal(String username, String newPassword, int performedByUserId, boolean isDefault) {
        String actor = String.valueOf(performedByUserId);
        String uname = safeTrim(username);
        String pass = safeTrim(newPassword);

        if (uname.isEmpty() || pass.isEmpty()) {
            logService.recordAction(actor, "IT_RESET_PASSWORD_FAILED", "Blank username or password.");
            return false;
        }

        User existing = userRepo.findByUsername(uname);
        if (existing == null) {
            logService.recordAction(actor, "IT_RESET_PASSWORD_FAILED", "User not found: " + uname);
            return false;
        }

        try {
            userRepo.updatePassword(uname, pass);

            User reloaded = userRepo.findByUsername(uname);
            boolean ok = (reloaded != null) && reloaded.verifyPassword(pass);

            String action = ok ? "IT_RESET_PASSWORD_OK" : "IT_RESET_PASSWORD_FAILED";
            String mode = isDefault ? "default" : "custom";
            logService.recordAction(actor, action, "Reset password (" + mode + ") for user: " + uname);

            return ok;
        } catch (Exception ex) {
            logService.recordAction(actor, "IT_RESET_PASSWORD_FAILED", "Exception resetting password for " + uname + ": " + ex.getMessage());
            return false;
        }
    }

    @Override
    public boolean lockAccount(String username, int performedByUserId) {
        return setLockStatus(username, true, performedByUserId);
    }

    @Override
    public boolean unlockAccount(String username, int performedByUserId) {
        return setLockStatus(username, false, performedByUserId);
    }

    @Override
    public boolean setLockStatus(String username, boolean locked, int performedByUserId) {
        String actor = String.valueOf(performedByUserId);
        String uname = safeTrim(username);

        if (uname.isEmpty()) {
            logService.recordAction(actor, "IT_SET_LOCK_FAILED", "Blank username.");
            return false;
        }

        User existing = userRepo.findByUsername(uname);
        if (existing == null) {
            logService.recordAction(actor, "IT_SET_LOCK_FAILED", "User not found: " + uname);
            return false;
        }

        try {
            userRepo.updateLockStatus(uname, locked);

            User reloaded = userRepo.findByUsername(uname);
            boolean ok = (reloaded != null) && (reloaded.isLocked() == locked);

            String action;
            if (ok) {
                action = locked ? "IT_LOCK_OK" : "IT_UNLOCK_OK";
            } else {
                action = "IT_SET_LOCK_FAILED";
            }

            logService.recordAction(actor, action, (locked ? "Locked" : "Unlocked") + " user: " + uname);
            return ok;
        } catch (Exception ex) {
            logService.recordAction(actor, "IT_SET_LOCK_FAILED", "Exception updating lock for " + uname + ": " + ex.getMessage());
            return false;
        }
    }

    private String safeTrim(String s) {
        return (s == null) ? "" : s.trim();
    }
}
