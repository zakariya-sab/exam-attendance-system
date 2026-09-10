package uiz.attendance.examattendance.ui

import uiz.attendance.examattendance.model.ExamBlock

sealed class AppScreen {
    object ExamBlockList : AppScreen()
    data class Scanning(val examBlock: ExamBlock) : AppScreen()
}
