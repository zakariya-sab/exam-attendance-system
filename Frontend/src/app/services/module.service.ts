import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Module } from '../models/module.model';

const API_BASE_URL = 'http://localhost:8080';

@Service()
export class ModuleService {
  private readonly http = inject(HttpClient);

  getModules(): Observable<Module[]> {
    return this.http.get<Module[]>(`${API_BASE_URL}/api/modules`);
  }
}
