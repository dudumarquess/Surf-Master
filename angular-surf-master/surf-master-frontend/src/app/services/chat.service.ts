import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { ChatMessage, ChatRole, ChatSession } from '../models/chat.model';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private readonly baseUrl = `${environment.apiBaseUrl}/chat`;

  constructor(private http: HttpClient) {}

  openSession(spotId: number, userId?: number): Observable<ChatSession> {
    let params = new HttpParams();
    if (userId != null) {
      params = params.set('userId', userId.toString());
    }
    return this.http.post<ChatSession>(`${this.baseUrl}/sessions/${spotId}`, null, { params });
  }

  getSession(sessionId: number): Observable<ChatSession> {
    return this.http.get<ChatSession>(`${this.baseUrl}/sessions/${sessionId}`);
  }

  sendMessage(sessionId: number, content: string, chatRole: ChatRole = 'USER'): Observable<ChatMessage> {
    return this.http.post<ChatMessage>(`${this.baseUrl}/messages`, {
      chatSessionId: sessionId,
      chatRole,
      content
    });
  }

  resetSession(sessionId: number, spotId: number, userId?: number): Observable<ChatSession> {
    return this.http.post<ChatSession>(`${this.baseUrl}/sessions/${sessionId}/reset`, {
      spotId,
      userId
    });
  }
}
