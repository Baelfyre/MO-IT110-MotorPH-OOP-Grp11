/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.motorph.utils;

import com.motorph.domain.models.AddressReference;
import com.motorph.repository.csv.CsvAddressReferenceRepository;

import java.util.List;

/**
 *
 * @author OngoJ.
 */
public final class AddressParser {

    private AddressParser() {
        // Utility class
    }

    public static ParsedAddress parse(String fullAddress, CsvAddressReferenceRepository addressRepo) {
        ParsedAddress parsed = new ParsedAddress();

        if (fullAddress == null || fullAddress.trim().isEmpty() || addressRepo == null) {
            return parsed;
        }

        String value = fullAddress.trim();
        List<AddressReference> rows = addressRepo.findAllRows();

        AddressReference bestMatch = null;

        for (AddressReference row : rows) {
            if (row == null) {
                continue;
            }

            String province = safe(row.getProvince());
            String city = safe(row.getCityMunicipality());
            String zip = safe(row.getZipCode());

            if (province.isEmpty() || city.isEmpty()) {
                continue;
            }

            boolean hasProvince = containsIgnoreCase(value, province);
            boolean hasCity = containsIgnoreCase(value, city);
            boolean hasZip = !zip.isEmpty() && containsIgnoreCase(value, zip);

            if (hasProvince && hasCity) {
                if (bestMatch == null) {
                    bestMatch = row;
                }

                if (hasZip) {
                    bestMatch = row;
                    break;
                }
            }
        }

        if (bestMatch == null) {
            parsed.setAddressLine1(value);
            return parsed;
        }

        parsed.setProvince(bestMatch.getProvince());
        parsed.setCityMunicipality(bestMatch.getCityMunicipality());
        parsed.setZipCode(bestMatch.getZipCode());

        String remaining = value;
        remaining = removeTokenIgnoreCase(remaining, parsed.getZipCode());
        remaining = removeTokenIgnoreCase(remaining, parsed.getProvince());
        remaining = removeTokenIgnoreCase(remaining, parsed.getCityMunicipality());
        remaining = cleanupCommas(remaining);

        if (remaining.contains(",")) {
            String[] parts = remaining.split(",", 2);
            parsed.setAddressLine1(parts[0].trim());
            parsed.setAddressLine2(parts.length > 1 ? parts[1].trim() : "");
        } else {
            parsed.setAddressLine1(remaining.trim());
            parsed.setAddressLine2("");
        }

        return parsed;
    }

    private static boolean containsIgnoreCase(String source, String token) {
        if (source == null || token == null || token.trim().isEmpty()) {
            return false;
        }
        return source.toLowerCase().contains(token.trim().toLowerCase());
    }

    private static String removeTokenIgnoreCase(String source, String token) {
        if (source == null || token == null || token.trim().isEmpty()) {
            return source;
        }

        String pattern = "(?i)(^|,\\s*)" + java.util.regex.Pattern.quote(token.trim()) + "(\\s*,|$)";
        String result = source.replaceAll(pattern, ", ");
        return cleanupCommas(result);
    }

    private static String cleanupCommas(String value) {
        if (value == null) {
            return "";
        }

        String cleaned = value.replaceAll("\\s+,", ",");
        cleaned = cleaned.replaceAll(",\\s*,+", ", ");
        cleaned = cleaned.replaceAll("^\\s*,\\s*", "");
        cleaned = cleaned.replaceAll("\\s*,\\s*$", "");
        cleaned = cleaned.replaceAll("\\s{2,}", " ");
        return cleaned.trim();
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    public static final class ParsedAddress {
        private String addressLine1 = "";
        private String addressLine2 = "";
        private String province = "";
        private String cityMunicipality = "";
        private String zipCode = "";

        public String getAddressLine1() {
            return addressLine1;
        }

        public void setAddressLine1(String addressLine1) {
            this.addressLine1 = addressLine1 == null ? "" : addressLine1;
        }

        public String getAddressLine2() {
            return addressLine2;
        }

        public void setAddressLine2(String addressLine2) {
            this.addressLine2 = addressLine2 == null ? "" : addressLine2;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province == null ? "" : province;
        }

        public String getCityMunicipality() {
            return cityMunicipality;
        }

        public void setCityMunicipality(String cityMunicipality) {
            this.cityMunicipality = cityMunicipality == null ? "" : cityMunicipality;
        }

        public String getZipCode() {
            return zipCode;
        }

        public void setZipCode(String zipCode) {
            this.zipCode = zipCode == null ? "" : zipCode;
        }
    }
}