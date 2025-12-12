import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import { Card } from '../components/Card';
import { Button } from '../components/Button';
import { Badge } from '../components/Badge';
import { Input } from '../components/Input';
import type { Poc, User, Phase, Status } from '../types';

export const PocList: React.FC = () => {
  const navigate = useNavigate();
  const [pocs, setPocs] = useState<Poc[]>([]);
  const [filteredPocs, setFilteredPocs] = useState<Poc[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Filter states
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedPhase, setSelectedPhase] = useState<Phase | ''>('');
  const [selectedStatus, setSelectedStatus] = useState<Status | ''>('');
  const [selectedOwnerId, setSelectedOwnerId] = useState<number | ''>('');

  useEffect(() => {
    loadData();
  }, []);

  useEffect(() => {
    applyFilters();
  }, [pocs, searchTerm, selectedPhase, selectedStatus, selectedOwnerId]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [pocsData, usersData] = await Promise.all([
        api.getPocs(),
        api.getUsers(),
      ]);
      setPocs(pocsData);
      setUsers(usersData);
      setError('');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load POCs');
    } finally {
      setLoading(false);
    }
  };

  const applyFilters = () => {
    let filtered = [...pocs];

    // Search filter
    if (searchTerm) {
      const search = searchTerm.toLowerCase();
      filtered = filtered.filter(
        (poc) =>
          poc.customerName.toLowerCase().includes(search) ||
          poc.title.toLowerCase().includes(search)
      );
    }

    // Phase filter
    if (selectedPhase) {
      filtered = filtered.filter((poc) => poc.currentPhase === selectedPhase);
    }

    // Status filter
    if (selectedStatus) {
      filtered = filtered.filter((poc) => poc.status === selectedStatus);
    }

    // Owner filter
    if (selectedOwnerId) {
      filtered = filtered.filter((poc) => poc.ownerId === selectedOwnerId);
    }

    setFilteredPocs(filtered);
  };

  const clearFilters = () => {
    setSearchTerm('');
    setSelectedPhase('');
    setSelectedStatus('');
    setSelectedOwnerId('');
  };

  const getPhaseColor = (phase: Phase): 'blue' | 'yellow' | 'purple' | 'green' | 'red' => {
    switch (phase) {
      case 'DISCOVERY':
        return 'blue';
      case 'PLANNING':
        return 'yellow';
      case 'EXECUTION':
        return 'purple';
      case 'VALIDATION':
        return 'green';
      case 'CLOSED_WON':
        return 'green';
      case 'CLOSED_LOST':
        return 'red';
    }
  };

  const getStatusColor = (status: Status): 'blue' | 'yellow' | 'red' => {
    switch (status) {
      case 'ACTIVE':
        return 'blue';
      case 'AT_RISK':
        return 'red';
      case 'CLOSED':
        return 'yellow';
    }
  };

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const formatPhase = (phase: Phase) => {
    return phase.replace(/_/g, ' ');
  };

  const getOwnerName = (ownerId: number) => {
    const owner = users.find((u) => u.id === ownerId);
    return owner ? owner.name : 'Unknown';
  };

  const phases: Phase[] = ['DISCOVERY', 'PLANNING', 'EXECUTION', 'VALIDATION', 'CLOSED_WON', 'CLOSED_LOST'];
  const statuses: Status[] = ['ACTIVE', 'AT_RISK', 'CLOSED'];

  const hasActiveFilters = searchTerm || selectedPhase || selectedStatus || selectedOwnerId;

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-xl text-gray-600">Loading POCs...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-danger-50 border border-danger-200 text-danger-700 px-4 py-3 rounded-lg">
        {error}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-gray-900">All POCs</h1>
        <Button onClick={() => navigate('/pocs/new')}>Create New POC</Button>
      </div>

      {/* Filters */}
      <Card>
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-semibold text-gray-900">Filters</h3>
            {hasActiveFilters && (
              <Button variant="secondary" size="sm" onClick={clearFilters}>
                Clear All Filters
              </Button>
            )}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <Input
              label="Search"
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Customer or POC title..."
            />

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Phase
              </label>
              <select
                value={selectedPhase}
                onChange={(e) => setSelectedPhase(e.target.value as Phase | '')}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              >
                <option value="">All Phases</option>
                {phases.map((phase) => (
                  <option key={phase} value={phase}>
                    {formatPhase(phase)}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Status
              </label>
              <select
                value={selectedStatus}
                onChange={(e) => setSelectedStatus(e.target.value as Status | '')}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              >
                <option value="">All Statuses</option>
                {statuses.map((status) => (
                  <option key={status} value={status}>
                    {status}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Owner
              </label>
              <select
                value={selectedOwnerId}
                onChange={(e) => setSelectedOwnerId(e.target.value ? parseInt(e.target.value) : '')}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              >
                <option value="">All Owners</option>
                {users.map((user) => (
                  <option key={user.id} value={user.id}>
                    {user.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="text-sm text-gray-600">
            Showing {filteredPocs.length} of {pocs.length} POCs
          </div>
        </div>
      </Card>

      {/* POC List Table */}
      <Card>
        {filteredPocs.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-gray-600 mb-4">
              {hasActiveFilters ? 'No POCs match your filters.' : 'No POCs found. Create your first POC to get started.'}
            </p>
            {hasActiveFilters ? (
              <Button variant="secondary" onClick={clearFilters}>
                Clear Filters
              </Button>
            ) : (
              <Button onClick={() => navigate('/pocs/new')}>Create POC</Button>
            )}
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Customer
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    POC Title
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Phase
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Owner
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Deal Value
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    End Date
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredPocs.map((poc) => (
                  <tr
                    key={poc.id}
                    className="hover:bg-gray-50 cursor-pointer"
                    onClick={() => navigate(`/pocs/${poc.id}`)}
                  >
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      {poc.customerName}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-700">
                      {poc.title}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <Badge color={getPhaseColor(poc.currentPhase)}>
                        {formatPhase(poc.currentPhase)}
                      </Badge>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <Badge color={getStatusColor(poc.status)}>{poc.status}</Badge>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-700">
                      {getOwnerName(poc.ownerId)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-700">
                      {formatCurrency(poc.dealValue)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-700">
                      {formatDate(poc.endDate)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                      <Button
                        variant="secondary"
                        size="sm"
                        onClick={(e) => {
                          e.stopPropagation();
                          navigate(`/pocs/${poc.id}`);
                        }}
                      >
                        View
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
};
