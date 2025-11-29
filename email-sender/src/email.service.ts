import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { GoogleGenerativeAI } from '@google/generative-ai';

@Injectable()
export class EmailService {
  private genAI: GoogleGenerativeAI;
  private model;

  constructor(private configService: ConfigService) {
    // Initialize Gemini API - you'll need to set GEMINI_API_KEY in your environment
    const apiKey = this.configService.get<string>('GEMINI_API_KEY') || '';
    this.genAI = new GoogleGenerativeAI(apiKey);
    this.model = this.genAI.getGenerativeModel({ model: 'gemini-2.5-flash' });
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
   * Generate cold email body using Gemini API
   */
  async generateColdEmail(prompt: string): Promise<string> {
    try {
      const result = await this.model.generateContent(
        `Generate a professional cold email based on this request: "${prompt}".
        Only return the email body text, without subject line, greetings can be included.
        Make it concise and professional.`,
      );
      const response = result.response;
      return response.text();
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
