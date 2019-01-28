package de.rubeen.bsc.provider.office365.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PagedResult<T> {
    @JsonProperty("@odata.nextLink")
    private String nextPageLink;

    @JsonProperty("@odata.context")
    private String dataContext;
    private List<T> value;

    public String getNextPageLink() {
        return nextPageLink;
    }
    public void setNextPageLink(String nextPageLink) {
        this.nextPageLink = nextPageLink;
    }
    public List<T> getValue() {
        return value;
    }
    public void setValue(List<T> value) {
        this.value = value;
    }
}