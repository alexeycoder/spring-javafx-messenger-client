package edu.alexey.messengerclient.services;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.alexey.messengerclient.entities.Conversation;
import edu.alexey.messengerclient.repositories.ConversationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConversationService {

	private final ConversationRepository conversationRepository;

	public List<Conversation> findAll() {
		return conversationRepository.findAll();
	}

	//	@Transactional(propagation = Propagation.REQUIRED)
	//	public List<Message> getMessages(int conversationId) {
	//		Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
	//		if (conversationOpt.isEmpty()) {
	//			return List.of();
	//		}
	//
	//		List<Message> messages = conversationOpt.get().getMessages();
	//		messages.forEach(m -> System.out.println(m.getMessageUuid()));
	//		return messages;
	//	}

}
