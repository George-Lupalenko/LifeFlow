import { Controller, Post, Body, HttpException, HttpStatus } from '@nestjs/common';
import { EmailService } from './email.service';

class GenerateEmailDto {
  prompt: string;
}

@Controller('email')
export class EmailController {
  constructor(private readonly emailService: EmailService) {}

  @Post('generate')
  async generateEmail(@Body() dto: GenerateEmailDto) {
    try {
      if (!dto.prompt) {
        throw new HttpException('Prompt is required', HttpStatus.BAD_REQUEST);
      }

      const result = await this.emailService.processPrompt(dto.prompt);
      return result;
    } catch (error) {
      throw new HttpException(
        error.message || 'Failed to process request',
        HttpStatus.INTERNAL_SERVER_ERROR,
      );
    }
  }
}
