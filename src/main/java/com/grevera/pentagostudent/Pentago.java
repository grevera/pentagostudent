package com.grevera.pentagostudent;

import java.util.ArrayList;
/**
 * pentago board game student version.
 * assignments (in order):
 *     1. getters
 *     2. toString
 *     3. isBoardFull
 *     4. canPlacePiece, placePiece
 *     5. checkWinner(Piece)
 *     6. rotateCW90
 *     7. rotateCCW90
 *     8. equals(Board) (specific)
 *     9. equals(Object) (generic)
 *     10. copy ctor
 *     11. getAllPossibleMoves
 *     12. isRotationOptional
 *     13. hashCode
 */
public class Pentago {
    /*
     * definitions
     */
    /**
     * compass directions (north, south, ...) to indicate direction of winner
     * (or None)
     */
    public enum Compass { N, NE, E, SE, S, SW, W, NW, None }
    /**
     * specific piece at a particular board position or none
     * (or out-of-bounds)
     */
    public enum Piece { Empty, Black, White, OutOfBounds }
    /** <pre>
     quadrants are the same as those in math class (not Java's Math class):
                 |
                 |
          II     |     I
                 |
     ------------+------------
                 |
         III     |     IV
                 |
                 |
     </pre>
     keep the particular order below. (see Pentago.Quadrant in GUIView.)
     */
    public enum Quadrant { II, I, III, IV }
    /** whose turn is it and what must they do? */
    public enum Turn { BlackPlacePiece, BlackRotate, WhitePlacePiece, WhiteRotate }
    /** who won? */
    public enum Winner { Black, White, Draw, None }
    //-----------------------------------------------------------------------
    private Piece[][] board;  ///< game board with pieces
    private boolean gameOver = false;  ///< is the game over?
    /*
     * info for last move below (useful for GUI when playing against computer)
     */
    private int lastCol;  ///< col of last piece placed
    private Piece lastPiece;  ///< last piece placed
    private Quadrant lastQuadrant;  ///< last quad selected
    private int lastRow;  ///< row of last piece placed
    private boolean lastWasPlacePiece;  ///< true = placePiece was called; false = one of rotates was called
    private boolean lastWasRotateCW90;  ///< true = last rotate was rotateCW90; false = last rotate was rotateCCw90

    private int quadSize;  ///< size / 2
    private int size;  ///< board size. default is 6x6. should always be an even no.
    private Turn turn = Turn.WhitePlacePiece;  ///< whose turn is it?
    /*
     * info for winner (if any)
     */
    private int winCol = -1;  ///< starting col of winner
    private Compass winDir = Compass.None;  ///< direction of winner (from start)
    private Winner winner = Winner.None;  ///< who won (including none yet and draw
    private int winnerLength;  ///< required length for a winner (length of actual winner must be >= this)
    private int winRow = -1;  ///< starting row of winner
    //-----------------------------------------------------------------------
    /** no arg ctor. default board size is 6x6, and winner length is 5. */
    public Pentago ( ) { init( 6, 5 ); }
    /**
     * ctor that allows one to specify the board size with a default winner
     * length of 5.
    */
    public Pentago ( int size ) { init( size, 5 ); }
    /**
     * ctor that allows one to specify both the board size and winner length.
     */
    public Pentago ( int size, int winnerLength ) { init( size, winnerLength ); }
    /** * called only by ctors for initialization. */
    private void init ( int size, int winnerLength ) {
        //sanity checks
        assert size >= 0;
        if (size < 1) {  //asserts may be off!
            System.err.println( "Pentago.init: bad size=" + size );
            return;
        }
        assert (size&1) == 0;  //should always be an even number
        if ((size&1) != 0) {
            System.err.println( "Pentago.init: Bad board size. Size must be an even number!" );
            return;
        }
        assert winnerLength >= 2;
        if (winnerLength < 2) {
            System.err.println( "Pentago.init: bad winner length=" + winnerLength );
            return;
        }

        this.size = size;
        this.quadSize = size / 2;
        this.winnerLength = winnerLength;

        board = new Piece[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                board[r][c] = Piece.Empty;
            }
        }
    }

    /** @todo v10: copy ctor. make a deep/independent copy of _everything_. */
    public Pentago ( Pentago copy ) {
    }
    //-----------------------------------------------------------------------
    // getters
    //-----------------------------------------------------------------------
    /**
     * getter for board contents.
     * technically, this should be called getBoard, but I like this better.
     */
    public Piece board ( int r, int c ) {
        if (!isInBounds(r, c))    return Piece.OutOfBounds;
        return this.board[r][c];
    }
    public boolean getGameOver ( ) { return false; }    ///< @todo v1: getter for gameOver
    public Pentago.Quadrant getLastQuadrant ( ) { return null; }    ///< @todo v1: getter for lastQuadrant
    public boolean getLastWasRotateCW90 ( ) { return false; }    ///< @todo v1: getter for lastWasRotateCW90
    public boolean getLastWasPlacePiece ( ) { return false; }    ///< @todo v1: getter for lastWasPlacePiece
    public int getSize ( ) { return -1; }    ///< @todo v1: getter for board size
    public Turn getTurn ( ) { return this.turn; }    ///< @todo v1: getter for player's turn
    public Winner getWinner ( ) { return null; }    ///< @todo v1: getter for winner
    public int getWinnerLength ( ) { return -1; }    ///< @todo v1: getter for min length for a winner
    public int getWinCol () { return -1; }    ///< @todo v1: getter for starting col of winner
    public Compass getWinDir ( ) { return null; }    ///< @todo v1: getter for direction of winner from start
    public int getWinRow ( ) { return -1; }    ///< @todo v1: getter for starting row of winner
    //-----------------------------------------------------------------------
    /**
     * @todo v11:
     * given the current configuration, generate a list of all possible moves.
     * this relies heavily on the copy ctor.
     * @return the array list of possible moves (including one of size 0 for
     * no moves).
     */
    public ArrayList< Pentago > getAllPossibleMoves ( ) {
        return null;
    }

    /** is a specific board position currently empty? */
    public boolean isEmpty ( int r, int c ) {
        return board(r, c) == Piece.Empty;
    }

    /** is position within bounds of the board? */
    public boolean isInBounds ( int r, int c ) {
        if (r < 0 || c < 0 || r >= size || c >= size)    return false;
        return true;
    }

    /** @todo v3: is the board full? */
    public boolean isBoardFull ( ) { return false; }

    /**
     * @todo v4: can the piece be placed at the specified position?
     * do not actually place it there (or alter anything).
     */
    public boolean canPlacePiece ( Piece p, int r, int c ) {
        return true;
    }

    /**
     * @todo v4: place piece at the specified position (but make sure that it
     * can be placed their by first calling canPlacePiece).
     * @sideeffect: don't forget to check for a winner, change to the other
     * player's turn, and update the last move.
     */
    public boolean placePiece ( Piece p, int r, int c ) { return true; }

    //rotates are the trickiest part

    /**
     * @todo v6: rotate the specified quadrant clockwise by 90 degrees.
     * @sideeffect: after a successful rotate, one must check for a winner,
     * game over, board full, and update the last move.
     */
    public boolean rotateCW90 ( Quadrant q ) { return true; }

    /**
     * @todo v7: rotate the specified quadrant counter/anti-clockwise by 90 degrees.
     * @sideeffect: after a successful rotate, one must check for a winner,
     * game over, board full, and update the last move.
     */
    public boolean rotateCCW90 ( Quadrant q ) {
        return true;
    }

    /**
     * check for a winner (typically 5-in-a-row or, more generally,
     * winnerLength-in-a-row, either horizontally, vertically, or
     * diagonally) for both players.
     * @sideeffect sets winner and gameOver vars appropriately
     */
    @PublicForTesting( shouldBe="private" )
    public void checkWinner ( ) {
        boolean blackWon = checkWinner( Piece.Black );
        boolean whiteWon = checkWinner( Piece.White );
        gameOver = blackWon || whiteWon;
        if (blackWon && whiteWon)    winner = Winner.Draw;
        else if (blackWon)           winner = Winner.Black;
        else if (whiteWon)           winner = Winner.White;
        else                         winner = Winner.None;
    }

    /**
     * @todo v5: check for a winner for the specified player.
     */
    private boolean checkWinner ( Piece p ) { return false; }

    /**
     * @todo v11: especially at the beginning of the game, there may be rotations
     * that make no difference. therefore, a rotation may not be necessary.
     * @return true if a rotation is not strictly necessary.
     */
    public boolean isRotateOptional ( ) {
        return false;
    }

    /**
     * @todo v8: generally, two things are 'equals' when _everything_ is
     * exactly the same. considerations:
     *     1. assume that both boards are exactly the same.
     *        assume that the game is over.
     *        (who won matters, of course.)
     *        does it matter whose turn it is?
     *     2. assume that both boards are exactly the same.
     *        assume that the game is over.
     *        (who won matters, of course.)
     *        do any of the 'last' variables matter (i.e., does the last move really matter)?
     *     3. assume that both boards are exactly the same.
     *        assume that the game is over.
     *        (who won matters, of course.)
     *        do any of the 'win' variables matter? (there may be more than one way to win.)
     */
    @Override public boolean equals ( Object o ) {
        Pentago p = (Pentago) o;
        return this.equals( p.board );
    }

    /** @todo v8: @return true if the contents of both _boards_ (only) are exactly the same. */
    private boolean equals ( Piece[][] otherBoard ) {
        return false;
    }

    /** @todo v12 */
    @Override public int hashCode ( ) { return super.hashCode(); }

    /**
     * @todo v2: ye olde toStringe methode.
     * @return
     */
    @Override public String toString ( ) {
        String s = "";

        return s;
    }

}
