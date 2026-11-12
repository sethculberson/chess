package org.example.model;

public class Move {
    private Square fromSquare;
    private Square toSquare;

    public Move(Square fromSquare, Square toSquare) {
        this.fromSquare = fromSquare;
        this.toSquare = toSquare;
    }
    public Square getToSquare() { return toSquare; }
    public Square getFromSquare() { return fromSquare; }
    public PieceType getPromotion() { return PieceType.QUEEN; }
}
