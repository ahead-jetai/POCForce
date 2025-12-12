import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import { Card } from '../components/Card';
import { Button } from '../components/Button';
import { Badge } from '../components/Badge';
import type { Poc, DashboardSummary, Phase, Status } from '../types';

export const Dashboard: React.FC = () => {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [pocs, setPocs] = useState<Poc[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [summaryData, pocsData] = await Promise.all([
        api.getDashboardSummary(),
        api.getPocs(),
      ]);
      setSummary(summaryData);
      setPocs(pocsData);
      setError('');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
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

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-xl text-gray-600">Loading dashboard...</div>
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
    <div className="space-y-8">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
        <Button onClick={() => navigate('/pocs/new')}>Create New POC</Button>
      </div>

      {/* Summary Cards */}
      {summary && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <Card>
            <div className="text-sm font-medium text-gray-600 mb-1">Active POCs</div>
            <div className="text-3xl font-bold text-gray-900">{summary.activePocCount}</div>
          </Card>
          <Card>
            <div className="text-sm font-medium text-gray-600 mb-1">At Risk</div>
            <div className="text-3xl font-bold text-danger-600">{summary.atRiskPocCount}</div>
          </Card>
          <Card>
            <div className="text-sm font-medium text-gray-600 mb-1">Total Pipeline Value</div>
            <div className="text-3xl font-bold text-success-600">
              {formatCurrency(summary.totalPipelineValue)}
            </div>
          </Card>
          <Card>
            <div className="text-sm font-medium text-gray-600 mb-2">POCs by Phase</div>
            <div className="space-y-1 text-sm">
              {Object.entries(summary.pocsByPhase).map(([phase, count]) => (
                <div key={phase} className="flex justify-between">
                  <span className="text-gray-700">{formatPhase(phase as Phase)}:</span>
                  <span className="font-semibold text-gray-900">{count}</span>
                </div>
              ))}
            </div>
          </Card>
        </div>
      )}

      {/* POC List Table */}
      <Card>
        <div className="mb-4">
          <h2 className="text-xl font-semibold text-gray-900">Your POCs</h2>
        </div>

        {pocs.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-gray-600 mb-4">No POCs found. Create your first POC to get started.</p>
            <Button onClick={() => navigate('/pocs/new')}>Create POC</Button>
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
                {pocs.map((poc) => (
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
