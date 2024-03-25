package edu.alexey.messengerclient.services;

import org.springframework.stereotype.Service;

import edu.alexey.messengerclient.repositories.ConversationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConversationService {

	private final ConversationRepository conversationRepository;

}
