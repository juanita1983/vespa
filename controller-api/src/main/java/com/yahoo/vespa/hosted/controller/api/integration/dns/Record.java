// Copyright 2017 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.hosted.controller.api.integration.dns;

import java.util.Objects;

/**
 * A basic representation of a DNS resource record, containing the record id, type, name and value.
 *
 * @author mpolden
 */
public class Record {

    private final RecordId id;
    private final Type type;
    private final String name;
    private final String value;

    public Record(RecordId id, Type type, String name, String value) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.value = value;
    }

    /** Unique identifier for this */
    public RecordId id() {
        return id;
    }

    /** DNS type of this */
    public Type type() {
        return type;
    }

    /** Value for this, e.g. IP address for "A" record */
    public String value() {
        return value;
    }

    /** Name of this, e.g. a FQDN for "A" record */
    public String name() {
        return name;
    }

    public enum Type {
        A,
        AAAA,
        CNAME,
        MX,
        NS,
        PTR,
        SOA,
        SRV,
        TXT
    }

    @Override
    public String toString() {
        return "Record{" +
               "id=" + id +
               ", type=" + type +
               ", name='" + name + '\'' +
               ", value='" + value + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Record record = (Record) o;
        return Objects.equals(id, record.id) &&
               type == record.type &&
               Objects.equals(name, record.name) &&
               Objects.equals(value, record.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, name, value);
    }
}
