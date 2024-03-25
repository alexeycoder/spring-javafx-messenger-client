package edu.alexey.messengerclient.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.alexey.messengerclient.entities.Conversation;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Integer> {

}
