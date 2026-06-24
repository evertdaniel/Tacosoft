interface PlaceholderProps {
  label: string;
}

export function Placeholder({ label }: PlaceholderProps) {
  return (
    <div data-testid={`${label.toLowerCase()}-placeholder`} className="p-6">
      <h1 className="text-2xl font-bold text-neutral-800">{label} page</h1>
      <p className="mt-2 text-neutral-600">This page is under construction.</p>
    </div>
  );
}
