package com.project362.sfuhive

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.project362.sfuhive.database.AssignmentViewModel
import com.project362.sfuhive.database.AssignmentViewModelFactory
import com.project362.sfuhive.ui.theme.SFUHiveTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var viewModelFactory: AssignmentViewModelFactory
    private lateinit var assignmentViewModel: AssignmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SFUHiveTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val loaded = prefs.getBoolean("assignments_loaded", false)


        viewModelFactory = Util.getViewModelFactory(this)
        assignmentViewModel =
            ViewModelProvider(this, viewModelFactory).get(AssignmentViewModel::class.java)


        assignmentViewModel.allAssignmentsLiveData.observe(this, Observer { it ->
            Log.d("DatabaseCheck", "Assignments count: ${it.size}")
        })

        // put all assignments into database on start if not there
//        if (!loaded) {
        lifecycleScope.launch {
            Util.getCanvasAssignments(this@MainActivity, this@MainActivity)
        }
//        } else {
//            Log.d("MainActivity", "Assignments already loaded — skipping fetch")
//        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SFUHiveTheme {
        Greeting("Android")
    }
}