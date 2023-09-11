package com.grevera.pentagostudent;

//import com.grevera.mcts.MCTSUCB;
//import com.grevera.mcts.PureMCTS;
//import com.grevera.mcts.PureMCTSV2;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.util.ArrayList;
/** <pre>
 *  how to enable asserts in intellij: <a href="https://se-education.org/guides/tutorials/intellijUsefulSettings.html">...</a>
 *  how to create a new JavaFX project (w/ media and/or gl support):
 *      run intellij
 *      New Project
 *      click on JavaFX
 *      specify the project name
 *      Next
 *      Additional libraries: FXGL
 *      Create
 *
 *      edit HelloApplication.java
 *      insert: import javafx.scene.media.MediaPlayer;
 *      mouse over the above
 *      choose: Add required ...
 *
 *      it should now build and run!
 *
 *  root (2x2; tile pane)
 *  4 groups
 *  </pre>
 */
public class GUIMain extends Application {
    final static public String path = "file:///" + System.getProperty("user.dir") + "/";
    final static public int SIZE = 6;  //game board size
    public Pentago b = new Pentago( SIZE );  //game board
    public ArrayList< Pentago > last = new ArrayList<>();  //for undo
    final static public int quadCount = 4;  //no. of quads (obviously)
    private boolean soundOn = true;
    public boolean busy = false;  //block multiple, concurrent animations
    private boolean busyCursor = false;
    private GUIView view;
    //-----------------------------------------------------------------------
    @Override public void start ( Stage stage ) {
        System.out.println( "Working Directory = " + System.getProperty("user.dir") );
        view = new GUIView( this, stage );
    }
    //-----------------------------------------------------------------------
    /** event handler. do not call directly. */
    public synchronized void doOnMousePressed ( MouseEvent m ) {  //respond to mouse clicks
        if (busyCursor) {
            System.err.println( "Busy. Ignoring mouse click." );
            return;
        }
        if (view.goodSnd != null)    view.goodSnd.play();
        if (b.getGameOver()) {
            if (view.badSnd != null)    view.badSnd.play();
            return;
        }

        //probably don't need busy var because of sync.
        if (busy)    return;  //only one at a time!
        busy = true;

        Node p = m.getPickResult().getIntersectedNode();  //what was picked?
        System.out.println( "picked:" + p );

        //time to place a piece?
        if (b.getTurn() == Pentago.Turn.BlackPlacePiece || b.getTurn() == Pentago.Turn.WhitePlacePiece) {
            tryToPlaceAPiece( p );
        } else {
            //otherwise, rotate a quadrant
            tryToRotateAQuadrant( m, p );
        }
    }
    //-----------------------------------------------------------------------
    private boolean tryToPlaceAPiece ( Node p ) {
        Pentago copy = new Pentago( b );
        //time to place a piece. determine what (piece position) was clicked on.
        boolean foundIt = false;
        boolean ok = false;
        //determine what _piece_ position was clicked on
        outer:
        for (int r=0; r<SIZE; r++) {
            for (int c=0; c<SIZE; c++) {
                if (p == view.sphere[r][c]) {
                    System.out.println( "(r,c) = " + r + "," + c );
                    //attempt to actually place a piece
                    if (b.getTurn() == Pentago.Turn.BlackPlacePiece) {
                        ok = b.placePiece( Pentago.Piece.Black, r, c );
                        if (ok)    view.sphere[r][c].setMaterial( view.black );
                    } else if (b.getTurn() == Pentago.Turn.WhitePlacePiece) {
                        ok = b.placePiece( Pentago.Piece.White, r, c );
                        if (ok)    view.sphere[r][c].setMaterial( view.white );
                    }
                    foundIt = true;
                    break outer;
                }
            }
        }  //end outer

        //check foundIt and ok and make some sound.
        if (!foundIt) {
            System.err.println( "Please select a piece position." );
            if (view.badSnd != null)    view.badSnd.play();
        } else if (!ok) {
            System.err.println( "Please perform a valid move." );
            if (view.badSnd != null)    view.badSnd.play();
        } else {
            //ok is true
            view.repaint();
            last.add( 0, copy );
        }
        checkEndOfGame();  //check winner or draw (and play sound)

        busy = false;
        view.setInfo();
        return ok;
    }
    //-----------------------------------------------------------------------
    private boolean tryToRotateAQuadrant ( MouseEvent m, Node p ) {
        Pentago copy = new Pentago( b );
        //not time to place a piece, so it must be time to rotate. determine
        // quadrant where click occurred. allowable possibilities are box,
        // tile pane (associated with quadrant), or sphere.
        int quadrantPicked = -1;
        //check box quad, group quad, or tile pane quad
        for (int i=0; i<view.boxQuad.length; i++) {
            if (p == view.boxQuad[i] || p == view.grpQuad[i] || p == view.tpQuad[i]) {
                quadrantPicked = i;
                break;
            }
        }

        //nothing found above? then check individual spheres in each quadrant.
        if (quadrantPicked == -1) {  //check top-left
            outer:
            for (int r = 0; r < SIZE / 2; r++) {
                for (int c = 0; c < SIZE / 2; c++) {
                    if (p == view.sphere[r][c]) {
                        quadrantPicked = 0;
                        break outer;
                    }
                }
            }
        }
        if (quadrantPicked == -1) {  //check top-right
            outer:
            for (int r = 0; r < SIZE / 2; r++) {
                for (int c = SIZE / 2; c < SIZE; c++) {
                    if (p == view.sphere[r][c]) {
                        quadrantPicked = 1;
                        break outer;
                    }
                }
            }
        }
        if (quadrantPicked == -1) {  //check bottom-left
            outer:
            for (int r = SIZE / 2; r < SIZE; r++) {
                for (int c = 0; c < SIZE / 2; c++) {
                    if (p == view.sphere[r][c]) {
                        quadrantPicked = 2;
                        break outer;
                    }
                }
            }
        }
        if (quadrantPicked == -1) {  //check bottom-right
            outer:
            for (int r = SIZE / 2; r < SIZE; r++) {
                for (int c = SIZE / 2; c < SIZE; c++) {
                    if (p == view.sphere[r][c]) {
                        quadrantPicked = 3;
                        break outer;
                    }
                }
            }
        }

        if (quadrantPicked == -1) {
            System.err.println( "bad pick:" + p );
            busy = false;
            return false;
        }

        //finally, let's animate the target quadrant
        Node target = view.grpQuad[ quadrantPicked ];
        //determine rotation direction (clockwise or counterclockwise)
        boolean cw = m.isAltDown() || m.isControlDown() || m.isShiftDown();
        //scale down, rotate, and then scale back up
        view.scaleDown( cw, target, quadrantPicked, false );  //start rotation
        last.add( 0, copy );
        return true;
    }
    //-----------------------------------------------------------------------
    /**
     * check winner or draw (and play sound)
     */
    public void checkEndOfGame ( ) {
        if (b.getWinner() == null)    return;
        switch (b.getWinner()) {
            case Black -> {
                System.out.println( "Black won!" );
                if (view.winnerSnd != null)    view.winnerSnd.play();
                view.animateWinner();
            }
            case White -> {
                System.out.println( "White won!" );
                if (view.winnerSnd != null)    view.winnerSnd.play();
                view.animateWinner();
            }
            case Draw -> {
                System.out.println( "Draw!" );
                if (view.loserSnd != null)    view.loserSnd.play();
            }
        }
    }
    //-----------------------------------------------------------------------
    /**
     * event handler. do not call directly.
     * toughest part of this was to make sure that we don't allow events to
     * overlap each other, AND don't allow them to queue up and be processed
     * one after the other in the future. the solution is to ignore additional
     * events when we are busy processing one.
     */
    public void doOnKeyPressed ( KeyEvent e ) {
        synchronized (this) {
            if (busyCursor) {
                if (e.getText().equalsIgnoreCase("q") || e.getText().equalsIgnoreCase("x")) {
                    System.out.println( "Bye!" );
                    System.exit( 0 );
                }
                System.err.println( "Busy. Ignoring " + e.getText() + "." );
                return;  //ignore event
            }
            //System.err.println( "Not busy." );
            busyCursor = true;
        }
        if (e == null)    return;
        String choice = e.getText().toLowerCase();
        switch (choice) {
            case "h" -> help();
            case "c", "?", "/" -> hint();  //computer suggested move
            case "n" -> {  //new game
                b = new Pentago( b.getSize(), b.getWinnerLength() );
                view.repaint();
                view.setInfo();
            }
            case "p" -> {
                //print out the undo list (last moves)
                System.out.println( "----------" );
                for (Pentago p : last) {
                    System.out.println( p );
                }
                System.out.println( "----------" );
            }
            case "q" , "x" -> {  //quit/exit
                System.out.println( "Bye!" );
                System.exit( 0 );
            }
            case "r" -> view.repaint();
            case "s" -> {  //toggle sound
                soundOn = !soundOn;
                System.out.println( "sound is " + (soundOn ? "" : "not ")+ "on" );
            }
            case "t" -> test();
            case "u" -> undo();
            default  -> {  //handle modifier key(s) for rotate
                if (e.isAltDown() || e.isControlDown() || e.isShiftDown()) {
                    System.out.println( "alt/ctrl/shift" );
                    if (b.getTurn() == Pentago.Turn.BlackRotate || b.getTurn() == Pentago.Turn.WhiteRotate)
                        view.tpRoot.setCursor( view.cwCursor );
                } else {
                    System.err.println( e + " unrecognized" );
                    help();
                }
            }
        }
        //since the computer hint make take a longer period of time to
        // calculate, it must be handled separately and by a separate
        // task/thread/
        switch (choice) {
            case "c", "?", "/" -> { }  //hint
            default -> { synchronized (this) { busyCursor = false; } }  //everything but hint
        }
    }
    //-----------------------------------------------------------------------
    private void help ( ) {
        System.out.println( "h=help, n=new game, q=quit, r=repaint, s=sound on/off, t=test, u=undo" );
    }
    //-----------------------------------------------------------------------
    /** event handler. do not call directly. */
    public void doOnKeyReleased ( KeyEvent e ) {
        if (e.isAltDown())              return;
        if (e.isControlDown())          return;
        if (e.isShiftDown())            return;
        if (!e.getText().equals(""))    return;
        if (b.getTurn() == Pentago.Turn.BlackRotate || b.getTurn() == Pentago.Turn.WhiteRotate) {
            view.tpRoot.setCursor( view.ccwCursor );
        }
    }
    //-----------------------------------------------------------------------
    /**
     * <a href="https://blog.idrsolutions.com/tutorial-change-default-cursor-javafx/">...</a>
     * computationally intensive tasks (such as hint()) should not run on the main UI thread.
     */
    private void hint ( ) {
        Task<?> task = new Task<>() {
            @Override protected Void call () {
                view.tpRoot.setCursor( Cursor.WAIT );     //change cursor to wait
                try {
                    /*
                    MCTSUCB pm = new MCTSUCB( b );     //determine best, next move
                    Pentago copy = new Pentago( b );     //make a copy of current (for undo list)
                    last.add( 0, copy );           //save current on undo list
                    b = pm.bestGameData;                 //apply best, next move
                    */
                } catch (AssertionError e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                view.setInfo();
                view.repaint();
                synchronized (this) { busyCursor = false; }
                return null;
            }
        };

        Thread th = new Thread( task );
        th.setDaemon( true );
        th.start();
    }
    //-----------------------------------------------------------------------
    private void test ( ) {
        b.placePiece( Pentago.Piece.White, 0, 0 );    view.repaint();    view.setInfo();
        b.rotateCCW90( Pentago.Quadrant.I );                view.repaint();    view.setInfo();
        b.placePiece( Pentago.Piece.Black, 1, 0 );    view.repaint();    view.setInfo();
        b.rotateCCW90( Pentago.Quadrant.I );                view.repaint();    view.setInfo();
        b.placePiece( Pentago.Piece.White, 1, 1 );    view.repaint();    view.setInfo();
        b.rotateCCW90( Pentago.Quadrant.I );                view.repaint();    view.setInfo();
        b.placePiece( Pentago.Piece.Black, 2, 0 );    view.repaint();    view.setInfo();
        b.rotateCCW90( Pentago.Quadrant.I );                view.repaint();    view.setInfo();
        b.placePiece( Pentago.Piece.White, 2, 2 );    view.repaint();    view.setInfo();
        b.rotateCCW90( Pentago.Quadrant.I );                view.repaint();    view.setInfo();
        b.placePiece( Pentago.Piece.Black, 3, 0 );    view.repaint();    view.setInfo();
        b.rotateCCW90( Pentago.Quadrant.I );                view.repaint();    view.setInfo();
        b.placePiece( Pentago.Piece.White, 3, 3 );    view.repaint();    view.setInfo();
        b.rotateCCW90( Pentago.Quadrant.I );                view.repaint();    view.setInfo();
        b.placePiece( Pentago.Piece.Black, 4, 0 );    view.repaint();    view.setInfo();
        b.rotateCCW90( Pentago.Quadrant.I );                view.repaint();    view.setInfo();
        b.placePiece( Pentago.Piece.White, 4, 4 );    view.repaint();    view.setInfo();

        checkEndOfGame();
    }
    //-----------------------------------------------------------------------
    /** very basic undo-the-last-move. */
    private void undo_basic ( ) {
        System.out.println( "undo" );
        if (last.isEmpty()) {
            System.err.println( "Can't undo." );
            if (view.badSnd != null)    view.badSnd.play();
            return;
        }
        if (view.undoSnd != null)    view.undoSnd.play();

        b = last.remove( 0 );
        view.setInfo();
        view.repaint();
    }
    //-----------------------------------------------------------------------
    /** undo-the-last-move w/ animation. @todo: needs work! */
    private void undo ( ) {
        if (last.isEmpty()) {
            System.err.println( "Can't undo." );
            if (view.badSnd != null)    view.badSnd.play();
            return;
        }
        if (view.undoSnd != null)    view.undoSnd.play();

        if (b.getLastWasPlacePiece()) {
            b = last.remove( 0 );
            view.setInfo();
            view.repaint();
        } else {
            //here if last was rotate
            int quadrantPicked = b.getLastQuadrant().ordinal();
            Node target = view.grpQuad[ quadrantPicked ];
            if (b.getLastWasRotateCW90()) {
                view.scaleDown( false, target, quadrantPicked, true );
            } else {
                view.scaleDown( true, target, quadrantPicked, true );
            }
        }
    }
    //-----------------------------------------------------------------------
    public static void main ( String[] args ) {
        launch( args );
    }

}
