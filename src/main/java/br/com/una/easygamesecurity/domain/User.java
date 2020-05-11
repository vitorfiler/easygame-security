package br.com.una.easygamesecurity.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "USER")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_USER")
    @SequenceGenerator(name = "SQ_USER")
    @Column(name = "ID")
    private Long id;

    @NotNull
    @Pattern(regexp = "^[_.@A-Za-z0-9-]*$")
    @Size(min = 1, max = 50)
    @Column(name = "LOGIN", length = 50, unique = true, nullable = false)
    private String login;

    @JsonIgnore
    @NotNull
    @Size(min = 60, max = 60)
    @Column(name = "PASSWORD_HASH", length = 60, nullable = false)
    private String password;

    @Size(max = 50)
    @Column(name = "FIRST_NAME", length = 50)
    private String firstName;

    @Size(max = 50)
    @Column(name = "LAST_NAME", length = 50)
    private String lastName;

    @Email
    @Size(min = 5, max = 254)
    @Column(name = "EMAIL", length = 254, unique = true)
    private String email;

    @NotNull
    @Column(name = "ACTIVATED", nullable = false)
    private boolean activated = false;

    @Size(min = 2, max = 10)
    @Column(name = "LANG_KEY", length = 10)
    private String langKey;

    @Size(max = 256)
    @Column(name = "IMAGE_URL", length = 256)
    private String imageUrl;

    @Size(max = 20)
    @Column(name = "ACTIVATION_KEY", length = 20)
    @JsonIgnore
    private String activationKey;

    @Size(max = 20)
    @Column(name = "RESET_KEY", length = 20)
    @JsonIgnore
    private String resetKey;

    @Column(name = "RESET_DATE")
    private Instant resetDate = null;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "USER_AUTHORITY",
            joinColumns = {@JoinColumn(name = "USER_ID", referencedColumnName = "ID")},
            inverseJoinColumns = {@JoinColumn(name = "AUTHORITY_NAME", referencedColumnName = "NAME")}
    )
    private Set<Authority> authorities = new HashSet<>();

    public void setLogin(String login) {
        this.login = StringUtils.lowerCase(login, Locale.ENGLISH);
    }

}
