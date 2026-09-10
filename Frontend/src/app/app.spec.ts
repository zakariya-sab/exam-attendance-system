import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';

import { App } from './app';
import { routes } from './app.routes';

describe('App', () => {
  beforeEach(async () => {
    sessionStorage.clear();
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter(routes)],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render the exam blocks heading when logged in', async () => {
    sessionStorage.setItem('authToken', 'fake-token');
    const harness = await RouterTestingHarness.create('/exam-blocks');
    const compiled = harness.routeNativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('Exam Blocks');
  });

  it('should redirect to login when not logged in', async () => {
    const harness = await RouterTestingHarness.create('/exam-blocks');
    expect(harness.routeDebugElement).toBeTruthy();
    const compiled = harness.routeNativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('Log in');
  });
});
