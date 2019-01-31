package de.rubeen.bsc.provider.office365.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Location {
    private String displayName;
    private PhysicalAddress address;
    private String locationEmailAddress;


    public Location() {
    }

    public Location(String displayName) {
        this.displayName = displayName;
    }

    public Location(String displayName, PhysicalAddress address, String locationEmailAddress) {
        this.displayName = displayName;
        this.address = address;
        this.locationEmailAddress = locationEmailAddress;
    }

    public String getLocationEmailAddress() {
        return locationEmailAddress;
    }

    public void setLocationEmailAddress(String locationEmailAddress) {
        this.locationEmailAddress = locationEmailAddress;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public PhysicalAddress getAddress() {
        return address;
    }

    public void setAddress(PhysicalAddress address) {
        this.address = address;
    }
}
