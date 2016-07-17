package com.example.eugene.dddb;

public class Item {
    private final long id;
    private final String value;
    private long prevId;
    private long nextId;

    public Item(long id, String value, long prevId, long nextId) {
        this.id = id;
        this.value = value;
        this.prevId = prevId;
        this.nextId = nextId;
    }

    public void setPrevId(long prevId) {
        this.prevId = prevId;
    }

    public void setNextId(long nextId) {
        this.nextId = nextId;
    }

    public long getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public long getPrevId() {
        return prevId;
    }

    public long getNextId() {
        return nextId;
    }

}