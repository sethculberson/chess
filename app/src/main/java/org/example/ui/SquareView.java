package org.example.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import static org.example.ui.BoardView.SQ;

import org.example.model.Color;
import org.example.model.Piece;

/**
 * One square of the board. Layers (bottom to top):
 * background, highlight tint, coordinate labels, move marker, piece.
 * All colors come from chess.css.
 */
final class SquareView extends StackPane {

    enum Marker { NONE, DOT, CAPTURE }

    private final Rectangle tint = new Rectangle(SQ, SQ);
    private final Circle marker = new Circle();
    private final Text piece = new Text();
    private final Text fileLabel = new Text();
    private final Text rankLabel = new Text();

    SquareView(int row, int col, Font pieceFont) {
        boolean light = (row + col) % 2 == 0;      // a8 = (0,0) is a light square

        Rectangle background = new Rectangle(SQ, SQ);
        background.getStyleClass().addAll("square", light ? "light" : "dark");

        tint.getStyleClass().add("tint");

        // coordinate labels use the opposite square color so they stay readable
        String coordColor = light ? "on-light" : "on-dark";
        fileLabel.getStyleClass().addAll("coord", coordColor);
        rankLabel.getStyleClass().addAll("coord", coordColor);
        StackPane.setAlignment(fileLabel, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(fileLabel, new Insets(0, 4, 2, 0));
        StackPane.setAlignment(rankLabel, Pos.TOP_LEFT);
        StackPane.setMargin(rankLabel, new Insets(2, 0, 0, 4));

        marker.setVisible(false);
        piece.setFont(pieceFont);

        // order matters: later children are drawn on top
        getChildren().addAll(background, tint, fileLabel, rankLabel, marker, piece);
        setMinSize(SQ, SQ);
        setPrefSize(SQ, SQ);
        setMaxSize(SQ, SQ);
    }

    void showPiece(Piece p) {
        if (p == null) {
            piece.setText("");
            return;
        }
        piece.setText(Glyphs.of(p));
        piece.getStyleClass().setAll("piece", p.getColor() == Color.WHITE ? "white" : "black");
    }

    void setPieceVisible(boolean visible) {
        piece.setVisible(visible);
    }

    /** cssClass: "selected", "check", "last-move", or null for none. */
    void setTint(String cssClass) {
        tint.getStyleClass().setAll("tint");
        if (cssClass != null) tint.getStyleClass().add(cssClass);
    }

    void setMarker(Marker m) {
        marker.setVisible(m != Marker.NONE);
        if (m == Marker.DOT) {
            marker.setRadius(SQ * 0.16);
            marker.getStyleClass().setAll("target-dot");
        } else if (m == Marker.CAPTURE) {
            marker.setRadius(SQ / 2 - 4);
            marker.getStyleClass().setAll("capture-ring");
        }
    }

    void setFileLabel(String text) { fileLabel.setText(text == null ? "" : text); }
    void setRankLabel(String text) { rankLabel.setText(text == null ? "" : text); }
}
