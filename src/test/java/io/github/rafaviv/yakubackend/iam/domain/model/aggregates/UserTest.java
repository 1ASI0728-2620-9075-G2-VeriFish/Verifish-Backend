package io.github.rafaviv.yakubackend.iam.domain.model.aggregates;

import io.github.rafaviv.yakubackend.iam.domain.model.entities.Role;
import io.github.rafaviv.yakubackend.iam.domain.model.valueobjects.Email;
import io.github.rafaviv.yakubackend.iam.domain.model.valueobjects.HashedPassword;
import io.github.rafaviv.yakubackend.iam.domain.model.valueobjects.Roles;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    @DisplayName("Given valid user data, When creating user, Then it is created successfully")
    void createUser_Successfully() {
        Email email = new Email("test@example.com");
        HashedPassword password = new HashedPassword("hashed123");

        User user = new User("testuser", email, password, "John", "Doe", false);

        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmailAddress());
        assertEquals("hashed123", user.getPasswordHash());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertFalse(user.isVerified());
        assertTrue(user.getActive());
        assertNotNull(user.getRoles());
        assertTrue(user.getRoles().isEmpty());
    }

    @Test
    @DisplayName("Given user with first and last name, When getting full name, Then returns concatenated name")
    void getFullName_Successfully() {
        Email email = new Email("test@example.com");
        HashedPassword password = new HashedPassword("hashed123");

        User user = new User("testuser", email, password, "John", "Doe", false);

        assertEquals("John Doe", user.getFullName());
    }

    @Test
    @DisplayName("Given user without roles, When adding role, Then role is added")
    void addRole_Successfully() {
        Email email = new Email("test@example.com");
        HashedPassword password = new HashedPassword("hashed123");
        User user = new User("testuser", email, password, "John", "Doe", false);
        Role role = new Role(Roles.ADMIN);

        user.addRole(role);

        assertEquals(1, user.getRoles().size());
        assertTrue(user.getRoles().contains(role));
    }

    @Test
    @DisplayName("Given user with role, When adding same role again, Then role is not duplicated")
    void addRole_DuplicateRole_NotAdded() {
        Email email = new Email("test@example.com");
        HashedPassword password = new HashedPassword("hashed123");
        User user = new User("testuser", email, password, "John", "Doe", false);
        Role role = new Role(Roles.ADMIN);

        user.addRole(role);
        user.addRole(role);

        assertEquals(1, user.getRoles().size());
    }

    @Test
    @DisplayName("Given user, When adding null role, Then no role is added")
    void addRole_NullRole_NotAdded() {
        Email email = new Email("test@example.com");
        HashedPassword password = new HashedPassword("hashed123");
        User user = new User("testuser", email, password, "John", "Doe", false);

        user.addRole(null);

        assertTrue(user.getRoles().isEmpty());
    }

    @Test
    @DisplayName("Given verified user, When creating user, Then isVerified is true")
    void createVerifiedUser_Successfully() {
        Email email = new Email("test@example.com");
        HashedPassword password = new HashedPassword("hashed123");

        User user = new User("testuser", email, password, "John", "Doe", true);

        assertTrue(user.isVerified());
    }

    @Test
    @DisplayName("Given user with multiple roles, When checking roles, Then all roles are present")
    void addRole_MultipleRoles_AllPresent() {
        Email email = new Email("test@example.com");
        HashedPassword password = new HashedPassword("hashed123");
        User user = new User("testuser", email, password, "John", "Doe", false);
        Role adminRole = new Role(Roles.ADMIN);
        Role operatorRole = new Role(Roles.OPERATOR);

        user.addRole(adminRole);
        user.addRole(operatorRole);

        assertEquals(2, user.getRoles().size());
        assertTrue(user.getRoles().contains(adminRole));
        assertTrue(user.getRoles().contains(operatorRole));
    }
}
