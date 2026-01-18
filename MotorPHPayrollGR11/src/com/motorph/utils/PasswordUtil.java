/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.utils;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.math.BigInteger;

/**
 * Handles password hashing using SHA-256.
 * Adheres to SRP: Only handles security logic.
 * @author ACER
 */
public class PasswordUtil {

    /**
     * Hashing Function.
     * Turns "ph12345" into a long, unreadable string of characters.
     */
    public static String hashPassword(String originalPassword) {
        try {
            // Use SHA-256 Algorithm (Standard Java Security)
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            
            // Convert password to bytes and hash it
            byte[] hash = md.digest(originalPassword.getBytes(StandardCharsets.UTF_8));
            
            // Convert bytes to Hex String
            BigInteger number = new BigInteger(1, hash);
            StringBuilder hexString = new StringBuilder(number.toString(16));

            // Pad with leading zeros if needed
            while (hexString.length() < 32) {
                hexString.insert(0, '0');
            }

            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Verification Function.
     * Hashes the input and compares it to the stored hash.
     */
    public static boolean checkPassword(String inputPassword, String storedHash) {
        String hashedInput = hashPassword(inputPassword);
        return hashedInput.equals(storedHash);
    }
}