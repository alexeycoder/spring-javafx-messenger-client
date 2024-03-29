package edu.alexey.messengerclient.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
public class Message implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "message_id")
	private Integer messageId;

	@Column(name = "message_uuid", nullable = false, updatable = false)
	private UUID messageUuid;

	@Column(name = "sent_at", nullable = false, updatable = false)
	private LocalDateTime sentAt;

	@Column(name = "content", nullable = false, updatable = false)
	private String content;

	@ManyToOne
	@JoinColumn(name = "conversation_id", referencedColumnName = "conversation_id")
	private Conversation conversation;

}
