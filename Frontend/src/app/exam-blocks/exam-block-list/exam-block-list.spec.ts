import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { ExamBlockList } from './exam-block-list';

describe('ExamBlockList', () => {
  let component: ExamBlockList;
  let fixture: ComponentFixture<ExamBlockList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExamBlockList],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(ExamBlockList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
