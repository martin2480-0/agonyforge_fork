package com.agonyforge.mud.demo.model.impl;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class CommandForce {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    @JoinColumn(name = "command_name", referencedColumnName = "name")
    private CommandReference command;

    @Column(nullable = false)
    private Boolean forcible;

    public CommandForce() {
        // this method intentionally left blank
    }

    public CommandForce(CommandReference command, Boolean forcible) {
        this.command = command;
        this.forcible = forcible;
    }

    public CommandReference getName() {
        return command;
    }

    public void setName(CommandReference command) {
        this.command = command;
    }

    public Boolean isForcible() {
        return forcible;
    }

    public void setForcible(Boolean forcible) {
        this.forcible = forcible;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CommandForce that)) return false;
        return Objects.equals(getId(), that.getId()) && Objects.equals(command, that.command) && Objects.equals(forcible, that.forcible);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), command, forcible);
    }
}
