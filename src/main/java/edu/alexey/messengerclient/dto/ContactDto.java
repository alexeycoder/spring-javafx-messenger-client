package edu.alexey.messengerclient.dto;

import java.util.UUID;

public record ContactDto(UUID userUuid, String displayName) {
}

//@Data
//public class ContactDto {
//	UUID userUuid;
//	String displayName;
//}