import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';

import { ExamBlockService } from './exam-block.service';

describe('ExamBlockService', () => {
  let service: ExamBlockService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient()],
    });
    service = TestBed.inject(ExamBlockService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
