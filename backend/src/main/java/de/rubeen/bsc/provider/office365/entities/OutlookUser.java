package de.rubeen.bsc.provider.office365.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import static java.text.MessageFormat.format;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OutlookUser {
    private String id, displayName, userPrincipalName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return format("[id={0}, displayName={2}, userPrincipalName={3}]", id, displayName, userPrincipalName);
    }

    public String getUserPrincipalName() {
        return userPrincipalName;
    }

    public void setUserPrincipalName(String userPrincipalName) {
        this.userPrincipalName = userPrincipalName;
    }
}