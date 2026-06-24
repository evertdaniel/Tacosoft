import { useState, FormEvent } from 'react';
import {
  useActiveCashRegister,
  useOpenCashRegister,
  useCloseCashRegister,
  useXReport,
  useZReport,
} from '@/hooks/useCash';
import { Loading } from '@/components/feedback/Loading';
import { ErrorState } from '@/components/feedback/ErrorState';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { OpenCashRegisterForm } from '../components/OpenCashRegisterForm';
import { XReportView } from '../components/XReportView';
import { ZReportView } from '../components/ZReportView';
import type { OpenCashRegisterBody, ZReportDto } from '@/types/domain.types';

export function CashRegisterPage() {
  const [showCloseForm, setShowCloseForm] = useState(false);
  const [closingAmount, setClosingAmount] = useState('');
  const [closeError, setCloseError] = useState<string | null>(null);
  const [closedReport, setClosedReport] = useState<ZReportDto | null>(null);

  const {
    data: activeRegister,
    isLoading: activeLoading,
    isError: activeError,
    error: activeErrorObj,
    refetch: refetchActive,
  } = useActiveCashRegister();
  const { data: xReport } = useXReport({ enabled: activeRegister?.status === 'OPEN' });
  const { data: zReport } = useZReport();
  const openRegister = useOpenCashRegister();
  const closeRegister = useCloseCashRegister();

  function handleOpen(body: OpenCashRegisterBody) {
    openRegister.mutate(body, {
      onSuccess: () => {
        refetchActive();
      },
    });
  }

  function handleCloseSubmit(event: FormEvent) {
    event.preventDefault();
    setCloseError(null);

    const parsedAmount = Number(closingAmount);
    if (!closingAmount || Number.isNaN(parsedAmount)) {
      setCloseError('Amount is required');
      return;
    }

    if (parsedAmount < 0) {
      setCloseError('Amount must be zero or positive');
      return;
    }

    if (!activeRegister) return;

    closeRegister.mutate(
      { id: activeRegister.id, body: { closingAmount: parsedAmount } },
      {
        onSuccess: (data) => {
          setClosedReport(data);
          setShowCloseForm(false);
          setClosingAmount('');
          refetchActive();
        },
      }
    );
  }

  if (activeLoading) {
    return <Loading message="Loading cash register…" />;
  }

  if (activeError) {
    const message = activeErrorObj?.message ?? 'Failed to load cash register.';
    if (message.toLowerCase().includes('no active register')) {
      return (
        <div className="space-y-6">
          <h1 className="text-2xl font-bold text-neutral-900">Cash register</h1>
          <OpenCashRegisterForm
            onSubmit={handleOpen}
            onCancel={() => {}}
            isSubmitting={openRegister.isPending}
          />
        </div>
      );
    }

    return (
      <ErrorState
        title="Cash register unavailable"
        message={message}
        onRetry={() => refetchActive()}
      />
    );
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-neutral-900">Cash register</h1>

      {activeRegister && (
        <div className="rounded-xl border border-neutral-200 bg-white p-5 shadow-sm">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <h2 className="text-lg font-semibold text-neutral-900">Active register</h2>
              <p className="text-sm text-neutral-500">Register: {activeRegister.id}</p>
              <p className="text-sm text-neutral-500">
                Opened: {new Date(activeRegister.openedAt).toLocaleString()}
              </p>
            </div>
            <Button onClick={() => setShowCloseForm((prev) => !prev)} data-testid="close-register-toggle">
              {showCloseForm ? 'Cancel' : 'Close register'}
            </Button>
          </div>

          {showCloseForm && (
            <form onSubmit={handleCloseSubmit} className="mt-4 space-y-4 rounded-lg bg-neutral-50 p-4">
              <Input
                id="closing-amount"
                type="number"
                label="Closing amount"
                value={closingAmount}
                onChange={(event) => setClosingAmount(event.target.value)}
                disabled={closeRegister.isPending}
                error={closeError ?? undefined}
                placeholder="0"
              />
              <div className="flex justify-end">
                <Button type="submit" disabled={closeRegister.isPending}>
                  {closeRegister.isPending ? 'Closing…' : 'Confirm close'}
                </Button>
              </div>
            </form>
          )}
        </div>
      )}

      {xReport && activeRegister?.status === 'OPEN' && <XReportView report={xReport} />}

      {closedReport && <ZReportView report={closedReport} />}

      {!closedReport && zReport && <ZReportView report={zReport} />}
    </div>
  );
}
