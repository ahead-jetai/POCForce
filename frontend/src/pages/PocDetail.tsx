import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import { Card } from '../components/Card';
import { Button } from '../components/Button';
import { Badge } from '../components/Badge';
import { Input } from '../components/Input';
import type { PocDetail as PocDetailType, Requirement, Phase } from '../types';

export const PocDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [poc, setPoc] = useState<PocDetailType | null>(null);
  const [requirements, setRequirements] = useState<Requirement[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [updatingRequirement, setUpdatingRequirement] = useState<number | null>(null);
  const [showCloseDialog, setShowCloseDialog] = useState(false);
  const [closeOutcome, setCloseOutcome] = useState<'WON' | 'LOST'>('WON');
  const [closeNotes, setCloseNotes] = useState('');

  useEffect(() => {
    if (id) {
      loadPocData();
    }
  }, [id]);

  const loadPocData = async () => {
    try {
      setLoading(true);
      const pocId = parseInt(id!);
      const [pocData, requirementsData] = await Promise.all([
        api.getPocById(pocId),
        api.getRequirements(pocId),
      ]);
      setPoc(pocData);
      setRequirements(requirementsData);
      setError('');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load POC details');
    } finally {
      setLoading(false);
    }
  };

  const handleToggleRequirement = async (requirement: Requirement) => {
    try {
      setUpdatingRequirement(requirement.id);
      const updated = await api.updateRequirement(parseInt(id!), requirement.id, {
        completed: !requirement.completed,
      });
      setRequirements((prev) =>
        prev.map((req) => (req.id === updated.id ? updated : req))
      );
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to update requirement');
    } finally {
      setUpdatingRequirement(null);
    }
  };

  const handleUpdateNotes = async (requirement: Requirement, notes: string) => {
    try {
      const updated = await api.updateRequirement(parseInt(id!), requirement.id, {
        notes: notes || undefined,
      });
      setRequirements((prev) =>
        prev.map((req) => (req.id === updated.id ? updated : req))
      );
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to update notes');
    }
  };

  const handleAdvancePhase = async () => {
    if (!poc) return;
    if (!window.confirm('Are you sure you want to advance to the next phase?')) return;

    try {
      const updated = await api.advancePoc(poc.id);
      setPoc({ ...poc, ...updated });
      await loadPocData(); // Reload to get new requirements
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to advance phase');
    }
  };

  const handleClosePoc = async () => {
    if (!poc) return;

    try {
      const updated = await api.closePoc(poc.id, {
        outcome: closeOutcome,
        notes: closeNotes || undefined,
      });
      setPoc({ ...poc, ...updated });
      setShowCloseDialog(false);
      setCloseNotes('');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to close POC');
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
      month: 'long',
      day: 'numeric',
    });
  };

  const formatPhase = (phase: Phase) => {
    return phase.replace(/_/g, ' ');
  };

  const isPhaseTerminal = (phase: Phase) => {
    return phase === 'CLOSED_WON' || phase === 'CLOSED_LOST';
  };

  const canAdvancePhase = () => {
    if (!poc || isPhaseTerminal(poc.currentPhase)) return false;
    const currentPhaseReqs = requirements.filter((req) => req.phase === poc.currentPhase);
    return currentPhaseReqs.every((req) => req.completed);
  };

  const getCompletionPercentage = () => {
    if (!poc) return 0;
    const currentPhaseReqs = requirements.filter((req) => req.phase === poc.currentPhase);
    if (currentPhaseReqs.length === 0) return 100;
    const completed = currentPhaseReqs.filter((req) => req.completed).length;
    return Math.round((completed / currentPhaseReqs.length) * 100);
  };

  const phases: Phase[] = ['DISCOVERY', 'PLANNING', 'EXECUTION', 'VALIDATION'];

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-xl text-gray-600">Loading POC details...</div>
      </div>
    );
  }

  if (error && !poc) {
    return (
      <div className="bg-danger-50 border border-danger-200 text-danger-700 px-4 py-3 rounded-lg">
        {error}
      </div>
    );
  }

  if (!poc) {
    return (
      <div className="text-center py-12">
        <p className="text-gray-600">POC not found</p>
      </div>
    );
  }

  const currentPhaseReqs = requirements.filter((req) => req.phase === poc.currentPhase);

  return (
    <div className="space-y-8">
      {error && (
        <div className="bg-danger-50 border border-danger-200 text-danger-700 px-4 py-3 rounded-lg">
          {error}
        </div>
      )}

      {/* Header */}
      <div className="flex justify-between items-start">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">{poc.customerName}</h1>
          <h2 className="text-xl text-gray-700 mb-4">{poc.title}</h2>
          <Badge color={getPhaseColor(poc.currentPhase)} size="lg">
            {formatPhase(poc.currentPhase)}
          </Badge>
        </div>
        <div className="flex gap-2">
          <Button variant="secondary" onClick={() => navigate(`/pocs/${poc.id}/edit`)}>
            Edit POC
          </Button>
          <Button variant="secondary" onClick={() => navigate('/pocs')}>
            Back to List
          </Button>
        </div>
      </div>

      {/* Business Metrics */}
      <Card>
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Business Metrics</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div>
            <div className="text-sm text-gray-600 mb-1">Deal Value</div>
            <div className="text-2xl font-bold text-gray-900">{formatCurrency(poc.dealValue)}</div>
          </div>
          <div>
            <div className="text-sm text-gray-600 mb-1">Projected Close Date</div>
            <div className="text-lg text-gray-900">{formatDate(poc.projectedCloseDate)}</div>
          </div>
          <div>
            <div className="text-sm text-gray-600 mb-1">Owner</div>
            <div className="text-lg text-gray-900">
              {poc.owner.name} ({poc.owner.role})
            </div>
          </div>
          <div>
            <div className="text-sm text-gray-600 mb-1">POC Kickoff Date</div>
            <div className="text-lg text-gray-900">{formatDate(poc.kickoffDate)}</div>
          </div>
          <div>
            <div className="text-sm text-gray-600 mb-1">POC End Date</div>
            <div className="text-lg text-gray-900">{formatDate(poc.endDate)}</div>
          </div>
          <div>
            <div className="text-sm text-gray-600 mb-1">Status</div>
            <div>
              <Badge
                color={
                  poc.status === 'ACTIVE' ? 'blue' : poc.status === 'AT_RISK' ? 'red' : 'yellow'
                }
              >
                {poc.status}
              </Badge>
            </div>
          </div>
        </div>
        {poc.description && (
          <div className="mt-6">
            <div className="text-sm text-gray-600 mb-1">Description</div>
            <div className="text-gray-900">{poc.description}</div>
          </div>
        )}
      </Card>

      {/* Phase Timeline */}
      <Card>
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Phase Timeline</h3>
        <div className="flex items-center justify-between">
          {phases.map((phase, index) => {
            const isPast =
              phases.indexOf(poc.currentPhase) > index ||
              isPhaseTerminal(poc.currentPhase);
            const isCurrent = poc.currentPhase === phase;
            return (
              <React.Fragment key={phase}>
                <div className="flex flex-col items-center flex-1">
                  <div
                    className={`w-10 h-10 rounded-full flex items-center justify-center font-bold ${
                      isCurrent
                        ? 'bg-primary-600 text-white'
                        : isPast
                        ? 'bg-success-500 text-white'
                        : 'bg-gray-300 text-gray-600'
                    }`}
                  >
                    {isPast ? '✓' : index + 1}
                  </div>
                  <div
                    className={`mt-2 text-sm font-medium ${
                      isCurrent ? 'text-primary-600' : isPast ? 'text-success-600' : 'text-gray-500'
                    }`}
                  >
                    {formatPhase(phase)}
                  </div>
                </div>
                {index < phases.length - 1 && (
                  <div
                    className={`flex-1 h-1 ${
                      isPast ? 'bg-success-500' : 'bg-gray-300'
                    }`}
                  />
                )}
              </React.Fragment>
            );
          })}
        </div>
      </Card>

      {/* Requirements Checklist */}
      {!isPhaseTerminal(poc.currentPhase) && (
        <Card>
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-lg font-semibold text-gray-900">
              Requirements - {formatPhase(poc.currentPhase)} Phase
            </h3>
            <div className="text-sm text-gray-600">
              {getCompletionPercentage()}% Complete
            </div>
          </div>
          <div className="space-y-4">
            {currentPhaseReqs.map((requirement) => (
              <div
                key={requirement.id}
                className="border border-gray-200 rounded-lg p-4 hover:bg-gray-50"
              >
                <div className="flex items-start gap-3">
                  <input
                    type="checkbox"
                    checked={requirement.completed}
                    onChange={() => handleToggleRequirement(requirement)}
                    disabled={updatingRequirement === requirement.id}
                    className="mt-1 h-5 w-5 text-primary-600 rounded border-gray-300 focus:ring-primary-500"
                  />
                  <div className="flex-1">
                    <div
                      className={`font-medium ${
                        requirement.completed ? 'line-through text-gray-500' : 'text-gray-900'
                      }`}
                    >
                      {requirement.description}
                    </div>
                    {requirement.completedAt && (
                      <div className="text-xs text-gray-500 mt-1">
                        Completed on {formatDate(requirement.completedAt)}
                      </div>
                    )}
                    <div className="mt-2">
                      <Input
                        label="Notes (optional)"
                        value={requirement.notes || ''}
                        onChange={(e) => setRequirements((prev) =>
                          prev.map((req) =>
                            req.id === requirement.id
                              ? { ...req, notes: e.target.value }
                              : req
                          )
                        )}
                        onBlur={(e) => handleUpdateNotes(requirement, e.target.value)}
                        placeholder="Add notes..."
                      />
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* Actions */}
      {!isPhaseTerminal(poc.currentPhase) && (
        <Card>
          <div className="flex gap-4">
            <Button
              onClick={handleAdvancePhase}
              disabled={!canAdvancePhase()}
            >
              Advance to Next Phase
            </Button>
            <Button variant="secondary" onClick={() => setShowCloseDialog(true)}>
              Close POC
            </Button>
          </div>
          {!canAdvancePhase() && (
            <p className="text-sm text-gray-600 mt-2">
              Complete all requirements in the current phase to advance.
            </p>
          )}
        </Card>
      )}

      {/* Close POC Dialog */}
      {showCloseDialog && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <Card className="max-w-md w-full">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Close POC</h3>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Outcome
                </label>
                <div className="flex gap-4">
                  <label className="flex items-center">
                    <input
                      type="radio"
                      value="WON"
                      checked={closeOutcome === 'WON'}
                      onChange={() => setCloseOutcome('WON')}
                      className="mr-2"
                    />
                    Won
                  </label>
                  <label className="flex items-center">
                    <input
                      type="radio"
                      value="LOST"
                      checked={closeOutcome === 'LOST'}
                      onChange={() => setCloseOutcome('LOST')}
                      className="mr-2"
                    />
                    Lost
                  </label>
                </div>
              </div>
              <Input
                label="Notes (optional)"
                value={closeNotes}
                onChange={(e) => setCloseNotes(e.target.value)}
                placeholder="Add closing notes..."
              />
              <div className="flex gap-2 justify-end">
                <Button variant="secondary" onClick={() => setShowCloseDialog(false)}>
                  Cancel
                </Button>
                <Button onClick={handleClosePoc}>Close POC</Button>
              </div>
            </div>
          </Card>
        </div>
      )}
    </div>
  );
};
