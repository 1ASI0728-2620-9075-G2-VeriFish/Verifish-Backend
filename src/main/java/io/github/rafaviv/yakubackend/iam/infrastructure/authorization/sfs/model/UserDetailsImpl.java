package io.github.rafaviv.yakubackend.iam.infrastructure.authorization.sfs.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.rafaviv.yakubackend.iam.domain.model.aggregates.User;
import io.github.rafaviv.yakubackend.shared.security.FarmAwarePrincipal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * This class is responsible for providing the user details to the Spring Security framework.
 * It implements the UserDetails interface with multitenancy support.
 */
@Getter
@EqualsAndHashCode
public class UserDetailsImpl implements UserDetails, FarmAwarePrincipal {

    private final Long id;
    @JsonIgnore
    private final String password;
    private final String username;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final Long farmId;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * This constructor initializes the UserDetailsImpl object without a farmId.
     * @param id The user ID.
     * @param password The password.
     * @param username The username.
     * @param email The email.
     * @param firstName The first name.
     * @param lastName The last name.
     * @param enabled Whether the user is enabled.
     * @param authorities The authorities.
     */
    public UserDetailsImpl(Long id, String password, String username, String email, 
                          String firstName, String lastName,
                          boolean enabled, Collection<? extends GrantedAuthority> authorities) {
        this(id, password, username, email, firstName, lastName, null, enabled, authorities);
    }

    /**
     * This constructor initializes the UserDetailsImpl object including the farmId.
     * @param id The user ID.
     * @param password The password.
     * @param username The username.
     * @param email The email.
     * @param firstName The first name.
     * @param lastName The last name.
     * @param farmId The farm ID associated with the user/token.
     * @param enabled Whether the user is enabled.
     * @param authorities The authorities.
     */
    public UserDetailsImpl(Long id, String password, String username, String email, 
                          String firstName, String lastName, Long farmId,
                          boolean enabled, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.password = password;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.farmId = farmId;
        this.authorities = authorities;
        this.accountNonExpired = true;
        this.accountNonLocked = true;
        this.credentialsNonExpired = true;
        this.enabled = enabled;
    }

    /**
     * This method is responsible for building the UserDetailsImpl object from the User object.
     * @param user The user object.
     * @return The UserDetailsImpl object.
     */
    public static UserDetailsImpl build(User user) {
        var authorities = user.getRoles()
                .stream()
                .map(role -> "ROLE_" + role.getName().name())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new UserDetailsImpl(
                user.getId(),
                user.getPasswordHash(),
                user.getUsername(),
                user.getEmailAddress(),
                user.getFirstName(),
                user.getLastName(),
                user.getFarmId(),
                user.getActive(),
                authorities);
    }

    /**
     * Returns a copy of this UserDetailsImpl with the specified farmId from the JWT claim.
     * @param tokenFarmId the farm ID extracted from the JWT token
     * @return a new UserDetailsImpl with the given farmId
     */
    public UserDetailsImpl withFarmId(Long tokenFarmId) {
        return new UserDetailsImpl(
                this.id,
                this.password,
                this.username,
                this.email,
                this.firstName,
                this.lastName,
                tokenFarmId,
                this.enabled,
                this.authorities);
    }

    /**
     * Get the full name of the user
     * @return The full name
     */
    public String getFullName() {
        return String.format("%s %s", firstName, lastName).trim();
    }

    @Override
    public Long getFarmId() {
        return farmId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
