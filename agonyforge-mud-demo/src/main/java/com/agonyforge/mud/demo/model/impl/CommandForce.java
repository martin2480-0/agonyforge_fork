package com.agonyforge.mud.demo.model.impl;

import jakarta.persistence.*;

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
}
