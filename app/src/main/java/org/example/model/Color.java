package org.example.model;

public enum Color {
    WHITE,
    BLACK;
    public static Color opposite(Color c) {
        if (c == WHITE) return BLACK;
        return WHITE;
    }
}
