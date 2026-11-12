package org.example.model;

import java.util.ArrayDeque;
import java.util.Deque;

import org.example.utils.Constants;

public class GameState {
    private Board board;
    private Color turnToMove = Color.WHITE;

    private int epRow;
    private int epCol;

    private Deque<UndoInfo> history = new ArrayDeque<UndoInfo>();

    public GameState() {
        this.board = new Board();
        board.setupStartingPosition();
        this.turnToMove = Color.WHITE;
        // castling rights all true, en passant square = none (-1), etc.
    }


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

        Piece toMove = board.getPieceAt(fromSquare.getRow(), fromSquare.getColumn());

        int capturedRow = toSquare.getRow(), capturedCol = toSquare.getColumn();
        if (move.isEnPassant()) {
            capturedRow = fromSquare.getRow();      // enemy pawn is beside us, on our row
        }
        Piece capturedPiece = board.getPieceAt(capturedRow, capturedCol);   // read AFTER adjusting
        Piece toPlace = toMove; 
        if (move.isPromotion()) {
            toPlace = new Piece(move.getPromotionPieceType(), toMove.getColor());
        }

        board.getSquares()[capturedRow][capturedCol].setCurrentPiece(null);
        board.getSquares()[fromSquare.getRow()][fromSquare.getColumn()].setCurrentPiece(null);
        board.getSquares()[toSquare.getRow()][toSquare.getColumn()].setCurrentPiece(toPlace);

        if (turnToMove == Color.WHITE) { turnToMove = Color.BLACK; } else { turnToMove = Color.WHITE; }

        // Update this when adding castling
        history.push(new UndoInfo(move, toMove, capturedPiece, capturedRow, capturedCol,
                                false, false, false, false, 0, 0));
    }

    public void undoMove() {
        UndoInfo u = history.pop();
        Move m = u.getMove();

        turnToMove = Color.opposite(turnToMove);

        // put the moved piece back (the original pawn, if it promoted)
        board.getSquares()[m.getFromSquare().getRow()][m.getFromSquare().getColumn()].setCurrentPiece(u.getMovedPiece());
        board.getSquares()[m.getToSquare().getRow()][m.getToSquare().getColumn()].setCurrentPiece(null);

        // put the captured piece back where it actually was (may be null = nothing captured)
        if (u.getCapturedPiece() != null) {
            board.getSquares()[u.getCapturedRow()][u.getCapturedCol()].setCurrentPiece(u.getCapturedPiece());
        }

        // castling: move the rook back too (see below)

        // restore the saved state
        /*
        whiteKingside  = u.isWhiteKingside();
        whiteQueenside = u.isWhiteQueenside();
        blackKingside  = u.isBlackKingside();
        blackQueenside = u.isBlackQueenside();
        epRow = u.getEpRow();
        epCol = u.getEpCol();
        halfmoveClock = u.getHalfmoveClock();*/
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
