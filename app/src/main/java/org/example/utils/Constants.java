package org.example.utils;

public class Constants {
    public static final int[][] KNIGHT_MOVES = {
        {2,1}, {2,-1}, {1,2}, {1,-2}, {-2,1}, {2,-1}, {-1,2}, {-1,-2}
    };
    public static final int[][] BISHOP_MOVES = {
        {1,1}, {1,-1}, {-1,1}, {-1,-1}
    };
    public static final int[][] ROOK_MOVES = {
        {1,0}, {-1,0}, {0,1}, {0,-1}
    };
    public static final int[][] KING_AND_QUEEN_MOVES = {
        {1,1}, {1,-1}, {-1,1}, {-1,-1},
        {1,0}, {-1,0}, {0,1}, {0,-1}
    };
}
