import { RouterProvider } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useTokenExpiry } from '@/hooks/useTokenExpiry';
import { router as defaultRouter } from './router';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: false,
      refetchOnWindowFocus: false,
    },
    mutations: {
      retry: false,
    },
  },
});

interface AppProps {
  router?: typeof defaultRouter;
}

function App({ router = defaultRouter }: AppProps) {
  useTokenExpiry();

  return (
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  );
}

export default App;
