import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';

import { ExamBlockService } from '../../services/exam-block.service';
import { ModuleService } from '../../services/module.service';
import { ExamBlock } from '../../models/exam-block.model';
import { Module } from '../../models/module.model';
import { ExamBlockForm } from '../exam-block-form/exam-block-form';

@Component({
  selector: 'app-exam-block-list',
  imports: [ExamBlockForm],
  templateUrl: './exam-block-list.html',
})
export class ExamBlockList implements OnInit {
  private readonly examBlockService = inject(ExamBlockService);
  private readonly moduleService = inject(ModuleService);

  protected readonly examBlocks = signal<ExamBlock[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly examBlockToEdit = signal<ExamBlock | null>(null);
  protected readonly deleteError = signal<string | null>(null);
  protected readonly deleteConfirmBlockId = signal<number | null>(null);
  protected readonly deleteConfirmMessage = signal<string | null>(null);

  protected readonly modules = signal<Module[]>([]);
  protected readonly modulesLoading = signal(true);
  protected readonly modulesError = signal<string | null>(null);

  protected readonly downloadingBlockId = signal<number | null>(null);
  protected readonly downloadErrors = signal<Record<number, string>>({});

  ngOnInit(): void {
    this.loadExamBlocks();
    this.loadModules();
  }

  protected loadExamBlocks(): void {
    this.loading.set(true);
    this.error.set(null);

    this.examBlockService.getExamBlocks().subscribe({
      next: (examBlocks) => {
        this.examBlocks.set(examBlocks);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load exam blocks. Please try again later.');
        this.loading.set(false);
      },
    });
  }

  private loadModules(): void {
    this.modulesLoading.set(true);
    this.modulesError.set(null);

    this.moduleService.getModules().subscribe({
      next: (modules) => {
        this.modules.set(modules);
        this.modulesLoading.set(false);
      },
      error: () => {
        this.modulesError.set('Failed to load modules. Please try again later.');
        this.modulesLoading.set(false);
      },
    });
  }

  protected onSaved(): void {
    this.examBlockToEdit.set(null);
    this.loadExamBlocks();
  }

  protected startEdit(examBlock: ExamBlock): void {
    this.deleteError.set(null);
    this.deleteConfirmBlockId.set(null);
    this.deleteConfirmMessage.set(null);
    this.examBlockToEdit.set(examBlock);
  }

  protected deleteExamBlock(examBlock: ExamBlock): void {
    this.deleteError.set(null);
    this.deleteConfirmBlockId.set(null);
    this.deleteConfirmMessage.set(null);

    this.examBlockService.deleteExamBlock(examBlock.id).subscribe({
      next: () => this.onDeleteSuccess(examBlock),
      error: (err: HttpErrorResponse) => {
        if (err.status === 409 && err.error?.error) {
          this.deleteConfirmBlockId.set(examBlock.id);
          this.deleteConfirmMessage.set(err.error.error);
          return;
        }

        this.deleteError.set('Failed to delete exam block. Please try again.');
      },
    });
  }

  protected deleteExamBlockForce(examBlock: ExamBlock): void {
    this.deleteError.set(null);

    this.examBlockService.deleteExamBlock(examBlock.id, true).subscribe({
      next: () => this.onDeleteSuccess(examBlock),
      error: () => {
        this.deleteError.set('Failed to delete exam block. Please try again.');
      },
    });
  }

  protected cancelDeleteConfirm(): void {
    this.deleteConfirmBlockId.set(null);
    this.deleteConfirmMessage.set(null);
  }

  private onDeleteSuccess(examBlock: ExamBlock): void {
    this.deleteConfirmBlockId.set(null);
    this.deleteConfirmMessage.set(null);
    if (this.examBlockToEdit()?.id === examBlock.id) {
      this.examBlockToEdit.set(null);
    }
    this.loadExamBlocks();
  }

  protected downloadAttendance(examBlock: ExamBlock): void {
    this.downloadingBlockId.set(examBlock.id);
    this.clearDownloadError(examBlock.id);

    this.examBlockService.exportAttendance(examBlock.id).subscribe({
      next: (response) => {
        this.downloadingBlockId.set(null);
        this.saveCsvResponse(response);
      },
      error: (err: HttpErrorResponse) => {
        this.downloadingBlockId.set(null);
        this.handleExportError(examBlock.id, err);
      },
    });
  }

  private clearDownloadError(blockId: number): void {
    const { [blockId]: _removed, ...rest } = this.downloadErrors();
    this.downloadErrors.set(rest);
  }

  private setDownloadError(blockId: number, message: string): void {
    this.downloadErrors.set({ ...this.downloadErrors(), [blockId]: message });
  }

  private saveCsvResponse(response: HttpResponse<Blob>): void {
    const blob = response.body;
    if (!blob) {
      return;
    }

    const filename = this.extractFilename(response.headers.get('Content-Disposition')) ?? 'attendance.csv';

    const objectUrl = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = objectUrl;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(objectUrl);
  }

  private extractFilename(contentDisposition: string | null): string | null {
    if (!contentDisposition) {
      return null;
    }

    const match = /filename="?([^";]+)"?/i.exec(contentDisposition);
    return match ? match[1] : null;
  }

  private handleExportError(blockId: number, err: HttpErrorResponse): void {
    const errorBody: unknown = err.error;

    if (errorBody instanceof Blob) {
      errorBody
        .text()
        .then((text) => {
          try {
            const parsed = JSON.parse(text);
            this.setDownloadError(
              blockId,
              typeof parsed?.error === 'string'
                ? parsed.error
                : 'Failed to export attendance. Please try again.',
            );
          } catch {
            this.setDownloadError(blockId, 'Failed to export attendance. Please try again.');
          }
        })
        .catch(() => {
          this.setDownloadError(blockId, 'Failed to export attendance. Please try again.');
        });
      return;
    }

    this.setDownloadError(blockId, 'Failed to export attendance. Please try again.');
  }
}
