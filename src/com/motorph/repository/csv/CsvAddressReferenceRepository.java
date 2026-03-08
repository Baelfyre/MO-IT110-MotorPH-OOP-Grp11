/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

import com.motorph.domain.models.AddressReference;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author OngoJ.
 */
public class CsvAddressReferenceRepository {

    private final Path addressCsvPath;

    public CsvAddressReferenceRepository(Path addressCsvPath) {
        this.addressCsvPath = addressCsvPath;
    }

    public List<AddressReference> findAllRows() {
        return loadRows();
    }

    public List<String> getProvinces() {
        Set<String> values = new LinkedHashSet<>();

        for (AddressReference row : loadRows()) {
            values.add(row.getProvince());
        }

        return new ArrayList<>(values);
    }

    public List<String> getCitiesByProvince(String province) {
        Set<String> values = new LinkedHashSet<>();

        if (isBlank(province)) {
            return new ArrayList<>(values);
        }

        for (AddressReference row : loadRows()) {
            if (row.getProvince().equalsIgnoreCase(province.trim())) {
                values.add(row.getCityMunicipality());
            }
        }

        return new ArrayList<>(values);
    }

    public String getZipCode(String province, String city) {
        if (isBlank(province) || isBlank(city)) {
            return "";
        }

        for (AddressReference row : loadRows()) {
            if (row.getProvince().equalsIgnoreCase(province.trim())
                    && row.getCityMunicipality().equalsIgnoreCase(city.trim())) {
                return row.getZipCode();
            }
        }

        return "";
    }

    private List<AddressReference> loadRows() {
        List<AddressReference> rows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(addressCsvPath.toFile()))) {
            String line;
            boolean firstRow = true;

            while ((line = reader.readLine()) != null) {
                if (firstRow) {
                    firstRow = false;
                    continue;
                }

                String[] parts = splitCsvLine(line);

                if (parts.length < 4) {
                    continue;
                }

                rows.add(new AddressReference(
                        parts[0].trim(), // Region
                        parts[1].trim(), // Province
                        parts[2].trim(), // CityMunicipality
                        "", // Area not used in unified file
                        parts[3].trim() // ZipCode
                ));
            }

        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Failed to load AddressReference.csv: " + addressCsvPath,
                    ex
            );
        }

        return rows;
    }

    private String[] splitCsvLine(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
