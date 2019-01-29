package de.rubeen.bsc.provider.office365;

import de.rubeen.bsc.provider.office365.entities.*;
import org.joda.time.field.StrictDateTimeField;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Date;

public interface OutlookService {
    @GET("/v1.0/me")
    Call<OutlookUser> getCurrentUser();

    @GET("/v1.0/me/events")
    Call<PagedResult<Event>> getEvents(
            @Query("$orderby") String orderBy,
            @Query("$select") String select,
            @Query("$top") Integer maxResults
    );

    @GET("/v1.0/me/calendars/{id}/calendarView")
    Call<PagedResult<Event>> getEvents(
            @Path("id") String calendarId,
            @Query("$orderby") String orderBy,
            @Query("$select") String select,
            @Query("$top") Integer maxResults,
            @Query("startdatetime") String startDateTime,
            @Query("enddatetime") String endDateTime
    );

    @GET("/v1.0/me/calendars/{id}/calendarView")
    Call<PagedResult<Event>> getEvents(
            @Path("id") String calendarId,
            @Query("$filter") String filter,
            @Query("$top") Integer maxResults,
            @Query("startdatetime") String startDateTime,
            @Query("enddatetime") String endDateTime
    );

    @POST("/v1.0/me/calendars/{id}/events")
    Call<Event> createEvent(
            @Path("id") String calendarId,
            @Body Event event
    );

    @GET("/v1.0/me/calendars")
    Call<PagedResult<Calendar>> getCalendars(
            @Query("$top") Integer maxResults
    );
}
