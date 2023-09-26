package com.grevera.pentagostudent;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Scanner;
/**
 * this code reads input from the keyboard or a string (if readFromKeyboard is
 * false), and plays the game of pentago.
 *
 * how to enable asserts in intellij:
 *   https://se-education.org/guides/tutorials/intellijUsefulSettings.html
 */
public class ConsoleMain {
    static boolean readFromKeyboard = true;  // (technically, System.in)

    public static void main ( String[] args ) {
        //input can be read from either the keyboard (viz., System.in),
        // or a string that we define (very useful for testing).
        Scanner in = new Scanner( System.in );
        if (!readFromKeyboard) {    //"read" from a string
            //java text block (https://docs.oracle.com/en/java/javase/17/text-blocks/index.html)
            String s = """
                    1 1 1 2
                    0 1 1 2
                    2 2 1 1
                    0 2 1 1
                    3 3 1 1
                    0 3 1 1
                    4 4 1 1
                    0 4 1 1
                    5 5 1 1
                    0 0 1 1
                    """;
            InputStream is = new ByteArrayInputStream( s.getBytes() );
            in = new Scanner( is );
            System.out.println( "Reading input from string." );
        }

        Pentago b = new Pentago();
        outer:
        while (!b.getGameOver()) {
            System.out.print( b );
            //besides game over, the game has four states as defined by the
            // Turn enum.
            String who = "?";
            if (b.getTurn() == null) {
                System.err.println( "Exiting because Pentago.getTurn() returns null." );
                System.exit( 0 );
            }
            switch (b.getTurn()) {
                case BlackPlacePiece, BlackRotate -> who = "black";
                case WhitePlacePiece, WhiteRotate -> who = "white";
            }
            System.out.println( who + "'s turn." );
            switch (b.getTurn()) {

                case BlackPlacePiece, WhitePlacePiece -> {    //place a piece
                    //get where to place a piece
                    System.out.print( "  Enter r c --> " );
                    //handle row
                    String rs = in.next();
                    if (!readFromKeyboard)    System.out.print( rs + " " );
                    if (rs.equalsIgnoreCase("q"))    break outer;
                    if (rs.equalsIgnoreCase("x"))    break outer;
                    //handle col
                    String cs = in.next();
                    if (!readFromKeyboard)    System.out.println( cs + " " );
                    //parse row and col
                    int r, c;
                    try {
                        r = Integer.parseInt( rs );
                    } catch (Exception e) {
                        System.err.println( "Bad row. Try again." );
                        continue;  //try again
                    }
                    try {
                        c = Integer.parseInt( cs );
                    } catch (Exception e) {
                        System.err.println( "Bad col. Try again." );
                        continue;  //try again
                    }
                    //place piece
                    boolean ok = false;
                    switch (b.getTurn()) {
                        case BlackPlacePiece -> ok = b.placePiece( Pentago.Piece.Black, r, c );
                        case WhitePlacePiece -> ok = b.placePiece( Pentago.Piece.White, r, c );
                        default -> System.err.println( "ConsoleMain: turn=" + b.getTurn() + " Something is very wrong!" );
                    }
                    if (!ok) {
                        System.err.println( "ConsoleMain: You can't place a piece there! Try again." );
                        continue;  //try again
                    }

                }

                case BlackRotate, WhiteRotate -> {  //rotate a quadrant
                    //first, pick a quadrant (1..4)
                    System.out.print( "  rotate quadrant [1..4] --> " );
                    String qs = in.next();
                    if (!readFromKeyboard)    System.out.println( qs );
                    qs = qs.toLowerCase();
                    Pentago.Quadrant q;
                    switch (qs) {
                        case "1", "i"   -> q = Pentago.Quadrant.I;
                        case "2", "ii"  -> q = Pentago.Quadrant.II;
                        case "3", "iii" -> q = Pentago.Quadrant.III;
                        case "4", "iv"  -> q = Pentago.Quadrant.IV;
                        case "q", "x"   -> { break outer; }  //why {} required?
                        default -> {
                            System.err.println( "ConsoleMain: Bad quadrant. Something is very wrong." );
                            continue;  //try again
                        }
                    }
                    //second, indicate direction cw or ccw
                    System.out.print( "  direction (cw/1 or ccw/2) --> " );
                    String dir = in.next();
                    if (!readFromKeyboard)    System.out.println( dir );
                    dir = dir.toLowerCase();
                    switch (dir) {
                        case "cw",  "1" -> b.rotateCW90( q );
                        case "ccw", "2" -> b.rotateCCW90( q );
                        case "q", "x"   -> { break outer; }  //why {} required?
                        default -> {
                            System.err.println( "ConsoleMain: Bad rotation direction!" );
                            continue;  //try again
                        }
                    }  //end switch dir
                }
            }

        }  //end while

        System.out.println( b );
        switch (b.getWinner()) {
            case Black -> System.out.println( "Black won! Start (r,c)=" + b.getWinRow() + "," + b.getWinCol() + ". Direction=" + b.getWinDir() + "." );
            case White -> System.out.println( "White won! Start (r,c)=" + b.getWinRow() + "," + b.getWinCol() + ". Direction=" + b.getWinDir() + "." );
            case Draw  -> System.out.println( "It's a draw!" );
            case None  -> System.out.println( "Quit." );
        }
        System.out.println( "Bye!" );
    }
}
