package com.agonyforge.mud.demo.model.impl;


import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "banned_users")
public class BannedUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private User bannedUser;

    private boolean permanent;

    private String reason;

    @Temporal(TemporalType.TIMESTAMP)
    private Date bannedToDate;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date bannedOn;

    public BannedUser() {
        // this method intentionally left blank
    }

    public BannedUser(User bannedUser, boolean permanent, Date bannedToDate, String reason) {
        this.bannedUser = bannedUser;
        this.permanent = permanent;
        this.bannedToDate = bannedToDate;
        this.reason = reason;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getBannedUser() {
        return bannedUser;
    }

    public void setBannedUser(User bannedUser) {
        this.bannedUser = bannedUser;
    }

    public Date getBannedToDate() {
        return bannedToDate;
    }

    public String getReason() {
        return reason;
    }

    public void setBannedToDate(Date bannedToDate) {
        this.bannedToDate = bannedToDate;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setPermanent(boolean temporary) {
        this.permanent = temporary;
    }

    public boolean isPermanent() {
        return permanent;
    }

    public Date getBannedOn() {
        return bannedOn;
    }

    public void setBannedOn(Date bannedOn) {
        this.bannedOn = bannedOn;
    }
}
