package com.motorph.ops.auth;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
import com.motorph.domain.models.User;

/**
 * Defines user authentication operations that all roles can perform. This layer
 * represents a use-case boundary and delegates validation to AuthService.
 *
 * @author ACER
 */
public interface AuthOps {

    /**
     * Attempts to authenticate a user and returns the resolved User on success.
     * A null return indicates failed authentication.
     */
    User login(String username, String password);
}
