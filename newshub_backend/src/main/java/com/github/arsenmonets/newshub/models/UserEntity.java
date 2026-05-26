package com.github.arsenmonets.newshub.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(nullable = false)
    private boolean isBlocked;

    @ManyToMany
    @JoinTable(name = "user_categories", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    private List<CategoryEntity> subscribedCategories = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "user_author_subscriptions", joinColumns = @JoinColumn(name = "subscriber_id"), inverseJoinColumns = @JoinColumn(name = "author_id"))
    private List<UserEntity> subscribedAuthors = new ArrayList<>();

    public UserEntity() {
    }

    public UserEntity(String login, String email, String password, UserRole role) {
        this.login = login;
        this.email = email;
        this.password = password;
        this.role = role;
        this.subscribedCategories = new ArrayList<>();
        this.subscribedAuthors = new ArrayList<>();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public List<CategoryEntity> getSubscribedCategories() {
        return subscribedCategories;
    }

    public void setSubscribedCategories(List<CategoryEntity> subscribedCategories) {
        this.subscribedCategories = subscribedCategories;
    }

    public List<UserEntity> getSubscribedAuthors() {
        return subscribedAuthors;
    }

    public void setSubscribedAuthors(List<UserEntity> subscribedAuthors) {
        this.subscribedAuthors = subscribedAuthors;
    }
}
