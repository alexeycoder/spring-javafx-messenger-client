package edu.alexey.messengerclient.entities;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
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

	public Contact(UUID userUuid, String displayName) {
		this.userUuid = userUuid;
		this.displayNameProperty = new SimpleStringProperty(displayName);
	}

	@Id
	@EqualsAndHashCode.Include
	@ToString.Include
	@Getter
	@Setter
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "contact_id")
	private Integer contactId;

	@EqualsAndHashCode.Include
	@Getter
	@Setter
	@Column(name = "user_uuid", nullable = false, unique = true)
	private UUID userUuid;

	@Transient
	private final StringProperty displayNameProperty;

	@Access(AccessType.PROPERTY)
	@EqualsAndHashCode.Include
	@ToString.Include
	@Column(name = "display_name", nullable = false)
	public String getDisplayName() {
		return displayNameProperty.get();
	}

	public void setDisplayName(String username) {
		this.displayNameProperty.set(username);
	}

	public StringProperty displayNameProperty() {
		return displayNameProperty;
	}

	@OneToOne(mappedBy = "contact", fetch = FetchType.LAZY) // mappedBy -- с помощью какого поля идёт обратная связь (поле в Conversation)
	@Getter
	@Setter
	private Conversation conversation;

}
