package uiz.attendance.examattendance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import uiz.attendance.examattendance.ui.AppScreen
import uiz.attendance.examattendance.ui.ExamBlockListScreen
import uiz.attendance.examattendance.ui.QrScanScreen
import uiz.attendance.examattendance.ui.theme.ExamAttendanceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExamAttendanceTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.ExamBlockList) }

    when (val screen = currentScreen) {
        AppScreen.ExamBlockList -> {
            ExamBlockListScreen(
                modifier = modifier.fillMaxSize(),
                onStartScanning = { examBlock -> currentScreen = AppScreen.Scanning(examBlock) }
            )
        }

        is AppScreen.Scanning -> {
            QrScanScreen(
                examBlock = screen.examBlock,
                modifier = modifier.fillMaxSize(),
                onBack = { currentScreen = AppScreen.ExamBlockList }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ExamAttendanceTheme {
        MainScreen()
    }
}