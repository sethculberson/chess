package org.example.utils;

import org.example.model.Board;
import org.example.model.Color;
import org.example.model.GameState;
import org.example.model.Piece;
import org.example.model.PieceType;

public class Utils {
    public static GameState fenParser(String fen) {
        String[] fields = fen.split(" ");

        String piecePositions = fields[0];

        char sideToMove = fields[1].charAt(0);

        String castlingRights = fields[2];
        boolean whiteCanCastleKingside = castlingRights.indexOf('K') != -1;
        boolean whiteCanCastleQueenside = castlingRights.indexOf('Q') != -1;
        boolean blackCanCastleKingside = castlingRights.indexOf('k') != -1;
        boolean blackCanCastleQueenside = castlingRights.indexOf('q') != -1;

        String enPassantTargetSquare = fields[3];
        int epRow = -1, epCol = -1;
        if (!enPassantTargetSquare.equals("-")) {
            char[] chars = enPassantTargetSquare.toCharArray();
            int adjust = 49;

            epRow = 8 - ((int) (chars[0] - adjust));
            epCol = (int) chars[1];
        }

        int halfMoveClock = Integer.parseInt(fields[4]);
        int fullMoveNumber = Integer.parseInt(fields[5]);


        GameState fenGameState = new GameState(getBoard(piecePositions), sideToMove == 'w' ? Color.WHITE : Color.BLACK, epRow, epCol, whiteCanCastleKingside, whiteCanCastleQueenside, blackCanCastleKingside, blackCanCastleQueenside, halfMoveClock, fullMoveNumber);
        
        return fenGameState;
    }

    private static Board getBoard(String state) {
        Board board = new Board();
        String[] rows = state.split("/");

        for (int r = 0; r < rows.length; r++) {
            char[] chars = rows[r].toCharArray();
            int columnIndex = 0;

            for (int c = 0; c < chars.length; c++) {
                if (chars[c] >= 49 && chars[c] <= 57) {
                    columnIndex += chars[c]-'0';
                } else {
                    board.setPieceAt(r, columnIndex, getPieceFromChar(chars[c]));
                    columnIndex++;
                }
            }
        }

        return board;
    }

    private static Piece getPieceFromChar(char c) {
        Color color = c >= 97 ? Color.BLACK : Color.WHITE;
        if (c < 97) c += 32;

        switch (c) {
            case 'r': return new Piece(PieceType.ROOK, color);
            case 'n': return new Piece(PieceType.KNIGHT, color);
            case 'b': return new Piece(PieceType.BISHOP, color);
            case 'q': return new Piece(PieceType.QUEEN, color);
            case 'k': return new Piece(PieceType.KING, color);
            case 'p': return new Piece(PieceType.PAWN, color);

            default: throw new IllegalArgumentException("Character is not a piece.");
        }
    }
}
