package edu.alexey.messengerclient.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.alexey.messengerclient.entities.Contact;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Integer> {

}
