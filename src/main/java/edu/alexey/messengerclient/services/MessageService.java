package edu.alexey.messengerclient.services;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.alexey.messengerclient.entities.Conversation;
import edu.alexey.messengerclient.entities.Message;
import edu.alexey.messengerclient.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {

	private final MessageRepository messageRepository;

	public List<Message> findAll() {
		return messageRepository.findAll();
	}

	public List<Message> findAllByConversation(Conversation conversation) {
		return messageRepository.findAllByConversation(conversation);
	}
}
