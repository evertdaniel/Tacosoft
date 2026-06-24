import { Button } from '@/components/ui/Button';

interface ErrorStateProps {
  title: string;
  message: string;
  onRetry?: () => void;
}

export function ErrorState({ title, message, onRetry }: ErrorStateProps) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 rounded-lg border border-red-100 bg-red-50 p-8 text-center" role="alert">
      <h3 className="text-lg font-semibold text-red-800">{title}</h3>
      <p className="text-sm text-red-700">{message}</p>
      {onRetry && (
        <Button variant="danger" onClick={onRetry}>
          Retry
        </Button>
      )}
    </div>
  );
}
