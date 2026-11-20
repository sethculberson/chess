package org.example.model;

public class Board {
    private Square[] squares = new Square[64];

    public Board() {
        for (int i = 0; i < squares.length; i++) {
            squares[i] = new Square(i/8, i%8);
        }
    }

    public Square[] getSquares() { return squares; }
    public Piece getPieceAt(int row, int col) {
        return squares[rowColToIndex(row, col)].getCurrentPiece();
    }

    public boolean isPiece(int row, int col, PieceType pieceType, Color color) {
        if (!GameState.isValidSquare(row, col)) return false;

        int index = row*8 + col;
        Piece p = squares[index].getCurrentPiece();

        if (p != null && p.getPieceType() == pieceType && p.getColor() == color) return true;
        return false;
    }

    public void setPieceAt(int row, int col, Piece piece) {
        squares[rowColToIndex(row, col)].setCurrentPiece(piece);
    }

    public void setupStartingPosition() {
        PieceType[] backRank = {
            PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
            PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        };

        for (int c = 0; c < 8; c++) {
            setPieceAt(0, c, new Piece(backRank[c], Color.BLACK));
            setPieceAt(1, c, new Piece(PieceType.PAWN, Color.BLACK));
            setPieceAt(6, c, new Piece(PieceType.PAWN, Color.WHITE));
            setPieceAt(7, c, new Piece(backRank[c], Color.WHITE));
        }
    }

    public static int rowColToIndex(int row, int col) { return row*8 + col; }
}
