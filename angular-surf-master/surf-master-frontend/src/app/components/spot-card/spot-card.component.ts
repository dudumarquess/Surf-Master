import { Component, Input } from '@angular/core';

import { Spot } from '../../models/spot.model';
import { Forecast } from '../../models/forecast.model';
import { ChatMessage, ChatSession } from '../../models/chat.model';
import { ForecastService } from '../../services/forecast.service';
import { ChatService } from '../../services/chat.service';

@Component({
  selector: 'app-spot-card',
  templateUrl: './spot-card.component.html',
  styleUrl: './spot-card.component.css'
})
export class SpotCardComponent {
  @Input({ required: true }) spot!: Spot;

  forecastExpanded = false;
  forecasts: Forecast[] = [];
  forecastsLoading = false;
  forecastError?: string;

  chatSession?: ChatSession;
  chatMessages: ChatMessage[] = [];
  chatInput = '';
  chatBusy = false;
  chatError?: string;
  openingChat = false;

  constructor(
    private forecastService: ForecastService,
    private chatService: ChatService
  ) {}

  toggleForecasts(): void {
    this.forecastExpanded = !this.forecastExpanded;
    if (this.forecastExpanded && !this.forecasts.length && !this.forecastsLoading) {
      this.loadForecasts();
    }
  }

  startChat(): void {
    if (this.chatSession || this.openingChat) {
      return;
    }

    this.chatError = undefined;
    this.openingChat = true;
    this.chatService.openSession(this.spot.id).subscribe({
      next: (session) => {
        this.chatSession = session;
        this.chatMessages = session.messages ?? [];
      },
      error: () => {
        this.chatError = 'Unable to open the chat session right now.';
      },
      complete: () => {
        this.openingChat = false;
      }
    });
  }

  sendMessage(): void {
    const trimmed = this.chatInput.trim();
    if (!trimmed || !this.chatSession || this.chatBusy) {
      return;
    }

    this.chatError = undefined;
    const clientId = crypto.randomUUID ? crypto.randomUUID() : `${Date.now()}-${Math.random()}`;
    const optimisticMessage: ChatMessage = {
      clientId,
      chatRole: 'USER',
      chatSessionId: this.chatSession.id,
      content: trimmed,
      createdAt: new Date().toISOString(),
      isPending: true
    };

    this.chatMessages = [...this.chatMessages, optimisticMessage];
    this.chatInput = '';
    this.chatBusy = true;

    this.chatService.sendMessage(this.chatSession.id, trimmed).subscribe({
      next: (assistantMessage) => {
        this.chatMessages = this.chatMessages.map((message) =>
          message.clientId === clientId ? { ...message, isPending: false } : message
        );
        this.chatMessages = [...this.chatMessages, assistantMessage];
      },
      error: () => {
        this.chatMessages = this.chatMessages.filter((message) => message.clientId !== clientId);
        this.chatError = 'We could not reach the Surf Master. Try sending the message again.';
      },
      complete: () => {
        this.chatBusy = false;
      }
    });
  }

  trackMessage(_index: number, message: ChatMessage): string | number | undefined {
    return message.id ?? message.clientId;
  }

  get hasChatHistory(): boolean {
    return this.chatMessages.length > 0;
  }

  formatTimestamp(timestamp?: string): string {
    if (!timestamp) {
      return '';
    }
    return new Date(timestamp).toLocaleString();
  }

  formatCoordinate(value: number, axis: 'lat' | 'lng'): string {
    const cardinal = axis === 'lat' ? (value >= 0 ? 'N' : 'S') : value >= 0 ? 'E' : 'W';
    return `${Math.abs(value).toFixed(2)}° ${cardinal}`;
  }

  private loadForecasts(): void {
    this.forecastsLoading = true;
    this.forecastError = undefined;
    this.forecastService.getForecastsForSpot(this.spot.id).subscribe({
      next: (forecasts) => {
        this.forecasts = [...forecasts].sort((a, b) => {
          return new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime();
        });
      },
      error: () => {
        this.forecastError = 'Could not fetch the forecast for this spot.';
      },
      complete: () => {
        this.forecastsLoading = false;
      }
    });
  }
}
