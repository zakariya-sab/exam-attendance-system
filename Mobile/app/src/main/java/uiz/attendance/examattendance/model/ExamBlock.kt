package uiz.attendance.examattendance.model

data class ExamBlock(
    val id: Long,
    val moduleName: String,
    val sessionType: String,
    val scheduledTime: String,
    val durationMinutes: Int,
    val year: Int
)
