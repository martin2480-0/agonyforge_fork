package com.agonyforge.mud.demo.model.impl;


import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "banned_users")
public class ReloadedUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private User reloadedUser;

    private String reason;

    public ReloadedUser() {
        // this method intentionally left blank
    }

    public ReloadedUser(User reloadedUser, String reason) {
        this.reloadedUser = reloadedUser;
        this.reason = reason;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public User getReloadedUser() {
        return reloadedUser;
    }

    public void setReloadedUser(User reloadedUser) {
        this.reloadedUser = reloadedUser;
    }
}
