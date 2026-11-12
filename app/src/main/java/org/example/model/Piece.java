package org.example.model;

public class Piece {
    private int value;
    private PieceType pieceType;
    private Color color;

    public Piece(PieceType pieceType, Color color) {
        this.pieceType = pieceType;
        this.color = color;

        switch (pieceType) {
            case QUEEN -> value = 9;
            case BISHOP -> value = 3;
            case KNIGHT -> value = 3;
            case ROOK -> value = 5;
            case PAWN -> value = 1;
            case KING -> value = 0;
        }
    }

    public int getValue() { return value; }

    public PieceType getPieceType() { return pieceType; }

    public Color getColor() { return color; }
}
