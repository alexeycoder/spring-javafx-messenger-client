package edu.alexey.messengerclient.entities;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "contacts")
//@DynamicUpdate
//@DynamicInsert
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Contact implements Serializable {

	private static final long serialVersionUID = 1L;

	public Contact() {
		this(null, null);
	}

	public Contact(UUID userUuid, String username) {
		this.userUuid = userUuid;
		this.usernameProperty = new SimpleStringProperty(username);
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "contact_id")
	@EqualsAndHashCode.Include
	@ToString.Include
	@Getter
	@Setter
	private Integer contactId;

	@Column(name = "user_uuid", nullable = false, unique = true)
	@EqualsAndHashCode.Include
	@Getter
	@Setter
	private UUID userUuid;

	@Transient
	private final StringProperty usernameProperty;

	@Column(name = "username", nullable = false)
	@Basic
	@EqualsAndHashCode.Include
	@ToString.Include
	public String getUsername() {
		return usernameProperty.get();
	}

	public void setUsername(String username) {
		this.usernameProperty.set(username);
	}

	public StringProperty usernameProperty() {
		return usernameProperty;
	}

	@OneToOne(mappedBy = "contact", fetch = FetchType.LAZY) // mappedBy -- с помощью какого поля идёт обратная связь (поле в Conversation)
	@Getter
	@Setter
	private Conversation conversation;

}
