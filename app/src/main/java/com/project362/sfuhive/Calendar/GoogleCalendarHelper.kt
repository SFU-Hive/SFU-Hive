package com.project362.sfuhive.Calendar

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import com.google.api.client.util.DateTime
import kotlinx.coroutines.*
import java.time.LocalDate

class GoogleCalendarHelper(
    private val activity: Activity,
    private val onEventsFetched: (List<Event>) -> Unit
) {
    companion object {
        const val RC_SIGN_IN = 1001
    }

    private lateinit var googleSignInClient: GoogleSignInClient
    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(CalendarScopes.CALENDAR_READONLY))
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }

    fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    fun signOut() {
        googleSignInClient.signOut().addOnCompleteListener {
            Toast.makeText(activity, "Signed out", Toast.LENGTH_SHORT).show()
        }
    }

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
                    .setMaxResults(2500) // Google Calendar limit
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute()
                val items = events.items

                withContext(Dispatchers.Main) {
                    onEventsFetched(items)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity, "Failed to fetch Google events", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}