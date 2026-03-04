/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.domain.models;

import com.motorph.domain.enums.Role;
import com.motorph.utils.PasswordUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author OngoJ.
 */
public class User {

    private int id;
    private String username;
    private String passwordHash;
    private final List<Role> roles = new ArrayList<>();
    private boolean locked;

    public User(int id, String username, String passwordHash, List<Role> roles, boolean locked) {
        setId(id);
        setUsername(username);
        this.passwordHash = passwordHash;
        this.locked = locked;

        if (roles != null) {
            for (Role r : roles) {
                addRole(r);
            }
        }

        if (this.roles.isEmpty()) {
            this.roles.add(Role.EMPLOYEE);
        }
    }

    // Annotation: Compatibility overload for repositories that store a single Role.
    public User(int id, String username, String passwordHash, Role role, boolean locked) {
        this(id, username, passwordHash, (role == null ? null : List.of(role)), locked);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("User id must be > 0.");
        }
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        this.username = username.trim();
    }

    public String getPassword() {
        return passwordHash;
    }

    public void setPassword(String password) {
        this.passwordHash = PasswordUtil.hashPassword(password);
    }

    // Annotation: Verifies password using PasswordUtil to support PBKDF2 and legacy plain text.
    public boolean verifyPassword(String password) {
        return PasswordUtil.verifyPassword(password, this.passwordHash);
    }

    public List<Role> getRoles() {
        return Collections.unmodifiableList(roles);
    }

    public void addRole(Role role) {
        if (role == null) {
            return;
        }
        if (!roles.contains(role)) {
            roles.add(role);
        }
    }

    public void removeRole(Role role) {
        if (role == null) {
            return;
        }
        if (roles.size() <= 1) {
            throw new IllegalStateException("User must retain at least one role.");
        }
        roles.remove(role);
        if (roles.isEmpty()) {
            roles.add(Role.EMPLOYEE);
        }
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    // Annotation: RBAC helper used by UI and Ops without exposing permission storage details.
    public boolean hasPermission(String permission) {
        if (permission == null || permission.trim().isEmpty()) {
            return false;
        }
        String p = permission.trim();
        for (Role r : roles) {
            if (r.getPermissions().contains(p)) {
                return true;
            }
        }
        return false;
    }
}
