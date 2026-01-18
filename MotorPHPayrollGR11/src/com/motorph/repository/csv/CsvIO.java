/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for generic CSV File Input/Output operations. Handles the raw
 * reading and writing logic to keep Repositories clean.
 *
 * @author ACER
 */
public class CsvIO {

    /**
     * Reads a CSV file and returns the lines as a list of String arrays. Uses
     * robust splitting to handle commas inside quotes.
     *
     * @param filePath The full path to the file.
     * @return List of String[], where each array is a row split by commas.
     */
    public static List<String[]> read(String filePath) {
        List<String[]> records = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // IMPORTANT: Use this Regex to handle "90,000" correctly!
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                // Optional: Clean up quotes right here so you don't have to do it everywhere else
                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i].trim().replace("\"", "");
                }

                records.add(data);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath);
            e.printStackTrace();
        }
        return records;
    }

    /**
     * Writes a string content to a file.
     *
     * @param filePath The target file path.
     * @param content The line(s) to write.
     * @param append If true, adds to the end of the file; if false, overwrites
     * it.
     */
    public static void write(String filePath, String content, boolean append) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath, append))) {
            pw.println(content);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + filePath);
            e.printStackTrace();
        }
    }
}
