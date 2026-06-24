import { useState } from 'react';
import { useUnpaidInvoices, useInvoices, usePayInvoice } from '@/hooks/useBilling';
import { Loading } from '@/components/feedback/Loading';
import { ErrorState } from '@/components/feedback/ErrorState';
import { Button } from '@/components/ui/Button';
import { InvoiceCard } from '../components/InvoiceCard';
import { PaymentModal } from '../components/PaymentModal';
import { InvoiceDto, PaymentBody } from '@/types/domain.types';

type InvoiceView = 'unpaid' | 'all';

export function InvoicesPage() {
  const [view, setView] = useState<InvoiceView>('unpaid');
  const [selectedInvoice, setSelectedInvoice] = useState<InvoiceDto | null>(null);

  const {
    data: unpaidInvoices,
    isLoading: unpaidLoading,
    isError: unpaidError,
    error: unpaidErrorObj,
    refetch: refetchUnpaid,
  } = useUnpaidInvoices();
  const {
    data: allInvoices,
    isLoading: allLoading,
    isError: allError,
    error: allErrorObj,
    refetch: refetchAll,
  } = useInvoices();
  const payInvoice = usePayInvoice();

  const invoices = view === 'unpaid' ? unpaidInvoices : allInvoices;
  const isLoading = view === 'unpaid' ? unpaidLoading : allLoading;
  const isError = view === 'unpaid' ? unpaidError : allError;
  const errorObj = view === 'unpaid' ? unpaidErrorObj : allErrorObj;
  const refetch = view === 'unpaid' ? refetchUnpaid : refetchAll;

  function handlePay(id: string) {
    const invoice = invoices?.find((item) => item.id === id) ?? null;
    setSelectedInvoice(invoice);
  }

  function handleSubmit(body: PaymentBody) {
    if (!selectedInvoice) return;

    payInvoice.mutate(
      { id: selectedInvoice.id, body },
      {
        onSuccess: () => {
          setSelectedInvoice(null);
        },
      }
    );
  }

  if (isLoading) {
    return <Loading message="Loading invoices…" />;
  }

  if (isError || !invoices) {
    return (
      <ErrorState
        title="Invoices unavailable"
        message={errorObj?.message ?? 'Failed to load invoices. Please try again.'}
        onRetry={() => refetch()}
      />
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-2xl font-bold text-neutral-900">Billing</h1>
        <div className="flex gap-2">
          <Button
            variant={view === 'unpaid' ? 'primary' : 'secondary'}
            onClick={() => setView('unpaid')}
            data-testid="unpaid-invoices-tab"
          >
            Unpaid
          </Button>
          <Button
            variant={view === 'all' ? 'primary' : 'secondary'}
            onClick={() => setView('all')}
            data-testid="all-invoices-tab"
          >
            All invoices
          </Button>
        </div>
      </div>

      {invoices.length === 0 ? (
        <p className="text-neutral-500">{view === 'unpaid' ? 'No unpaid invoices.' : 'No invoices found.'}</p>
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {invoices.map((invoice) => (
            <InvoiceCard key={invoice.id} invoice={invoice} onPay={handlePay} />
          ))}
        </div>
      )}

      <PaymentModal
        isOpen={selectedInvoice !== null}
        invoice={selectedInvoice}
        onClose={() => setSelectedInvoice(null)}
        onSubmit={handleSubmit}
        isSubmitting={payInvoice.isPending}
      />
    </div>
  );
}
