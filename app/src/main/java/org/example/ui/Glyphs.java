package org.example.ui;

import org.example.model.Piece;
import javafx.scene.text.Font;

import java.util.List;

/**
 * Draws pieces with Unicode chess symbols, so no image files are needed.
 * The solid glyphs are used for both colors; CSS fills them white or black.
 */
final class Glyphs {

    // Fonts known to contain the chess symbols. The first one installed is used.
    private static final String[] CANDIDATES = {
            "Apple Symbols",        // macOS
            "Segoe UI Symbol",      // Windows
            "DejaVu Sans",          // Linux
            "Noto Sans Symbols2",
            "FreeSerif"
    };

    private Glyphs() {}   // static helpers only, never instantiated

    static Font font(double size) {
        List<String> installed = Font.getFamilies();
        for (String family : CANDIDATES) {
            if (installed.contains(family)) return Font.font(family, size);
        }
        return Font.font(size);   // fall back to the default font
    }

    static String of(Piece p) {
        return switch (p.getPieceType()) {
            case KING   -> "\u265A";   // ♚
            case QUEEN  -> "\u265B";   // ♛
            case ROOK   -> "\u265C";   // ♜
            case BISHOP -> "\u265D";   // ♝
            case KNIGHT -> "\u265E";   // ♞
            case PAWN   -> "\u265F";   // ♟
        };
    }
}