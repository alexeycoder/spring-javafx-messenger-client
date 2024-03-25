package edu.alexey.messengerclient.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.alexey.messengerclient.entities.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

}
