import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Avatar } from './Avatar';

describe('Avatar', () => {
  it('renders initials fallback from name', () => {
    render(<Avatar name="Ada Lovelace" />);
    expect(screen.getByText('AL')).toBeInTheDocument();
  });

  it('renders a single initial when name has one word', () => {
    render(<Avatar name="Grace" />);
    expect(screen.getByText('G')).toBeInTheDocument();
  });

  it('renders an image when src is provided', () => {
    render(<Avatar name="Alan Turing" src="https://example.com/avatar.png" />);
    expect(screen.getByRole('img', { name: 'Alan Turing' })).toHaveAttribute('src', 'https://example.com/avatar.png');
  });
});
