package org.example.model;

public class UndoInfo {
    private final Move move;

    // what was captured, and where (not always the destination: en passant!)
    private final Piece capturedPiece;
    private final int capturedRow, capturedCol;

    // the piece that moved, as it was BEFORE the move (needed to undo promotion)
    private final Piece movedPiece;

    // game state that a move can change and that can't be recomputed
    private final boolean whiteKingside, whiteQueenside, blackKingside, blackQueenside;
    private final int epRow, epCol;

    public UndoInfo(Move move, Piece movedPiece,
                    Piece capturedPiece, int capturedRow, int capturedCol,
                    boolean whiteKingside, boolean whiteQueenside,
                    boolean blackKingside, boolean blackQueenside,
                    int epRow, int epCol) {
        this.move = move;
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
        this.capturedRow = capturedRow;
        this.capturedCol = capturedCol;
        this.whiteKingside = whiteKingside;
        this.whiteQueenside = whiteQueenside;
        this.blackKingside = blackKingside;
        this.blackQueenside = blackQueenside;
        this.epRow = epRow;
        this.epCol = epCol;
    }

    public Move getMove()            { return move; }
    public Piece getMovedPiece()     { return movedPiece; }
    public Piece getCapturedPiece()  { return capturedPiece; }
    public int getCapturedRow()      { return capturedRow; }
    public int getCapturedCol()      { return capturedCol; }
    public boolean isWhiteKingside() { return whiteKingside; }
    public boolean isWhiteQueenside(){ return whiteQueenside; }
    public boolean isBlackKingside() { return blackKingside; }
    public boolean isBlackQueenside(){ return blackQueenside; }
    public int getEpRow()            { return epRow; }
    public int getEpCol()            { return epCol; }
}
