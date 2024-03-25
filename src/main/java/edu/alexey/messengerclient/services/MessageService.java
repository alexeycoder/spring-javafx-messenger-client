package edu.alexey.messengerclient.services;

import org.springframework.stereotype.Service;

import edu.alexey.messengerclient.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {

	private final MessageRepository messageRepository;

}
