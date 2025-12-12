// Domain enums
export type Phase = 'DISCOVERY' | 'PLANNING' | 'EXECUTION' | 'VALIDATION' | 'CLOSED_WON' | 'CLOSED_LOST';
export type Status = 'ACTIVE' | 'AT_RISK' | 'CLOSED';
export type UserRole = 'SE' | 'CSE' | 'SALES_ENGINEER' | 'MANAGER';

// User
export interface User {
  id: number;
  name: string;
  email: string;
  role: UserRole;
}

// Requirement
export interface Requirement {
  id: number;
  pocId: number;
  phase: Phase;
  description: string;
  completed: boolean;
  completedAt: string | null;
  notes: string | null;
  displayOrder: number;
}

// POC
export interface Poc {
  id: number;
  customerName: string;
  title: string;
  description: string | null;
  dealValue: number;
  projectedCloseDate: string;
  kickoffDate: string;
  endDate: string;
  currentPhase: Phase;
  ownerId: number;
  status: Status;
  createdAt: string;
  updatedAt: string;
}

// POC Detail (includes owner details)
export interface PocDetail extends Poc {
  owner: User;
}

// Dashboard Summary
export interface DashboardSummary {
  activePocCount: number;
  atRiskPocCount: number;
  totalPipelineValue: number;
  pocsByPhase: Record<Phase, number>;
}

// DTOs for API requests
export interface CreatePocRequest {
  customerName: string;
  title: string;
  description?: string;
  dealValue: number;
  projectedCloseDate: string;
  kickoffDate: string;
  endDate: string;
  ownerId: number;
}

export interface UpdatePocRequest {
  customerName?: string;
  title?: string;
  description?: string;
  dealValue?: number;
  projectedCloseDate?: string;
  kickoffDate?: string;
  endDate?: string;
  ownerId?: number;
}

export interface ClosePocRequest {
  outcome: 'WON' | 'LOST';
  notes?: string;
}

export interface UpdateRequirementRequest {
  completed?: boolean;
  notes?: string;
}

// Auth DTOs
export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: User;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  role: UserRole;
}

// Auth Context
export interface AuthContextType {
  user: User | null;
  token: string | null;
  login: (email: string, password: string) => Promise<void>;
  register: (name: string, email: string, password: string, role: UserRole) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
}
