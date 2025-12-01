package com.project362.sfuhive.Calendar

import com.google.api.services.calendar.model.Event

//Simple in-memory cache for Google Calendar events keyed by a string date.

object GoogleEventCache {
    val events = mutableMapOf<String, List<Event>>()
}
