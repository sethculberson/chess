package org.example.model;

import java.util.ArrayDeque;
import java.util.Deque;

import org.example.utils.Constants;

public class GameState {
    private Board board;
    private Color turnToMove = Color.WHITE;

    private int epRow;
    private int epCol;

    private boolean whiteKingside = true;
    private boolean whiteQueenside = true;
    private boolean blackKingside = true;
    private boolean blackQueenside = true;

    private Deque<UndoInfo> history = new ArrayDeque<UndoInfo>();

    public GameState() {
        this.board = new Board();
        board.setupStartingPosition();
        this.turnToMove = Color.WHITE;
        // castling rights all true, en passant square = none (-1), etc.
    }

    public boolean canCastleKingside(Color turn) { return turn == Color.WHITE ? whiteKingside : blackKingside; }
    public boolean canCastleQueenside(Color turn) { return turn == Color.WHITE ? whiteQueenside : blackQueenside; }

    public boolean canUndo() { return !history.isEmpty(); }

    public boolean isInCheck(Color side) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = board.getPieceAt(r, c);
                if (piece == null) continue;
                if (piece.getColor() == side && piece.getPieceType() == PieceType.KING) {
                    return (
                        this.isSquareAttacked(r, c, Color.opposite(side))
                    );
                }
            }
        }
        throw new IllegalStateException("NO KING ON BOARD");
    }

    public Board getBoard() { return board; }

    public Color getTurnToMove() { return turnToMove; }

    public void makeMove(Move move) {
        Square fromSquare = move.getFromSquare();
        Square toSquare = move.getToSquare();
        int fromRow = fromSquare.getRow(), fromCol = fromSquare.getColumn();
        int toRow = toSquare.getRow(), toCol = toSquare.getColumn();

        Piece toMove = board.getPieceAt(fromRow, fromCol);

        int capturedRow = toRow, capturedCol = toCol;
        if (move.isEnPassant()) {
            capturedRow = fromRow;
        }
        Piece capturedPiece = board.getPieceAt(capturedRow, capturedCol);
        history.push(new UndoInfo(move, toMove, capturedPiece, capturedRow, capturedCol,
                                whiteKingside, whiteQueenside, blackKingside, blackQueenside, 0, 0));

        Piece placed = toMove;
        if (move.isPromotion()) {
            placed = new Piece(move.getPromotionPieceType(), toMove.getColor());
        }
        board.setPieceAt(capturedRow, capturedCol, null);
        board.setPieceAt(fromRow, fromCol, null);
        board.setPieceAt(toRow, toCol, placed);

        // Castling: the king moved two columns, so move the rook as well
        if (toMove.getPieceType() == PieceType.KING && Math.abs(toCol - fromCol) == 2) {
            if (toCol == 6) {                       // kingside
                board.setPieceAt(fromRow, 5, board.getPieceAt(fromRow, 7));
                board.setPieceAt(fromRow, 7, null);
            } else {                                // queenside
                board.setPieceAt(fromRow, 3, board.getPieceAt(fromRow, 0));
                board.setPieceAt(fromRow, 0, null);
            }
        }

        // Any move from or to these squares loses castling rights
        updateCastlingRights(fromRow, fromCol);
        updateCastlingRights(toRow, toCol);

        if (turnToMove == Color.WHITE) { turnToMove = Color.BLACK; } else { turnToMove = Color.WHITE; }
    }

    private void updateCastlingRights(int row, int col) {
        if (row == 7 && col == 4) { whiteKingside = false; whiteQueenside = false; }
        if (row == 0 && col == 4) { blackKingside = false; blackQueenside = false; }
        if (row == 7 && col == 7) whiteKingside = false;
        if (row == 7 && col == 0) whiteQueenside = false;
        if (row == 0 && col == 7) blackKingside = false;
        if (row == 0 && col == 0) blackQueenside = false;
    }

    public void undoMove() {
        UndoInfo u = history.pop();
        // Castling: move the rook back
        Move m = u.getMove();
        int fromRow = m.getFromSquare().getRow();
        int fromCol = m.getFromSquare().getColumn();
        int toCol = m.getToSquare().getColumn();

        if (u.getMovedPiece().getPieceType() == PieceType.KING && Math.abs(toCol - fromCol) == 2) {
            if (toCol == 6) {
                board.setPieceAt(fromRow, 7, board.getPieceAt(fromRow, 5));
                board.setPieceAt(fromRow, 5, null);
            } else {
                board.setPieceAt(fromRow, 0, board.getPieceAt(fromRow, 3));
                board.setPieceAt(fromRow, 3, null);
            }
        }

        // Restore the saved state
        whiteKingside  = u.isWhiteKingside();
        whiteQueenside = u.isWhiteQueenside();
        blackKingside  = u.isBlackKingside();
        blackQueenside = u.isBlackQueenside();

        turnToMove = Color.opposite(turnToMove);

        // put the moved piece back (the original pawn, if it promoted)
        board.getSquares()[m.getFromSquare().getRow()][m.getFromSquare().getColumn()].setCurrentPiece(u.getMovedPiece());
        board.getSquares()[m.getToSquare().getRow()][m.getToSquare().getColumn()].setCurrentPiece(null);

        // put the captured piece back where it actually was (may be null = nothing captured)
        if (u.getCapturedPiece() != null) {
            board.getSquares()[u.getCapturedRow()][u.getCapturedCol()].setCurrentPiece(u.getCapturedPiece());
        }
    }

    public int getEnPassantRow() { return epRow; }

    public int getEnPassantCol() { return epCol; }

    public boolean isSquareAttacked(int r, int c, Color attackerColor) {
        // By PAWNS
        int pawnDirection = attackerColor == Color.WHITE ? 1 : -1;

        if (board.isPiece(r+pawnDirection, c+1, PieceType.PAWN, attackerColor) ||
            board.isPiece(r+pawnDirection, c-1, PieceType.PAWN, attackerColor))
            return true;
        
        // By KNIGHTS
        for (int[] d : Constants.KNIGHT_MOVES) {
            if (board.isPiece(r+d[0], c+d[1], PieceType.KNIGHT, attackerColor)) return true;
        }

        // By BISHOPS (+ Queen)
        if (castRays(r, c, Constants.BISHOP_MOVES, attackerColor, PieceType.BISHOP)) return true;

        // By ROOKS (+ Queen)
        if (castRays(r, c, Constants.ROOK_MOVES, attackerColor, PieceType.ROOK)) return true;

        // By King
        for (int[] d : Constants.KING_AND_QUEEN_MOVES) {
            if (board.isPiece(r+d[0], c+d[1], PieceType.KING, attackerColor)) return true;
        }
        return false;
    }

    private boolean castRays(int r, int c, int[][] directions, Color attackerColor, PieceType pieceAttacking) {
        for (int[] direction : directions) {
            int dR = direction[0], dC = direction[1];

            int rRay = r+dR; 
            int cRay = c+dC;

            while (isValidSquare(rRay, cRay)) {
                Piece piece = board.getPieceAt(rRay, cRay);
                if (piece != null) {
                    if (piece.getColor() == attackerColor && (piece.getPieceType() == pieceAttacking || piece.getPieceType() == PieceType.QUEEN))
                        return true;
                    break;
                }

                // keep sliding
                rRay += dR;
                cRay += dC;
            }
        }
        return false;
    }

    public static boolean isValidSquare(int r, int c) {
        return (r < 8 && r >= 0) && (c < 8 && c >= 0);
    }

    public Move getLastMove() {
        return history.isEmpty() ? null : history.peek().getMove();
    }
}
