/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.domain.models;

import java.time.LocalDate;

/**
 *
 * @author OngoJ.
 */
public class EmployeeDetails {

    private LocalDate birthday;
    private String address;
    private String phoneNumber;

    private String sssNumber;
    private String tinNumber;
    private String philHealthNumber;
    private String pagIbigNumber;
    private String email;

    public EmployeeDetails() {
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = safe(address);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = safe(phoneNumber);
    }

    public String getSssNumber() {
        return sssNumber;
    }

    public void setSssNumber(String sssNumber) {
        this.sssNumber = safe(sssNumber);
    }

    public String getTinNumber() {
        return tinNumber;
    }

    public void setTinNumber(String tinNumber) {
        this.tinNumber = safe(tinNumber);
    }

    public String getPhilHealthNumber() {
        return philHealthNumber;
    }

    public void setPhilHealthNumber(String philHealthNumber) {
        this.philHealthNumber = safe(philHealthNumber);
    }

    public String getPagIbigNumber() {
        return pagIbigNumber;
    }

    public void setPagIbigNumber(String pagIbigNumber) {
        this.pagIbigNumber = safe(pagIbigNumber);
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = safe(email).toLowerCase();
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
