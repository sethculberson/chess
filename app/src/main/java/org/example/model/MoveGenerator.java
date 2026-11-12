package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class MoveGenerator {
    public static List<Move> getPseudoLegalMoves(GameState gs) {
        Board board = gs.getBoard();
        List<Move> moves = new ArrayList<Move>();

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = board.getPieceAt(r, c);
                if (piece == null || piece.getColor() != gs.getTurnToMove()) {
                   continue; 
                }
                switch (piece.getPieceType()) {
                    case PAWN -> pawnMoves(gs, r, c, moves);
                    case KNIGHT -> knightMoves(gs, r, c, moves);
                    case BISHOP -> bishopMoves(gs, r, c, moves);
                    case ROOK -> rookMoves(gs, r, c, moves);
                    case QUEEN -> queenMoves(gs, r, c, moves);
                    case KING -> kingMoves(gs, r, c, moves);
                    default -> System.out.println("ERR");
                }
            }
        }
        return moves;
    }

    public static List<Move> getLegalMoves(GameState gs) {
        List<Move> legal = new ArrayList<Move>();

        Color currentPlayer = gs.getTurnToMove();

        for (Move move : getPseudoLegalMoves(gs)) {
            gs.makeMove(move);
            if (!gs.isInCheck(currentPlayer)) legal.add(move);
            gs.undoMove();
        }
        
        return legal;
    }

    public static void pawnMoves(GameState gs, int row, int column, List<Move> moves) {
        Color turn = gs.getTurnToMove();
        Board board = gs.getBoard();

        int direction = turn == Color.WHITE ? -1 : 1;

        if (board.getPieceAt(row + direction, column) == null) {
            addPawnMove(board, row, column, row + direction, column, turn, moves);

            if ((row == 6 && turn == Color.WHITE) || (row == 1 && turn == Color.BLACK)) {
                if (board.getPieceAt(row + direction*2, column) == null) {
                    moves.add(new Move(board.getSquares()[row][column], board.getSquares()[row+direction*2][column]));
                }
            }
        }

        if (column > 0) {
            Piece leftDiagonal = board.getPieceAt(row+direction, column-1);
            if (leftDiagonal != null && leftDiagonal.getColor() != turn) {
                addPawnMove(board, row, column, row + direction, column - 1, turn, moves);
            }
        }

        if (column < 7) {
            Piece rightDiagonal = board.getPieceAt(row+direction, column+1);
            if (rightDiagonal != null && rightDiagonal.getColor() != turn) {
                addPawnMove(board, row, column, row + direction, column + 1, turn, moves);
            }
        }

        Move lastMove = gs.getLastMove();
        if (lastMove != null) {
            Square lastTo = lastMove.getToSquare();
            Square lastFrom = lastMove.getFromSquare();
            Piece lastPieceMoved = board.getPieceAt(lastTo.getRow(), lastTo.getColumn());

            if (lastPieceMoved != null &&
                lastPieceMoved.getPieceType() == PieceType.PAWN &&
                lastPieceMoved.getColor() != turn &&
                lastTo.getRow() == row &&
                Math.abs(lastTo.getColumn() - column) == 1 &&
                Math.abs(lastFrom.getRow() - lastTo.getRow()) == 2) {

                moves.add(new Move(board.getSquares()[row][column],
                                board.getSquares()[row + direction][lastTo.getColumn()],
                                true));
            }
        }


    }

    private static void addPawnMove(Board board, int fromRow, int fromCol, int toRow, int toCol,
                                    Color turn, List<Move> moves) {
        Square from = board.getSquares()[fromRow][fromCol];
        Square to = board.getSquares()[toRow][toCol];
        int promotionRow = turn == Color.WHITE ? 0 : 7;

        if (toRow == promotionRow) {
            for (PieceType type : PieceType.PROMOTION_PIECES) {
                moves.add(new Move(from, to, true, type));
            }
        } else {
            moves.add(new Move(from, to));
        }
    }

    public static void knightMoves(GameState gs, int row, int column, List<Move> moves) {
        Color turn = gs.getTurnToMove();
        Board board = gs.getBoard();

        int[][] directions = {
            {2, 1}, {2, -1}, {1, 2}, {-1, 2}, {-2, 1}, {-2, -1}, {1, -2}, {-1, -2}
        };

        for (int[] d : directions) {
            int dR = d[0], dC = d[1];
            int row2 = row+dR, col2 = column+dC;

            if (!isValidSquare(row2, col2)) continue;
            Piece target = board.getPieceAt(row2, col2);
            if (target != null && target.getColor() == turn) continue;

            moves.add(new Move(board.getSquares()[row][column], board.getSquares()[row2][col2], false, null));
        }
    }

    public static void bishopMoves(GameState gs, int row, int column, List<Move> moves) {
        int[][] directions = {
            {1,1}, {1,-1}, {-1, 1}, {-1,-1}
        };

        slidingMoves(gs, row, column, directions, moves);
    }

    public static void rookMoves(GameState gs, int row, int column, List<Move> moves) {
        int[][] directions = {
            {1,0}, {-1,0}, {0, 1}, {0,-1}
        };

        slidingMoves(gs, row, column, directions, moves);
    }

    public static void queenMoves(GameState gs, int row, int column, List<Move> moves) {
        int[][] directions = {
            {1,0}, {-1,0}, {0, 1}, {0,-1}, // Rook moves
            {1,1}, {1,-1}, {-1, 1}, {-1,-1} // Bishop moves
        };

        slidingMoves(gs, row, column, directions, moves);
    }

    public static void kingMoves(GameState gs, int row, int column, List<Move> moves) {
        Color turn = gs.getTurnToMove();
        Board board = gs.getBoard();

        int[][] directions = {
            {1,1}, {1,0}, {1,-1},
            {0,1}, {0,-1},
            {-1,1}, {-1,0}, {-1,-1}
        };

        for (int[] d : directions) {
            int dR = d[0], dC = d[1];
            int row2 = row+dR, col2 = column + dC;

            if (!isValidSquare(row2, col2)) continue;
            Piece piece = board.getPieceAt(row2, col2);
            if (piece == null || piece.getColor() != turn) {
                moves.add(new Move(board.getSquares()[row][column], board.getSquares()[row2][col2], false, null));
            }
        }
    }

    private static void slidingMoves(GameState gs, int row, int column, int[][] directions, List<Move> moves) {
        Color turn = gs.getTurnToMove();
        Board board = gs.getBoard();

        for (int[] d : directions) {
            boolean isUnblocked = true;
            int currRow = row, currCol = column;
            int dR = d[0], dC = d[1];
            while (isUnblocked) {
                int row2 = currRow + dR, col2 = currCol + dC;
                if (!isValidSquare(row2, col2)) break;
                Piece piece = board.getPieceAt(row2, col2);
                if (piece != null) isUnblocked = false;
                if (piece == null || piece.getColor() != turn) {
                    moves.add(new Move(board.getSquares()[row][column], board.getSquares()[row2][col2], false, null));
                }
                currRow = row2; currCol = col2;
            }
        }
    }

    public static boolean isValidSquare(int row, int column) {
        return ((row < 8 && row >= 0) && (column < 8 && column >= 0));
    }
}
