package com.agonyforge.mud.demo.model.impl;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "mud_command")
public class CommandReference extends Persistent {
    @Id
    private String name;
    private Integer priority;
    private String beanName;
    private String description;
    private boolean canBeForced;
    private boolean canExecuteWhileFrozen;

    public CommandReference() {
        // this method intentionally left blank
    }

    public CommandReference(int priority, String name, String beanName, String description, boolean canBeForced, boolean canExecuteWhileFrozen) {
        this.name = name;
        this.priority = priority;
        this.beanName = beanName;
        this.description = description;
        this.canBeForced = canBeForced;
        this.canExecuteWhileFrozen = canExecuteWhileFrozen;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommandReference)) return false;
        CommandReference that = (CommandReference) o;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    public boolean isCanBeForced() {
        return canBeForced;
    }

    public void setCanBeForced(boolean canBeForced) {
        this.canBeForced = canBeForced;
    }

    public boolean isCanExecuteWhileFrozen() {
        return canExecuteWhileFrozen;
    }

    public void setCanExecuteWhileFrozen(boolean canExecuteWhileFrozen) {
        this.canExecuteWhileFrozen = canExecuteWhileFrozen;
    }
}
