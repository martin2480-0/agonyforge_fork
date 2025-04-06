package com.agonyforge.mud.demo.model.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Pronoun {
    HE("he", "him", "his", "himself", false),
    SHE("she", "her", "her", "herself", false),
    THEY("they", "them", "their", "themselves", true),
    IT("it", "it", "its", "itself", false),
    XE("xe", "xem", "xyrs", "xemself", false),
    ZE("ze", "zir", "zirs", "zirself", false),
    AE("ae", "aer", "aers", "aerself", false),
    EY("ey", "em", "eirs", "emself", false),
    FAE("fae", "faer", "faer", "faerself", false);


    private final String subject;
    private final String object;
    private final String possessive;
    private final String reflexive;
    private final boolean plural;

    Pronoun(String subject, String object, String possessive, String reflexive, boolean plural) {
        this.subject = subject;
        this.object = object;
        this.possessive = possessive;
        this.reflexive = reflexive;
        this.plural = plural;
    }

    @JsonCreator
    public static Pronoun fromValue(String value) {
        for (Pronoun pronoun : values()) {
            if (pronoun.format().equalsIgnoreCase(value)) {
                return pronoun;
            }
        }
        throw new IllegalArgumentException("Unknown pronoun: " + value);
    }

    @JsonValue
    public String format() {
        return String.format("%s/%s", subject, object);
    }

    public String getSubject() {
        return subject;
    }

    public String getObject() {
        return object;
    }

    public String getPossessive() {
        return possessive;
    }

    public String getReflexive() {
        return reflexive;
    }

    public boolean isPlural() {
        return plural;
    }


    @Override
    public String toString() {
        return String.format("%s/%s", subject, object);
    }
}
