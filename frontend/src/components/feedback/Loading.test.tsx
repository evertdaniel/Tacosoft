import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Loading } from './Loading';

describe('Loading', () => {
  it('renders the default loading message', () => {
    render(<Loading />);
    expect(screen.getByText('Loading…')).toBeInTheDocument();
  });

  it('renders a custom message when provided', () => {
    render(<Loading message="Fetching dashboard…" />);
    expect(screen.getByText('Fetching dashboard…')).toBeInTheDocument();
  });
});
