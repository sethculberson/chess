package org.example.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.example.model.Board;
import org.example.model.Color;
import org.example.model.GameState;
import org.example.model.MoveGenerator;
import org.example.model.Piece;
import org.example.model.PieceType;
import org.junit.jupiter.api.Test;

public class UtilsTest {
    private static final String START_FEN =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    // ---------- helpers ----------

    private static void assertPiece(Board board, int row, int col, PieceType type, Color color) {
        Piece p = board.getPieceAt(row, col);
        assertNotNull(p, "expected a piece at " + row + "," + col);
        assertEquals(type, p.getPieceType(), "wrong type at " + row + "," + col);
        assertEquals(color, p.getColor(), "wrong color at " + row + "," + col);
    }

    private static void assertEmpty(Board board, int row, int col) {
        assertNull(board.getPieceAt(row, col), "expected empty square at " + row + "," + col);
    }

    // ---------- tests ----------

    @Test
    void startingFenMatchesStartingPosition() {
        Board parsed = Utils.fenParser(START_FEN).getBoard();
        Board expected = new GameState().getBoard();

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece a = expected.getPieceAt(r, c);
                Piece b = parsed.getPieceAt(r, c);
                if (a == null) {
                    assertNull(b, "expected empty at " + r + "," + c);
                } else {
                    assertNotNull(b, "missing piece at " + r + "," + c);
                    assertEquals(a.getPieceType(), b.getPieceType(), "type at " + r + "," + c);
                    assertEquals(a.getColor(), b.getColor(), "color at " + r + "," + c);
                }
            }
        }
    }

    @Test
    void startingFenHasWhiteToMoveAndAllCastlingRights() {
        GameState gs = Utils.fenParser(START_FEN);

        assertEquals(Color.WHITE, gs.getTurnToMove());
        assertTrue(gs.canCastleKingside(Color.WHITE));
        assertTrue(gs.canCastleQueenside(Color.WHITE));
        assertTrue(gs.canCastleKingside(Color.BLACK));
        assertTrue(gs.canCastleQueenside(Color.BLACK));
    }

    @Test
    void parsesPiecesEmptySquaresAndSideToMove() {
        // After 1.e4: White pawn on e4, e2 empty, Black to move
        GameState gs = Utils.fenParser("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1");
        Board board = gs.getBoard();

        assertPiece(board, 4, 4, PieceType.PAWN, Color.WHITE);   // e4
        assertEmpty(board, 6, 4);                                 // e2
        assertEmpty(board, 4, 3);                                 // d4: the "4" before P was read as empties
        assertEmpty(board, 4, 5);                                 // f4: the "3" after P too
        assertEquals(Color.BLACK, gs.getTurnToMove());
    }

    @Test
    void parsesPartialCastlingRights() {
        GameState gs = Utils.fenParser("r3k2r/8/8/8/8/8/8/R3K2R w Kq - 0 1");

        assertTrue(gs.canCastleKingside(Color.WHITE));
        assertFalse(gs.canCastleQueenside(Color.WHITE));
        assertFalse(gs.canCastleKingside(Color.BLACK));
        assertTrue(gs.canCastleQueenside(Color.BLACK));
    }

    @Test
    void parsesNoCastlingRights() {
        GameState gs = Utils.fenParser("r3k2r/8/8/8/8/8/8/R3K2R w - - 0 1");

        assertFalse(gs.canCastleKingside(Color.WHITE));
        assertFalse(gs.canCastleQueenside(Color.WHITE));
        assertFalse(gs.canCastleKingside(Color.BLACK));
        assertFalse(gs.canCastleQueenside(Color.BLACK));
    }

    @Test
    void startingPositionHas20LegalMoves() {
        assertEquals(20, MoveGenerator.getLegalMoves(Utils.fenParser(START_FEN)).size());
    }

    @Test
    void kiwipeteHas48LegalMoves() {
        // Standard test position: castling both ways, pins, captures everywhere
        GameState gs = Utils.fenParser(
                "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
        assertEquals(48, MoveGenerator.getLegalMoves(gs).size());
    }

}
