package de.rubeen.bsc.provider.office365;

import de.rubeen.bsc.provider.office365.entities.Calendar;
import de.rubeen.bsc.provider.office365.entities.Event;
import de.rubeen.bsc.provider.office365.entities.OutlookUser;
import de.rubeen.bsc.provider.office365.entities.PagedResult;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OutlookService {
    @GET("/v1.0/me")
    Call<OutlookUser> getCurrentUser();

    @GET("/v1.0/me/events")
    Call<PagedResult<Event>> getEvents(
            @Query("$orderby") String orderBy,
            @Query("$select") String select,
            @Query("$top") Integer maxResults
    );

    @GET("/v1.0/me/calendars")
    Call<PagedResult<Calendar>> getCalendars(
            @Query("$top") Integer maxResults
    );
}
