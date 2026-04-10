package com.pch.mng.jql;

public class JqlParseException extends RuntimeException {

    private final int position;

    public JqlParseException(String message, int position) {
        super(message);
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
