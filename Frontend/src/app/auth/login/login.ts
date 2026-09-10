import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
})
export class Login {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly submitting = signal(false);
  protected readonly loginError = signal<string | null>(null);

  protected readonly form = new FormGroup({
    username: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  protected onSubmit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    const { username, password } = this.form.getRawValue();

    this.loginError.set(null);
    this.submitting.set(true);

    this.authService.login(username, password).subscribe({
      next: (response) => {
        this.submitting.set(false);
        this.authService.saveToken(response.token);
        this.router.navigateByUrl('/exam-blocks');
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.loginError.set(err.error?.error ?? 'Failed to log in. Please try again.');
      },
    });
  }
}
