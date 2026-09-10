import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { ExamBlockForm } from './exam-block-form';

describe('ExamBlockForm', () => {
  let component: ExamBlockForm;
  let fixture: ComponentFixture<ExamBlockForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExamBlockForm],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(ExamBlockForm);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('modules', []);
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
