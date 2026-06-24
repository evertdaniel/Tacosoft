import { useEffect, useRef } from 'react';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useQueryClient } from '@tanstack/react-query';
import { useAuthStore } from '@/stores/auth.store';
import { useTenantStore } from '@/stores/tenant.store';

const WS_URL = `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/ws`;

export function useWebSocket() {
  const token = useAuthStore((state) => state.token);
  const restaurantId = useTenantStore((state) => state.currentRestaurantId);
  const queryClient = useQueryClient();
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (!token || !restaurantId) {
      return;
    }

    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      onConnect: () => {
        client.subscribe(`/topic/restaurant/${restaurantId}/tables`, (_message: IMessage) => {
          queryClient.invalidateQueries({ queryKey: ['tables'] });
        });
      },
      debug: () => {},
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
    };
  }, [token, restaurantId, queryClient]);
}
