package edu.alexey.messengerclient.services;

import org.springframework.stereotype.Service;

import edu.alexey.messengerclient.repositories.ContactRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContactService {

	private final ContactRepository contactRepository;

}
