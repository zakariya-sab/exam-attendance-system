package uiz.attendance.examattendance.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import uiz.attendance.examattendance.model.ExamBlock
import uiz.attendance.examattendance.model.PresentStudentDto
import uiz.attendance.examattendance.model.ScanRequest

interface ApiService {
    @GET("api/exam-blocks")
    suspend fun getExamBlocks(): List<ExamBlock>

    @POST("api/attendance/scan")
    suspend fun scanAttendance(@Body request: ScanRequest): Response<PresentStudentDto>
}
