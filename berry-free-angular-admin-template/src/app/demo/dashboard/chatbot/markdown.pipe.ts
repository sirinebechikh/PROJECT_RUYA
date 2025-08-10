import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'markdown',
  standalone: true
})
export class MarkdownPipe implements PipeTransform {

  transform(value: string): string {
    if (!value) return '';

    // Simple markdown-like transformations
    let result = value
      // Bold text **text** or __text__
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/__(.*?)__/g, '<strong>$1</strong>')
      
      // Italic text *text* or _text_
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
      .replace(/_(.*?)_/g, '<em>$1</em>')
      
      // Code blocks `code`
      .replace(/`(.*?)`/g, '<code>$1</code>')
      
      // Line breaks
      .replace(/\n/g, '<br>')
      
      // Emojis and special characters (keep as is)
      .replace(/📊|📁|💰|✅|❌|🎉|👥|👤|📅|💡|🤖|💵|📈|📉|⚠️|🔍|📋|🚀/g, '<span class="emoji">$&</span>');

    return result;
  }
}
