package edu.alexey.messengerclient.entities;

import java.io.Serializable;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "persons")
@DynamicUpdate
@DynamicInsert
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long personId;

	private final StringProperty fullNameProperty;
	private final StringProperty phoneProperty;

	public User() {
		this("", "");
	}

	public User(String fullName, String phone) {
		this.fullNameProperty = new SimpleStringProperty(fullName);
		this.phoneProperty = new SimpleStringProperty(phone);
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "person_id")
	@Basic
	@EqualsAndHashCode.Include
	@ToString.Include
	public Long getPersonId() {
		return personId;
	}

	public void setPersonId(Long personId) {
		this.personId = personId;
	}

	@Column(name = "full_name")
	@Basic
	@EqualsAndHashCode.Include
	@ToString.Include
	public String getFullName() {
		return fullNameProperty.get();
	}

	public void setFullName(String fullName) {
		this.fullNameProperty.set(fullName);
	}

	@Column(name = "phone")
	@Basic
	@EqualsAndHashCode.Include
	@ToString.Include
	public String getPhone() {
		return phoneProperty.get();
	}

	public void setPhone(String phone) {
		this.phoneProperty.set(phone);
	}

	public StringProperty fullNameProperty() {
		return fullNameProperty;
	}

	public StringProperty phoneProperty() {
		return phoneProperty;
	}

	@Column(name = "photo")
	@Basic
	@Getter
	@Setter
	private byte[] photo;

	@Column(name = "address")
	@Basic
	@Getter
	@Setter
	private String address;

	//	@Override
	//	public String toString() {
	//		return String.format("Person [fullName=%s, phone=%s]", getFullName(), getPhone());
	//	}

}
