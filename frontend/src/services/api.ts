import axios, { AxiosInstance } from 'axios';
import type {
  Poc,
  PocDetail,
  CreatePocRequest,
  UpdatePocRequest,
  ClosePocRequest,
  Requirement,
  UpdateRequirementRequest,
  User,
  DashboardSummary,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
} from '../types';

class ApiClient {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: '/api',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Add request interceptor to include auth token
    this.client.interceptors.request.use((config) => {
      const token = localStorage.getItem('token');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    });

    // Add response interceptor to handle auth errors
    this.client.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }
    );
  }

  // Auth endpoints
  async login(request: LoginRequest): Promise<LoginResponse> {
    const { data } = await this.client.post<LoginResponse>('/auth/login', request);
    return data;
  }

  async register(request: RegisterRequest): Promise<LoginResponse> {
    const { data } = await this.client.post<LoginResponse>('/auth/register', request);
    return data;
  }

  // POC endpoints
  async getPocs(params?: {
    phase?: string;
    ownerId?: number;
    status?: string;
    search?: string;
  }): Promise<Poc[]> {
    const { data } = await this.client.get<Poc[]>('/v1/pocs', { params });
    return data;
  }

  async getPocById(id: number): Promise<PocDetail> {
    const { data } = await this.client.get<PocDetail>(`/v1/pocs/${id}`);
    return data;
  }

  async createPoc(request: CreatePocRequest): Promise<Poc> {
    const { data } = await this.client.post<Poc>('/v1/pocs', request);
    return data;
  }

  async updatePoc(id: number, request: UpdatePocRequest): Promise<Poc> {
    const { data } = await this.client.put<Poc>(`/v1/pocs/${id}`, request);
    return data;
  }

  async deletePoc(id: number): Promise<void> {
    await this.client.delete(`/v1/pocs/${id}`);
  }

  async advancePoc(id: number): Promise<Poc> {
    const { data } = await this.client.post<Poc>(`/v1/pocs/${id}/advance`);
    return data;
  }

  async closePoc(id: number, request: ClosePocRequest): Promise<Poc> {
    const { data } = await this.client.post<Poc>(`/v1/pocs/${id}/close`, request);
    return data;
  }

  // Requirement endpoints
  async getRequirements(pocId: number): Promise<Requirement[]> {
    const { data } = await this.client.get<Requirement[]>(`/v1/pocs/${pocId}/requirements`);
    return data;
  }

  async updateRequirement(
    pocId: number,
    requirementId: number,
    request: UpdateRequirementRequest
  ): Promise<Requirement> {
    const { data } = await this.client.patch<Requirement>(
      `/v1/pocs/${pocId}/requirements/${requirementId}`,
      request
    );
    return data;
  }

  // User endpoints
  async getUsers(): Promise<User[]> {
    const { data } = await this.client.get<User[]>('/v1/users');
    return data;
  }

  async getUserById(id: number): Promise<User> {
    const { data } = await this.client.get<User>(`/v1/users/${id}`);
    return data;
  }

  // Dashboard endpoints
  async getDashboardSummary(): Promise<DashboardSummary> {
    const { data } = await this.client.get<DashboardSummary>('/v1/dashboard/summary');
    return data;
  }
}

export const api = new ApiClient();
