package com.project362.sfuhive.Calendar

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.project362.sfuhive.database.Calendar.GoogleEventDatabase
import com.project362.sfuhive.database.Calendar.GoogleEventEntity
import kotlinx.coroutines.*


// Helper class that handles Google Sign-In and fetching events from the user's primary Google Calendar.

class GoogleCalendarHelper(
    private val activity: Activity,
    private val onEventsFetched: (List<Event>) -> Unit
) {
    companion object {
        const val RC_SIGN_IN = 1001
    }

    private lateinit var googleSignInClient: GoogleSignInClient

    //Configure the GoogleSignIn client with the Calendar readonly scope.

    fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(CalendarScopes.CALENDAR_READONLY))
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }

    // Return the sign-in intent so callers can launch the sign-in flow
    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    // Simple helper to check whether a user is already signed in
    fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(activity) != null
    }

    // Sign out the current user and show a toast on completion
    fun signOut() {
        googleSignInClient.signOut().addOnCompleteListener {
            Toast.makeText(activity, "Signed out", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Handle result returned from the Google sign in intent. If sign-in succeeds
     * the helper will fetch calendar events for the signed-in account.
     */
    fun handleSignInResult(requestCode: Int, data: Intent?) {
        if (requestCode != RC_SIGN_IN) return

        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.result
            if (account != null) {
                Toast.makeText(activity, "Signed in as ${account.email}", Toast.LENGTH_SHORT).show()
                fetchCalendarEvents(account)
            }
        } catch (e: Exception) {
            Toast.makeText(activity, "Sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Convenience: refresh events for the last signed-in account
    fun refreshEvents() {
        val account = GoogleSignIn.getLastSignedInAccount(activity)
        if (account != null) {
            fetchCalendarEvents(account)
        } else {
            Toast.makeText(activity, "Please sign in first", Toast.LENGTH_SHORT).show()
        }
    }

    // Fetch events using Google Calendar API and persist them into the local DB.
    fun fetchCalendarEvents(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(
            activity, listOf(CalendarScopes.CALENDAR_READONLY)
        )
        credential.selectedAccount = account.account

        val service = com.google.api.services.calendar.Calendar.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("SFU Hive Calendar").build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val events = service.events().list("primary")
                    .setMaxResults(2500)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute()

                val items = events.items ?: emptyList()

                // Persist into local DB as a cache — keep only basic fields used by UI
                val dao = GoogleEventDatabase.getInstance(activity).googleEventDao()
                dao.deleteAllEvents()

                val entities = items.mapNotNull { event ->
                    val start = event.start?.dateTime ?: event.start?.date ?: return@mapNotNull null
                    val dateStr = start.toString().substring(0, 10)

                    GoogleEventEntity(
                        eventId = event.id ?: "",
                        title = event.summary ?: "Untitled Event",
                        date = dateStr,
                        startTime = event.start.dateTime?.toString(),
                        endTime = event.end?.dateTime?.toString()
                    )
                }

                dao.insertEvents(entities)

                // Deliver the raw Event objects back on the main thread for immediate UI updates
                withContext(Dispatchers.Main) {
                    onEventsFetched(items)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity, "Failed to fetch Google events", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
