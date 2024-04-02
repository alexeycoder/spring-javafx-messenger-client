package edu.alexey.messengerclient.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class ContactDto {

	private UUID userUuid;

	private String displayName;
}
