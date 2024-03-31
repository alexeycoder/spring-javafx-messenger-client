package edu.alexey.messengerclient.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record IncomingMessageDto(UUID messageUuid, UUID senderUuid, UUID addresseeUuid, LocalDateTime sentAt,
		String content) {

	public IncomingMessageDto {
		if (messageUuid == null) {
			throw new IllegalArgumentException("messageUuid cannot be null");
		}
		if (senderUuid == null && addresseeUuid == null) {
			throw new IllegalArgumentException("Both senderUuid and addresseeUuid cannot be null simultaneously");
		}
		if (sentAt == null) {
			throw new IllegalArgumentException("sentAt cannot be null");
		}
		if (content == null || content.isBlank()) {
			throw new IllegalArgumentException("content cannot be null or empty");
		}
	}
}
