package com.project362.sfuhive.Calendar

import com.google.api.services.calendar.model.Event

object GoogleEventCache {
    val events = mutableMapOf<String, List<Event>>()
}
