import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MarkdownPipe } from './markdown.pipe';

interface ChatMessage {
  text: string;
  isUser: boolean;
  timestamp: Date;
  type?: string;
}

interface ChatResponse {
  response: string;
  type: string;
  data?: any;
}

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule, MarkdownPipe],
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.scss']
})
export class ChatbotComponent implements OnInit {
  messages: ChatMessage[] = [];
  currentMessage: string = '';
  isLoading: boolean = false;
  isExpanded: boolean = false;
  suggestions: string[] = [];

  private apiUrl = 'http://localhost:8081/api/chatbot';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.initializeChat();
    this.loadSuggestions();
  }

  initializeChat() {
    this.messages.push({
      text: '🤖 Bonjour! Je suis votre assistant RU\'ya. Comment puis-je vous aider avec vos données financières?',
      isUser: false,
      timestamp: new Date(),
      type: 'info'
    });
  }

  loadSuggestions() {
    this.http.get<string[]>(`${this.apiUrl}/suggestions`).subscribe({
      next: (suggestions) => {
        this.suggestions = suggestions;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des suggestions:', error);
      }
    });
  }

  sendMessage() {
    if (!this.currentMessage.trim() || this.isLoading) return;

    // Ajouter le message de l'utilisateur
    this.messages.push({
      text: this.currentMessage,
      isUser: true,
      timestamp: new Date()
    });

    const question = this.currentMessage;
    this.currentMessage = '';
    this.isLoading = true;

    // Envoyer la question au backend
    this.http.post<ChatResponse>(`${this.apiUrl}/ask`, { question }).subscribe({
      next: (response) => {
        this.messages.push({
          text: response.response,
          isUser: false,
          timestamp: new Date(),
          type: response.type
        });
        this.isLoading = false;
        this.scrollToBottom();
      },
      error: (error) => {
        console.error('Erreur chatbot:', error);
        this.messages.push({
          text: 'Désolé, une erreur s\'est produite. Veuillez réessayer.',
          isUser: false,
          timestamp: new Date(),
          type: 'error'
        });
        this.isLoading = false;
        this.scrollToBottom();
      }
    });
  }

  useSuggestion(suggestion: string) {
    this.currentMessage = suggestion;
    this.sendMessage();
  }

  toggleChat() {
    this.isExpanded = !this.isExpanded;
  }

  clearChat() {
    this.messages = [];
    this.initializeChat();
  }

  private scrollToBottom() {
    setTimeout(() => {
      const chatContainer = document.querySelector('.chat-messages');
      if (chatContainer) {
        chatContainer.scrollTop = chatContainer.scrollHeight;
      }
    }, 100);
  }

  onKeyPress(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }
}
