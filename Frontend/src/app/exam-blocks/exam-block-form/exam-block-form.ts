import { Component, computed, effect, inject, input, output, signal } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { ExamBlockService } from '../../services/exam-block.service';
import { ExamBlock, ExamBlockInput, SessionType } from '../../models/exam-block.model';
import { Module } from '../../models/module.model';

const CREATE_MODE_DEFAULTS = {
  moduleId: null,
  sessionType: 'NORMAL' as SessionType,
  scheduledTime: '',
  durationMinutes: null,
  year: null,
};

type ValidatedField = 'moduleId' | 'sessionType' | 'scheduledTime' | 'durationMinutes' | 'year';

const FIELD_VALIDATION_MESSAGES: Record<ValidatedField, Record<string, string>> = {
  moduleId: { required: 'must not be null' },
  sessionType: { required: 'must not be null' },
  scheduledTime: { required: 'must not be null' },
  durationMinutes: {
    required: 'must not be null',
    min: 'duration must be at least 1 minute',
    max: 'duration must be at most 1440 minutes',
    integer: 'duration must be a whole number of minutes',
  },
  year: {
    required: 'must not be null',
    min: 'exam year must be at least 1400',
  },
};

function integerValidator(control: AbstractControl<number | null>): ValidationErrors | null {
  const value = control.value;
  return value === null || Number.isInteger(value) ? null : { integer: true };
}

@Component({
  selector: 'app-exam-block-form',
  imports: [ReactiveFormsModule],
  templateUrl: './exam-block-form.html',
})
export class ExamBlockForm {
  private readonly examBlockService = inject(ExamBlockService);

  readonly modules = input.required<Module[]>();
  readonly examBlockToEdit = input<ExamBlock | null>(null);
  readonly saved = output<void>();

  protected readonly submitting = signal(false);
  protected readonly fieldErrors = signal<Record<string, string> | null>(null);
  protected readonly formError = signal<string | null>(null);
  protected readonly showValidationSummary = signal(false);
  protected readonly isEditing = computed(() => this.examBlockToEdit() !== null);

  protected readonly form = new FormGroup({
    moduleId: new FormControl<number | null>(null, { validators: [Validators.required] }),
    sessionType: new FormControl<SessionType>('NORMAL', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    scheduledTime: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    durationMinutes: new FormControl<number | null>(null, {
      validators: [Validators.required, Validators.min(1), Validators.max(1440), integerValidator],
    }),
    year: new FormControl<number | null>(null, {
      validators: [Validators.required, Validators.min(1400)],
    }),
  });

  constructor() {
    effect(() => {
      const examBlock = this.examBlockToEdit();

      this.fieldErrors.set(null);
      this.formError.set(null);
      this.showValidationSummary.set(false);

      if (examBlock) {
        this.form.setValue({
          moduleId: examBlock.moduleId,
          sessionType: examBlock.sessionType,
          scheduledTime: examBlock.scheduledTime.slice(0, 16),
          durationMinutes: examBlock.durationMinutes,
          year: examBlock.year,
        });
      } else {
        this.form.reset(CREATE_MODE_DEFAULTS);
      }
    });
  }

  protected onSubmit(): void {
    if (this.submitting()) {
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.showValidationSummary.set(true);
      return;
    }

    const { moduleId, sessionType, scheduledTime, durationMinutes, year } = this.form.getRawValue();
    const payload: ExamBlockInput = {
      moduleId: moduleId!,
      sessionType,
      scheduledTime: this.toApiDateTime(scheduledTime),
      durationMinutes: durationMinutes!,
      year: year!,
    };

    this.fieldErrors.set(null);
    this.formError.set(null);
    this.showValidationSummary.set(false);
    this.submitting.set(true);

    const editingId = this.examBlockToEdit()?.id;
    const request = editingId
      ? this.examBlockService.updateExamBlock(editingId, payload)
      : this.examBlockService.createExamBlock(payload);

    request.subscribe({
      next: () => {
        this.submitting.set(false);
        this.form.reset(CREATE_MODE_DEFAULTS);
        this.saved.emit();
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.applyError(err);
      },
    });
  }

  protected fieldError(field: ValidatedField): string | null {
    const serverMessage = this.fieldErrors()?.[field];
    if (serverMessage) {
      return serverMessage;
    }

    const control = this.form.controls[field];
    if (!control.touched || !control.errors) {
      return null;
    }

    const messages = FIELD_VALIDATION_MESSAGES[field];
    for (const key of Object.keys(messages)) {
      if (control.errors[key]) {
        return messages[key];
      }
    }

    return null;
  }

  private applyError(err: HttpErrorResponse): void {
    const body: unknown = err.error;

    if (this.isFieldMap(err.status, body)) {
      this.fieldErrors.set(body);
      return;
    }

    if (this.isSingleErrorMessage(body)) {
      this.formError.set(body.error);
      return;
    }

    this.formError.set('Failed to save exam block. Please try again.');
  }

  private isFieldMap(status: number, body: unknown): body is Record<string, string> {
    return (
      status === 400 &&
      !!body &&
      typeof body === 'object' &&
      typeof (body as Record<string, unknown>)['error'] !== 'string'
    );
  }

  private isSingleErrorMessage(body: unknown): body is { error: string } {
    return (
      !!body && typeof body === 'object' && typeof (body as Record<string, unknown>)['error'] === 'string'
    );
  }

  private toApiDateTime(value: string): string {
    return value.length === 16 ? `${value}:00` : value;
  }
}
