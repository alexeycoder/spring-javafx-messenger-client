package edu.alexey.messengerclient.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "conversations")
@Getter
@Setter
@EqualsAndHashCode(exclude = { "messages" })
@ToString(exclude = { "messages" })
public class Conversation implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "conversation_id")
	private Integer conversationId;

	@OneToOne
	@JoinColumn(name = "contact_id", referencedColumnName = "contact_id")
	private Contact contact;

	@OneToMany(mappedBy = "conversation", fetch = FetchType.LAZY) // mappedBy -- с помощью какого поля идёт обратная связь (поле в Message)
	private List<Message> messages = new ArrayList<Message>();
}
