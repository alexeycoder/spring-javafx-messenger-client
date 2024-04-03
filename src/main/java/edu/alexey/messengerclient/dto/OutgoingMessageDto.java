package edu.alexey.messengerclient.dto;

import java.util.UUID;

public record OutgoingMessageDto(UUID addresseeUuid, String content) {
}
