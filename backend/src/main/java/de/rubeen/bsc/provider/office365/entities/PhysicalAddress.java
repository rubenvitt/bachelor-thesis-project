package de.rubeen.bsc.provider.office365.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PhysicalAddress {
    private String street;
    private String city;
    private String state;
    private String countryOrRegion;
    private String postalCode;

    public PhysicalAddress() {
    }

    public PhysicalAddress(String street, String city, String state, String countryOrRegion, String postalCode) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.countryOrRegion = countryOrRegion;
        this.postalCode = postalCode;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountryOrRegion() {
        return countryOrRegion;
    }

    public void setCountryOrRegion(String countryOrRegion) {
        this.countryOrRegion = countryOrRegion;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
