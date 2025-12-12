import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import { Card } from '../components/Card';
import { Button } from '../components/Button';
import { Input } from '../components/Input';
import type { User, CreatePocRequest, UpdatePocRequest } from '../types';

export const PocForm: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isEditMode = !!id;

  const [formData, setFormData] = useState<CreatePocRequest>({
    customerName: '',
    title: '',
    description: '',
    dealValue: 0,
    projectedCloseDate: '',
    kickoffDate: '',
    endDate: '',
    ownerId: 0,
  });

  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    loadUsers();
    if (isEditMode) {
      loadPoc();
    }
  }, [id]);

  const loadUsers = async () => {
    try {
      const usersData = await api.getUsers();
      setUsers(usersData);
      if (!isEditMode && usersData.length > 0) {
        setFormData((prev) => ({ ...prev, ownerId: usersData[0].id }));
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load users');
    }
  };

  const loadPoc = async () => {
    try {
      setLoading(true);
      const poc = await api.getPocById(parseInt(id!));
      setFormData({
        customerName: poc.customerName,
        title: poc.title,
        description: poc.description || '',
        dealValue: poc.dealValue,
        projectedCloseDate: poc.projectedCloseDate,
        kickoffDate: poc.kickoffDate,
        endDate: poc.endDate,
        ownerId: poc.ownerId,
      });
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load POC');
    } finally {
      setLoading(false);
    }
  };

  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};

    if (!formData.customerName.trim()) {
      errors.customerName = 'Customer name is required';
    }
    if (!formData.title.trim()) {
      errors.title = 'POC title is required';
    }
    if (formData.dealValue <= 0) {
      errors.dealValue = 'Deal value must be greater than 0';
    }
    if (!formData.projectedCloseDate) {
      errors.projectedCloseDate = 'Projected close date is required';
    }
    if (!formData.kickoffDate) {
      errors.kickoffDate = 'Kickoff date is required';
    }
    if (!formData.endDate) {
      errors.endDate = 'End date is required';
    }
    if (!formData.ownerId) {
      errors.ownerId = 'Owner is required';
    }

    // Date validations
    if (formData.kickoffDate && formData.endDate) {
      const kickoff = new Date(formData.kickoffDate);
      const end = new Date(formData.endDate);
      if (end <= kickoff) {
        errors.endDate = 'End date must be after kickoff date';
      }
    }

    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!validateForm()) {
      return;
    }

    try {
      setLoading(true);
      if (isEditMode) {
        const updateData: UpdatePocRequest = {
          customerName: formData.customerName,
          title: formData.title,
          description: formData.description || undefined,
          dealValue: formData.dealValue,
          projectedCloseDate: formData.projectedCloseDate,
          kickoffDate: formData.kickoffDate,
          endDate: formData.endDate,
          ownerId: formData.ownerId,
        };
        await api.updatePoc(parseInt(id!), updateData);
        navigate(`/pocs/${id}`);
      } else {
        const newPoc = await api.createPoc(formData);
        navigate(`/pocs/${newPoc.id}`);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || `Failed to ${isEditMode ? 'update' : 'create'} POC`);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (field: keyof CreatePocRequest, value: any) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    // Clear validation error for this field
    if (validationErrors[field]) {
      setValidationErrors((prev) => {
        const newErrors = { ...prev };
        delete newErrors[field];
        return newErrors;
      });
    }
  };

  const handleCancel = () => {
    if (isEditMode) {
      navigate(`/pocs/${id}`);
    } else {
      navigate('/');
    }
  };

  if (loading && isEditMode) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-xl text-gray-600">Loading POC...</div>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto">
      <Card>
        <h1 className="text-2xl font-bold text-gray-900 mb-6">
          {isEditMode ? 'Edit POC' : 'Create New POC'}
        </h1>

        {error && (
          <div className="bg-danger-50 border border-danger-200 text-danger-700 px-4 py-3 rounded-lg mb-6">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          <Input
            label="Customer Name"
            type="text"
            value={formData.customerName}
            onChange={(e) => handleChange('customerName', e.target.value)}
            required
            placeholder="Acme Corporation"
            error={validationErrors.customerName}
          />

          <Input
            label="POC Title"
            type="text"
            value={formData.title}
            onChange={(e) => handleChange('title', e.target.value)}
            required
            placeholder="Q4 Enterprise Platform Evaluation"
            error={validationErrors.title}
          />

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              POC Description
            </label>
            <textarea
              value={formData.description}
              onChange={(e) => handleChange('description', e.target.value)}
              placeholder="Detailed description of the POC objectives and scope..."
              rows={4}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            />
          </div>

          <Input
            label="Deal Value ($)"
            type="number"
            value={formData.dealValue}
            onChange={(e) => handleChange('dealValue', parseFloat(e.target.value) || 0)}
            required
            min="0"
            step="1"
            placeholder="100000"
            error={validationErrors.dealValue}
          />

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <Input
              label="Kickoff Date"
              type="date"
              value={formData.kickoffDate}
              onChange={(e) => handleChange('kickoffDate', e.target.value)}
              required
              error={validationErrors.kickoffDate}
            />

            <Input
              label="End Date"
              type="date"
              value={formData.endDate}
              onChange={(e) => handleChange('endDate', e.target.value)}
              required
              error={validationErrors.endDate}
            />

            <Input
              label="Projected Close Date"
              type="date"
              value={formData.projectedCloseDate}
              onChange={(e) => handleChange('projectedCloseDate', e.target.value)}
              required
              error={validationErrors.projectedCloseDate}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Assigned Owner <span className="text-danger-500">*</span>
            </label>
            <select
              value={formData.ownerId}
              onChange={(e) => handleChange('ownerId', parseInt(e.target.value))}
              required
              className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent ${
                validationErrors.ownerId ? 'border-danger-500' : 'border-gray-300'
              }`}
            >
              <option value={0}>Select owner...</option>
              {users.map((user) => (
                <option key={user.id} value={user.id}>
                  {user.name} ({user.role})
                </option>
              ))}
            </select>
            {validationErrors.ownerId && (
              <p className="mt-1 text-sm text-danger-600">{validationErrors.ownerId}</p>
            )}
          </div>

          <div className="flex gap-4 pt-4">
            <Button type="submit" disabled={loading}>
              {loading ? 'Saving...' : isEditMode ? 'Update POC' : 'Create POC'}
            </Button>
            <Button type="button" variant="secondary" onClick={handleCancel}>
              Cancel
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
};
