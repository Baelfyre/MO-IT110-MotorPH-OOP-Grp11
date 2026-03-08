/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.motorph.utils;

/**
 *
 * @author OngoJ.
 */

public final class AddressFormatter {

    private static final String PROVINCE_PLACEHOLDER = "Select Province";
    private static final String CITY_PLACEHOLDER = "Select City / Municipality";

    private AddressFormatter() {
        // Annotation: Utility class, no instances.
    }

    public static String buildFullAddress(
            String addressLine1,
            String addressLine2,
            String cityMunicipality,
            String province,
            String zipCode
    ) {
        StringBuilder sb = new StringBuilder();

        appendPart(sb, addressLine1);
        appendPart(sb, addressLine2);
        appendPart(sb, cityMunicipality);
        appendPart(sb, province);
        appendPart(sb, zipCode);

        return sb.toString();
    }

    private static void appendPart(StringBuilder sb, String value) {
        if (isSkippable(value)) {
            return;
        }

        if (sb.length() > 0) {
            sb.append(", ");
        }

        sb.append(value.trim());
    }

    private static boolean isSkippable(String value) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        String trimmed = value.trim();
        return trimmed.equalsIgnoreCase(PROVINCE_PLACEHOLDER)
                || trimmed.equalsIgnoreCase(CITY_PLACEHOLDER);
    }
}