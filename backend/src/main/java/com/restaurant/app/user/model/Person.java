package com.restaurant.app.user.model;

import jakarta.persistence.*;
import java.util.UUID;

/** Person entity - shared personal data. */
@Entity
@Table(name = "person")
public class Person {

    @Id
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private String id = UUID.randomUUID().toString();

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(name = "email", length = 120)
    private String email;

    @Column(name = "phone", length = 40)
    private String phone;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "document_id", length = 40)
    private String documentId;

    // Default constructor
    public Person() {}

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public static class Builder {
        private final Person person = new Person();

        public Builder id(String id) {
            person.id = id;
            return this;
        }

        public Builder firstName(String firstName) {
            person.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            person.lastName = lastName;
            return this;
        }

        public Builder email(String email) {
            person.email = email;
            return this;
        }

        public Builder phone(String phone) {
            person.phone = phone;
            return this;
        }

        public Builder address(String address) {
            person.address = address;
            return this;
        }

        public Builder documentId(String documentId) {
            person.documentId = documentId;
            return this;
        }

        public Person build() {
            return person;
        }
    }
}
