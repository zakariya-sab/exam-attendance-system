import { Service, inject } from '@angular/core';
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ExamBlock, ExamBlockInput } from '../models/exam-block.model';

const API_BASE_URL = 'http://localhost:8080';

@Service()
export class ExamBlockService {
  private readonly http = inject(HttpClient);

  getExamBlocks(): Observable<ExamBlock[]> {
    return this.http.get<ExamBlock[]>(`${API_BASE_URL}/api/exam-blocks`);
  }

  createExamBlock(examBlock: ExamBlockInput): Observable<ExamBlock> {
    return this.http.post<ExamBlock>(`${API_BASE_URL}/api/exam-blocks`, examBlock);
  }

  updateExamBlock(id: number, examBlock: ExamBlockInput): Observable<ExamBlock> {
    return this.http.put<ExamBlock>(`${API_BASE_URL}/api/exam-blocks/${id}`, examBlock);
  }

  deleteExamBlock(id: number, force = false): Observable<void> {
    let params = new HttpParams();
    if (force) {
      params = params.set('force', true);
    }

    return this.http.delete<void>(`${API_BASE_URL}/api/exam-blocks/${id}`, { params });
  }

  exportAttendance(id: number): Observable<HttpResponse<Blob>> {
    return this.http.get(`${API_BASE_URL}/api/exam-blocks/${id}/attendance/export`, {
      responseType: 'blob',
      observe: 'response',
    });
  }
}
