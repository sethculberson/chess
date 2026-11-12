package org.example.model;

public class Move {
    private Square fromSquare;
    private Square toSquare;
    private boolean isEnPassant = false;

    public Move(Square fromSquare, Square toSquare) {
        this.fromSquare = fromSquare;
        this.toSquare = toSquare;
    }

    public Move(Square fromSquare, Square toSquare, boolean isEnPassant) {
        this.fromSquare = fromSquare;
        this.toSquare = toSquare;
        this.isEnPassant = isEnPassant;
    }

    public Move(Square fromSquare, Square toSquare, boolean isPromotion, PieceType promotionType) {
        this.fromSquare = fromSquare;
        this.toSquare = toSquare;

        // More logic ...
    }
    public Square getToSquare() { return toSquare; }
    public Square getFromSquare() { return fromSquare; }
    public PieceType getPromotion() { return PieceType.QUEEN; }
    public boolean isEnPassant() { return isEnPassant; }
}
