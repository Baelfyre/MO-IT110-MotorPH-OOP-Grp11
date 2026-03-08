/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.motorph.domain.models;

/**
 *
 * @author OngoJ.
 */

public class AddressReference {
    private final String region;
    private final String province;
    private final String cityMunicipality;
    private final String area;
    private final String zipCode;

    public AddressReference(String region, String province, String cityMunicipality, String area, String zipCode) {
        this.region = region;
        this.province = province;
        this.cityMunicipality = cityMunicipality;
        this.area = area;
        this.zipCode = zipCode;
    }

    public String getRegion() {
        return region;
    }

    public String getProvince() {
        return province;
    }

    public String getCityMunicipality() {
        return cityMunicipality;
    }

    public String getArea() {
        return area;
    }

    public String getZipCode() {
        return zipCode;
    }
}