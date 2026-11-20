package org.example.ui;

import org.example.model.Color;
import org.example.model.GameState;
import org.example.model.Move;
import org.example.model.MoveGenerator;
import org.example.model.Piece;
import org.example.model.PieceType;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.List;

/**
 * Draws the board and turns mouse input into moves (click-click or drag-and-drop).
 * Layers: grid of 64 SquareViews, then an overlay holding the dragged "ghost" piece.
 */
public class BoardView extends StackPane {

    public static final double SQ = 80;            // square size in pixels

    private final GridPane grid = new GridPane();
    private final Pane overlay = new Pane();
    private final Text ghost = new Text();
    private final SquareView[][] squares = new SquareView[8][8];
    private final Font pieceFont = Glyphs.font(SQ * 0.8);

    private GameState game;
    private List<Move> legalMoves = List.of();
    private Move lastMove;
    private boolean flipped;

    private int selRow = -1, selCol = -1;          // selected square, -1 = none
    private boolean dragging;

    private Runnable onPositionChanged = () -> {};

    public BoardView(GameState game) {
        this.game = game;

        setMinSize(8 * SQ, 8 * SQ);
        setMaxSize(8 * SQ, 8 * SQ);
        setAlignment(Pos.TOP_LEFT);

        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                squares[r][c] = new SquareView(r, c, pieceFont);

        ghost.setFont(pieceFont);
        ghost.setTextOrigin(VPos.CENTER);
        ghost.setVisible(false);
        overlay.getChildren().add(ghost);
        overlay.setMouseTransparent(true);         // clicks pass through to the board

        getChildren().addAll(grid, overlay);
        layoutSquares();

        setOnMousePressed(this::onPressed);
        setOnMouseDragged(this::onDragged);
        setOnMouseReleased(this::onReleased);

        positionChanged();
    }

    // ---------- public API used by ChessApp ----------

    public GameState getGame()        { return game; }
    public List<Move> getLegalMoves() { return legalMoves; }
    public void setOnPositionChanged(Runnable r) { onPositionChanged = r; }

    public void newGame(GameState newGame) {
        game = newGame;
        lastMove = null;
        clearSelection();
        positionChanged();
    }

    public void undo() {
        if (!game.canUndo()) return;
        game.undoMove();
        lastMove = null;
        clearSelection();
        positionChanged();
    }

    public void flip() {
        flipped = !flipped;
        layoutSquares();
        redraw();
    }

    // ---------- mouse handling ----------

    private void onPressed(MouseEvent e) {
        if (e.getButton() != MouseButton.PRIMARY) {   // right-click cancels selection
            clearSelection();
            redraw();
            return;
        }
        int[] sq = toModel(e.getX(), e.getY());
        if (sq == null) return;
        int r = sq[0], c = sq[1];

        // click-click: something is selected and this square is one of its targets
        if (hasSelection() && tryMove(selRow, selCol, r, c)) return;

        Piece p = pieceAt(r, c);
        if (p != null && p.getColor() == game.getTurnToMove() && !legalMoves.isEmpty()) {
            selRow = r;
            selCol = c;
            dragging = true;
            redraw();                                 // hides the piece on its square
            showGhost(p, e.getX(), e.getY());
        } else {
            clearSelection();
            redraw();
        }
    }

    private void onDragged(MouseEvent e) {
        if (dragging) moveGhost(e.getX(), e.getY());
    }

    private void onReleased(MouseEvent e) {
        if (!dragging) return;
        dragging = false;
        ghost.setVisible(false);

        int[] sq = toModel(e.getX(), e.getY());
        boolean droppedElsewhere = sq != null && !(sq[0] == selRow && sq[1] == selCol);
        if (droppedElsewhere && tryMove(selRow, selCol, sq[0], sq[1])) return;

        redraw();   // illegal drop or plain click: piece snaps back, stays selected
    }

    /** Plays from -> to if legal. Returns true if the input was used. */
    private boolean tryMove(int fr, int fc, int tr, int tc) {
        List<Move> candidates = legalMoves.stream()
                .filter(m -> m.getFromSquare().getRow() == fr && m.getFromSquare().getColumn() == fc
                          && m.getToSquare().getRow() == tr && m.getToSquare().getColumn() == tc)
                .toList();
        if (candidates.isEmpty()) return false;

        dragging = false;
        ghost.setVisible(false);

        Move chosen = candidates.get(0);
        if (candidates.size() > 1) {               // several moves to one square = promotion
            PieceType choice = askPromotion();
            if (choice == null) {                  // dialog cancelled
                clearSelection();
                redraw();
                return true;
            }
            chosen = candidates.stream()
                    .filter(m -> m.getPromotionPieceType() == choice)
                    .findFirst()
                    .orElse(candidates.get(0));
        }

        game.makeMove(chosen);
        lastMove = chosen;
        clearSelection();
        positionChanged();
        return true;
    }

    private PieceType askPromotion() {
        Dialog<PieceType> dialog = new Dialog<>();
        dialog.setTitle("Promotion");
        dialog.setHeaderText("Promote pawn to:");
        if (getScene() != null) dialog.initOwner(getScene().getWindow());

        ButtonType queen  = new ButtonType("Queen", ButtonBar.ButtonData.OK_DONE);
        ButtonType rook   = new ButtonType("Rook");
        ButtonType bishop = new ButtonType("Bishop");
        ButtonType knight = new ButtonType("Knight");
        dialog.getDialogPane().getButtonTypes().addAll(queen, rook, bishop, knight, ButtonType.CANCEL);

        dialog.setResultConverter(bt ->
                bt == queen  ? PieceType.QUEEN  :
                bt == rook   ? PieceType.ROOK   :
                bt == bishop ? PieceType.BISHOP :
                bt == knight ? PieceType.KNIGHT : null);

        return dialog.showAndWait().orElse(null);
    }

    // ---------- drawing ----------

    /** After the position changes: recompute legal moves, redraw, notify the app. */
    private void positionChanged() {
        legalMoves = MoveGenerator.getLegalMoves(game);
        redraw();
        onPositionChanged.run();
    }

    private void redraw() {
        Color toMove = game.getTurnToMove();
        boolean inCheck = game.isInCheck(toMove);

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                SquareView sq = squares[r][c];
                Piece p = pieceAt(r, c);

                sq.showPiece(p);
                sq.setPieceVisible(!(dragging && r == selRow && c == selCol));

                // later assignments win: selected > check > last move
                String tint = null;
                if (lastMove != null
                        && ((r == lastMove.getFromSquare().getRow() && c == lastMove.getFromSquare().getColumn())
                         || (r == lastMove.getToSquare().getRow()   && c == lastMove.getToSquare().getColumn()))) {
                    tint = "last-move";
                }
                if (inCheck && p != null && p.getPieceType() == PieceType.KING && p.getColor() == toMove) {
                    tint = "check";
                }
                if (r == selRow && c == selCol) {
                    tint = "selected";
                }
                sq.setTint(tint);
                sq.setMarker(SquareView.Marker.NONE);
            }
        }

        if (hasSelection()) {
            for (Move m : legalMoves) {
                if (m.getFromSquare().getRow() == selRow && m.getFromSquare().getColumn() == selCol) {
                    boolean capture = pieceAt(m.getToSquare().getRow(), m.getToSquare().getColumn()) != null;
                    squares[m.getToSquare().getRow()][m.getToSquare().getColumn()]
                            .setMarker(capture ? SquareView.Marker.CAPTURE : SquareView.Marker.DOT);
                }
            }
        }
    }

    /** Places squares in the grid (honoring flip) and sets a-h / 1-8 labels. */
    private void layoutSquares() {
        grid.getChildren().clear();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                int displayRow = flipped ? 7 - r : r;
                int displayCol = flipped ? 7 - c : c;
                SquareView sq = squares[r][c];
                sq.setFileLabel(displayRow == 7 ? String.valueOf((char) ('a' + c)) : null);
                sq.setRankLabel(displayCol == 0 ? String.valueOf(8 - r) : null);
                grid.add(sq, displayCol, displayRow);
            }
        }
    }

    private void showGhost(Piece p, double x, double y) {
        ghost.setText(Glyphs.of(p));
        ghost.getStyleClass().setAll("piece", p.getColor() == Color.WHITE ? "white" : "black");
        ghost.setVisible(true);
        moveGhost(x, y);
    }

    private void moveGhost(double x, double y) {
        ghost.setX(x - ghost.getLayoutBounds().getWidth() / 2);
        ghost.setY(y);                             // text origin is CENTER
    }
 
    // ---------- helpers ----------

    /** Pixel position -> model {row, col}, or null if outside the board. */
    private int[] toModel(double x, double y) {
        int dc = (int) Math.floor(x / SQ);
        int dr = (int) Math.floor(y / SQ);
        if (dr < 0 || dr > 7 || dc < 0 || dc > 7) return null;
        return flipped ? new int[]{7 - dr, 7 - dc} : new int[]{dr, dc};
    }

    private Piece pieceAt(int r, int c) {
        return game.getBoard().getSquares()[r*8 + c].getCurrentPiece();
    }

    private boolean hasSelection() { return selRow >= 0; }

    private void clearSelection() {
        selRow = -1;
        selCol = -1;
        dragging = false;
        ghost.setVisible(false);
    }
}