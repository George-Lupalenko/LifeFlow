import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { generateText } from 'ai';
import { openai } from '@ai-sdk/openai';

@Injectable()
export class EmailService {
  private apiKey: string;

  constructor(private configService: ConfigService) {
    // Initialize OpenAI API - you'll need to set OPENAI_API_KEY in your environment
    this.apiKey = this.configService.get<string>('OPENAI_API_KEY') || '';
  }

  /**
   * Extract email address from the prompt
   */
  extractEmail(prompt: string): string | null {
    const emailRegex = /\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b/;
    const match = prompt.match(emailRegex);
    return match ? match[0] : null;
  }

  /**
   * Generate cold email body using OpenAI via Vercel AI SDK
   */
  async generateColdEmail(prompt: string): Promise<string> {
    try {
      const { text } = await generateText({
        model: openai('gpt-4o-mini'),
        prompt: `Generate a professional cold email based on this request: "${prompt}".
        Only return the email body text, without subject line, greetings can be included.
        Make it concise and professional.`,
      });
      return text;
    } catch (error) {
      throw new Error(`Failed to generate email: ${error.message}`);
    }
  }

  /**
   * Process the user prompt and return email data
   */
  async processPrompt(prompt: string): Promise<{ to: string; body: string }> {
    const email = this.extractEmail(prompt);

    if (!email) {
      throw new Error('No email address found in the prompt');
    }

    const body = await this.generateColdEmail(prompt);

    return {
      to: email,
      body: body.trim(),
    };
  }
}
