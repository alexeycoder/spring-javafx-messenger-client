package edu.alexey.messengerclient.repositories;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.alexey.messengerclient.entities.Conversation;
import edu.alexey.messengerclient.entities.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

	Message getTopBy(Sort sort);

	List<Message> findAllByConversation(Conversation conversation);
}
