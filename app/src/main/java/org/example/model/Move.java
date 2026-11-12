package org.example.model;

public class Move {
    private Square fromSquare;
    private Square toSquare;
    private boolean isEnPassant = false;
    private boolean isPromotion = false;
    private PieceType promotionType;

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

        this.isPromotion = isPromotion;
        this.promotionType = promotionType;
    }
    public Square getToSquare() { return toSquare; }
    public Square getFromSquare() { return fromSquare; }
    public boolean isEnPassant() { return isEnPassant; }
    public boolean isPromotion() { return isPromotion; }
    public PieceType getPromotionPieceType() { return promotionType; }
}
