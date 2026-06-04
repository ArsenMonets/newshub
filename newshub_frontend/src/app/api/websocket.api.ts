import { Injectable, signal } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import { NewsPreviewDTO } from '../models/models';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WebSocketAPI {
  private stompClient: Client | null = null;
  
  newsCreated$ = new Subject<NewsPreviewDTO>();
  newsUpdated$ = new Subject<NewsPreviewDTO>();
  newsDeleted$ = new Subject<number>();

  constructor() {
    this.connect();
  }

  private connect() {
    const protocol = globalThis.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${globalThis.location.host}/ws`;
    
    this.stompClient = new Client({
      brokerURL: wsUrl,
      debug: (msg) => console.log(msg),
      onConnect: () => {
        this.stompClient?.subscribe('/topic/news/created', (message: IMessage) => {
          this.newsCreated$.next(JSON.parse(message.body));
        });
        this.stompClient?.subscribe('/topic/news/updated', (message: IMessage) => {
          this.newsUpdated$.next(JSON.parse(message.body));
        });
        this.stompClient?.subscribe('/topic/news/deleted', (message: IMessage) => {
          this.newsDeleted$.next(JSON.parse(message.body));
        });
      }
    });
    this.stompClient.activate();
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.deactivate();
    }
  }
}

