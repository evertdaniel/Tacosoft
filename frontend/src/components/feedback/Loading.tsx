interface LoadingProps {
  message?: string;
}

export function Loading({ message = 'Loading…' }: LoadingProps) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 p-8 text-neutral-600" role="status" aria-live="polite">
      <div className="h-8 w-8 animate-spin rounded-full border-4 border-neutral-200 border-t-blue-600" />
      <span className="text-sm">{message}</span>
    </div>
  );
}
