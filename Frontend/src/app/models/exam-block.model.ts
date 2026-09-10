export type SessionType = 'NORMAL' | 'RATTRAPAGE';

export interface ExamBlock {
  id: number;
  moduleId: number;
  moduleCode: string;
  moduleName: string;
  sessionType: SessionType;
  scheduledTime: string;
  durationMinutes: number;
  year: number;
}

export interface ExamBlockInput {
  moduleId: number;
  sessionType: SessionType;
  scheduledTime: string;
  durationMinutes: number;
  year: number;
}
