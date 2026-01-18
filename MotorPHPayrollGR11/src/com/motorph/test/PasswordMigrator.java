/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.test;

import com.motorph.repository.csv.DataPaths;
import com.motorph.utils.PasswordUtil;
import java.io.*;
import java.util.Scanner;

/**
 * UTILITY SCRIPT:
 * run this ONCE to convert your plain-text "data_LogIn.csv" 
 * into a hashed "data_LogIn_SECURE.csv".
 * @author ACER
 */
public class PasswordMigrator {

    public static void main(String[] args) {
        System.out.println("Starting Password Migration...");
        
        File inputFile = new File(DataPaths.LOGIN_CSV); // Reads "./data/data_LogIn.csv"
        File outputFile = new File("./data/data_LogIn_SECURE.csv"); // The new file

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            // 1. Read and Write the Header row (don't hash this!)
            if ((line = br.readLine()) != null) {
                bw.write(line);
                bw.newLine();
            }

            // 2. Process every user
            int count = 0;
            while ((line = br.readLine()) != null) {
                // Split CSV line correctly
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                
                if (data.length >= 2) {
                    String username = data[0];
                    String plainPassword = data[1]; // e.g., "mGarcia"
                    
                    // --- THE MAGIC HAPPENS HERE ---
                    // Convert "mGarcia" -> "a8b9c..." (SHA-256 Hash)
                    String hashedPassword = PasswordUtil.hashPassword(plainPassword);
                    
                    // Rebuild the CSV line with the NEW password
                    StringBuilder newLine = new StringBuilder();
                    newLine.append(username).append(",");
                    newLine.append(hashedPassword).append(","); // Insert Hash
                    
                    // Append the rest of the columns (Last Name, First Name, etc.)
                    for (int i = 2; i < data.length; i++) {
                        newLine.append(data[i]);
                        if (i < data.length - 1) newLine.append(",");
                    }
                    
                    bw.write(newLine.toString());
                    bw.newLine();
                    count++;
                }
            }
            
            System.out.println("Success! Migrated " + count + " users.");
            System.out.println("New file created at: " + outputFile.getAbsolutePath());
            System.out.println("PLEASE ACTION: Delete the old 'data_LogIn.csv' and rename 'data_LogIn_SECURE.csv' to 'data_LogIn.csv'");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}