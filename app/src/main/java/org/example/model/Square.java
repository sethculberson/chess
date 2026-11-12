package org.example.model;

public class Square {
    private int row;
    private int column;
    private Piece currentPiece;

    public Square(int r, int c) {
        row = r;
        column = c;
        currentPiece = null;
    }

    public int getRow() { return row; }
    public int getColumn() { return column; }
    public void setCurrentPiece(Piece piece) { this.currentPiece = piece; }
    public Piece getCurrentPiece() { return currentPiece; }
}
