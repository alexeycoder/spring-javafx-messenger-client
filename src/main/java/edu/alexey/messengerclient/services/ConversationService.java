package edu.alexey.messengerclient.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import edu.alexey.messengerclient.dto.ContactDto;
import edu.alexey.messengerclient.entities.Contact;
import edu.alexey.messengerclient.entities.Conversation;
import edu.alexey.messengerclient.repositories.ContactRepository;
import edu.alexey.messengerclient.repositories.ConversationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConversationService {

	private final ConversationRepository conversationRepository;
	private final ContactRepository contactRepository;

	public List<Conversation> findAll() {
		return conversationRepository.findAll();
	}

	public Optional<Conversation> createConversationIfNotExistsAndSave(ContactDto contactDto) {

		Contact contact = contactRepository.findByUserUuid(contactDto.userUuid());
		if (contact != null) {
			if (conversationRepository.existsByContact(contact)) {
				return Optional.empty();
			}
		} else {
			contact = new Contact(contactDto.userUuid(), contactDto.displayName());
			contactRepository.save(contact);
		}

		Conversation conversation = new Conversation();
		conversation.setContact(contact);
		return Optional.of(conversationRepository.saveAndFlush(conversation));
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
