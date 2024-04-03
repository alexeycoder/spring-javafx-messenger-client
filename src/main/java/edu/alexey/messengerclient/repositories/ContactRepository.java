package edu.alexey.messengerclient.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.alexey.messengerclient.entities.Contact;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Integer> {

	Contact findByUserUuid(UUID userUuid);

	boolean existsByUserUuid(UUID userUuid);
}
