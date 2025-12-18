export type ChatRole = 'USER' | 'ASSISTANT' | 'SYSTEM';

export interface ChatMessage {
  id?: number;
  chatRole: ChatRole;
  chatSessionId?: number;
  content: string;
  createdAt?: string;
  clientId?: string;
  isPending?: boolean;
}

export interface ChatSession {
  id: number;
  userId?: number | null;
  spotId: number;
  createdAt: string;
  messages: ChatMessage[];
}
