package com.grevera.pentagostudent;

import javafx.animation.AnimationTimer;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.ImageCursor;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.media.AudioClip;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.Scene;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
/**
 * GUIView is the View in the MVC for the Pentago (Model in MVC).
 * GUIMain is the Controller.
 */
public class GUIView {
    final static private String icon = GUIMain.path + "images/game.jpg";  //name of icon image file
    final static private String cwImage = GUIMain.path + "images/blue-cw2.png";
    final static private String ccwImage = GUIMain.path + "images/blue-ccw2.png";
    //sounds
    final static private String badClickSound, goodClickSound, loserSound, rotateClickSound, undoSound, winnerSound;
    static  {
        //note: "\\\\" is one backslash
        badClickSound    = (GUIMain.path + "sounds/badClick2.mp3").replaceAll( "\\\\", "/" );
        goodClickSound   = (GUIMain.path + "sounds/goodClick2.mp3").replaceAll( "\\\\", "/" );
        loserSound       = (GUIMain.path + "sounds/loser.mp3").replaceAll( "\\\\", "/" );
        rotateClickSound = (GUIMain.path + "sounds/rotateClick2.mp3").replaceAll( "\\\\", "/" );
        undoSound        = (GUIMain.path + "sounds/undo2.mp3").replaceAll( "\\\\", "/" );
        winnerSound      = (GUIMain.path + "sounds/winner.mp3").replaceAll( "\\\\", "/" );
    }
    public AudioClip badSnd, goodSnd, loserSnd, rotateSnd, undoSnd, winnerSnd;
    final private GUIMain ctrl;
    PhongMaterial black;   //black piece
    PhongMaterial white;   //white piece
    PhongMaterial empty;   //no piece
    PhongMaterial box;     //quad
    PhongMaterial winMat;  //winner
    final static private boolean transparent = true;  //no window "surround", title bar, etc.
    private double windowSize;  //window size in pixels
    private double quadSize;  //in pixels
    private double sphereSize;  //in pixels
    final static private double gap = 8;  //in pixels
    final static private double inset = 8;  //in pixels
    Sphere[][] sphere = new Sphere[ GUIMain.SIZE ][ GUIMain.SIZE ];
    Group[] grpQuad = new Group[ GUIMain.quadCount ];
    Box[] boxQuad = new Box[ GUIMain.quadCount ];
    TilePane[] tpQuad = new TilePane[ GUIMain.quadCount ];  ///< container for each quad's pieces
    TilePane tpRoot = new TilePane();  ///< entire playing board (container for quads)
    ImageCursor cwCursor;
    ImageCursor ccwCursor;
    private Text info;
    /**
     *
     * math quads            screen quads array subscripts
     * II  |  I                0  |  1
     * ----------             ---------
     * III |  IV               2  |  3
     *
     * @todo george: fix/change order here and in Pentago Quadrant enum. for
     * some reason, when I do, there is a bug in the undo animation ONLY for
     * Q1 and Q2.
     */
    final private Pentago.Quadrant[] screen_quad_to_math_quad = new Pentago.Quadrant[]{ Pentago.Quadrant.II, Pentago.Quadrant.I, Pentago.Quadrant.III, Pentago.Quadrant.IV };
    //-----------------------------------------------------------------------
    public GUIView ( GUIMain controller, Stage stage ) {
        ctrl = controller;

        stage.setTitle( "Pentago" );
        initSounds();
        initImages( stage );

        if (transparent)    stage.initStyle( StageStyle.TRANSPARENT );
        getWindowSize();
        initMaterials();
        initQuadrants();
        init( stage );
    }
    //-----------------------------------------------------------------------
    private void initSounds ( ) {
        //bad click
        try {
            badSnd = new AudioClip( badClickSound );
        } catch (Exception e) {
            System.err.println( "Error reading back click sound: " + e.getMessage() );
        }
        //good click
        try {
            goodSnd = new AudioClip( goodClickSound );
        } catch (Exception e) {
            System.err.println( "Error reading good click sound: " + e.getMessage() );
        }
        //loser
        try {
            loserSnd = new AudioClip( loserSound );
        } catch (Exception e) {
            System.err.println( "Error reading loser sound: " + e.getMessage() );
        }
        //rotate click
        try {
            rotateSnd = new AudioClip( rotateClickSound );
        } catch (Exception e) {
            System.err.println( "Error reading rotate click sound: " + e.getMessage() );
        }
        //undo
        try {
            undoSnd = new AudioClip( undoSound );
        } catch (Exception e) {
            System.err.println( "Error reading undo sound: " + e.getMessage() );
        }
        //winner
        try {
            winnerSnd = new AudioClip( winnerSound );
        } catch (Exception e) {
            System.err.println( "Error reading winner sound: " + e.getMessage() );
        }
        //goodClick.setVolume( goodClick.getVolume()/5 );
    }
    //-----------------------------------------------------------------------
    private void initImages ( Stage stage ) {
        //get-n-set the app icon
        Image im = null;
        try {
            //stackoverflow.com/questions/10121991/javafx-application-icon
            im = new Image( icon );
            stage.getIcons().add( im );
        } catch (Exception e) {
            System.err.println( "Error reading icon: " + e.getMessage() );
        }
        if (im == null || im.isError())
            System.err.println( "Icon could not be read." );

        //get-n-set the cw cursor (from www.flaticon.com/search?word=undo)
        im = null;
        try {
            im = new Image( cwImage );
            cwCursor = new ImageCursor( im );
        } catch (Exception e) {
            System.err.println( "Error reading cw cursor: " + e.getMessage() );
        }
        if (im == null || im.isError())
            System.err.println( "Cursor cw could not be read." );

        //get-n-set the ccw cursor (from www.flaticon.com/search?word=undo)
        im = null;
        try {
            im = new Image( ccwImage );
            ccwCursor = new ImageCursor( im );
        } catch (Exception e) {
            System.err.println( "Error reading ccw cursor: " + e.getMessage() );
        }
        if (im == null || im.isError())
            System.err.println( "Cursor ccw could not be read." );
    }
    //-----------------------------------------------------------------------
    private void getWindowSize ( ) {
        double sw  = Screen.getPrimary().getBounds().getWidth();
        double sh  = Screen.getPrimary().getBounds().getHeight();
        windowSize = sw;
        if (sh < windowSize)    windowSize = sh;
        windowSize *= 0.90;  //10% smaller

        quadSize   = windowSize / 2 - gap/2 - inset;
        sphereSize = quadSize / 6;

        System.out.println( "windowSize    =" + windowSize );
        System.out.println( "    quadSize  =" + quadSize );
        System.out.println( "    sphereSize=" + sphereSize );
    }
    //-----------------------------------------------------------------------
    private void initMaterials ( ) {
        black = new PhongMaterial();
        black.setSpecularPower( 20 );
        black.setDiffuseColor( Color.BLACK );
        black.setSpecularColor( Color.SILVER );

        white = new PhongMaterial();
        white.setSpecularPower( 50 );
        white.setDiffuseColor( Color.WHITE );
        white.setSpecularColor( Color.SILVER );

        empty = new PhongMaterial();
        empty.setSpecularPower( 5 );
        empty.setDiffuseColor( Color.MAROON );
        empty.setSpecularColor( Color.BLACK );

        box = new PhongMaterial();
        box.setSpecularPower( 5 );
        box.setDiffuseColor( Color.MAROON );
        box.setSpecularColor( Color.DARKRED );

        winMat = new PhongMaterial();
        winMat.setSpecularPower( 20 );
        winMat.setDiffuseColor( Color.GOLD );
        winMat.setSpecularColor( Color.WHITE );
    }
    //-----------------------------------------------------------------------
    private void initQuadrants ( ) {
        initSpheres();
        for (int i=0; i<grpQuad.length; i++) {
            grpQuad[i] = new Group();

            boxQuad[i] = new Box( quadSize, quadSize, 0 );
            boxQuad[i].setMaterial( box );
            //grpQuad[i].getChildren().add( boxQuad[i] );

            tpQuad[i] = new TilePane();
            //tpQuad[i].setStyle( "-fx-border-radius: 10px; -fx-border-width: 1px; -fx-border-color: black; " );
            tpQuad[i].setPrefRows( 3 );
            tpQuad[i].setPrefColumns( 3 );
            //tpQuad[i].setHgap( gap );
            //tpQuad[i].setVgap( gap );
            //tpQuad[i].setPadding( new Insets(inset, inset, inset, inset) );
            tpQuad[i].setTranslateX( -quadSize / 2 );
            tpQuad[i].setTranslateY( -quadSize / 2 );
            addSpheresToQuadrant( i );

            grpQuad[i].getChildren().add( tpQuad[i] );
        }
    }
    //-----------------------------------------------------------------------
    private void addSpheresToQuadrant ( int which ) {
        switch (which) {
            case 0 -> {
                TilePane tp = tpQuad[which];
                for (int r = 0; r < GUIMain.SIZE / 2; r++) {
                    for (int c = 0; c < GUIMain.SIZE / 2; c++) {
                        tp.getChildren().add( sphere[r][c] );
                    }
                }
            }
            case 1 -> {
                TilePane tp = tpQuad[which];
                for (int r = 0; r < GUIMain.SIZE / 2; r++) {
                    for (int c = GUIMain.SIZE / 2; c < GUIMain.SIZE; c++) {
                        tp.getChildren().add( sphere[r][c] );
                    }
                }
            }
            case 2 -> {
                TilePane tp = tpQuad[which];
                for (int r = GUIMain.SIZE / 2; r < GUIMain.SIZE; r++) {
                    for (int c = 0; c < GUIMain.SIZE / 2; c++) {
                        tp.getChildren().add( sphere[r][c] );
                    }
                }
            }
            case 3 -> {
                TilePane tp = tpQuad[which];
                for (int r = GUIMain.SIZE / 2; r < GUIMain.SIZE; r++) {
                    for (int c = GUIMain.SIZE / 2; c < GUIMain.SIZE; c++) {
                        tp.getChildren().add( sphere[r][c] );
                    }
                }
            }
        }
    }
    //-----------------------------------------------------------------------
    private void initSpheres ( ) {
        for (int r = 0; r < GUIMain.SIZE; r++) {
            for (int c = 0; c < GUIMain.SIZE; c++) {
                sphere[r][c] = new Sphere();
                Sphere s = sphere[r][c];
                s.setRadius( sphereSize );
                switch (ctrl.b.board(r, c)) {
                    case Black -> s.setMaterial( black );
                    case White -> s.setMaterial( white );
                    case Empty -> s.setMaterial( empty );
                    default    -> System.err.println( "GUIMain.initSpheres: Something is very wrong!" );
                }
            }
        }
    }
    //-----------------------------------------------------------------------
    private void init ( Stage stage ) {
        tpRoot.setPrefRows( 2 );
        tpRoot.setPrefColumns( 2 );
        tpRoot.setHgap( gap );
        tpRoot.setVgap( gap );
        tpRoot.setStyle( "-fx-background-color:rgb(50,0,0,1); "
                       + "-fx-background-radius: 10px; " );
        tpRoot.setPadding( new Insets(inset, inset, inset, inset) );

        for (Node n : grpQuad) {
            tpRoot.getChildren().add( n );
        }

        info = new Text();
        info.setFont( Font.font("Verdana", 20) );
        info.setTextAlignment( TextAlignment.LEFT );
        info.setFill( Color.rgb(255,100,100,0.5) );
        info.setTranslateX( -0.30*windowSize );
        info.setTranslateY( -0.48*windowSize );
        setInfo();

        StackPane root = new StackPane( tpRoot );
        //root.setStyle( "-fx-border-radius: 50px;" );
        root.getChildren().add( info );

        //create a scene
        Scene scene;
        if (transparent)
            scene = new Scene( root, windowSize, windowSize, Color.TRANSPARENT );
        else
            scene = new Scene( root, windowSize, windowSize, Color.GRAY );

        //specify event handler(s)
        scene.setOnMousePressed( ctrl::doOnMousePressed );
        scene.setOnKeyPressed(   ctrl::doOnKeyPressed   );
        scene.setOnKeyReleased(  ctrl::doOnKeyReleased  );

        stage.setScene( scene );
        stage.show();

        tpRoot.setCursor( Cursor.CROSSHAIR );
    }
    //-----------------------------------------------------------------------
    public void setInfo ( ) {
        if (ctrl.b.getGameOver()) {
            switch (ctrl.b.getWinner()) {
                case Black -> info.setText( "Game over. Black won!" );
                case White -> info.setText( "Game over. White won!" );
                case Draw  -> info.setText( "Game over. Draw." );
                case None  -> info.setText( "Game over." );
            }
            tpRoot.setCursor( Cursor.DEFAULT );
            return;
        }

        if (ctrl.b.getTurn() == null) {
            System.err.println( "Exiting because Pentago.getTurn() returns null." );
            System.exit( 0 );
        }
        switch (ctrl.b.getTurn()) {
            case BlackPlacePiece -> {
                info.setText( "black to place piece" );
                tpRoot.setCursor( Cursor.CROSSHAIR );
            }
            case BlackRotate     -> {
                info.setText( "black to rotate" );
                tpRoot.setCursor( ccwCursor );
            }
            case WhitePlacePiece -> {
                info.setText( "white to place piece" );
                tpRoot.setCursor( Cursor.CROSSHAIR );
            }
            case WhiteRotate     -> {
                info.setText( "white to rotate" );
                tpRoot.setCursor( ccwCursor );
            }
        }
    }
    //-----------------------------------------------------------------------
    /**
     * @todo george: is is really necessary to repaint everything, or just a
     * specific quadrant?
     */
    public void repaint ( ) {
        for (int r = 0; r < GUIMain.SIZE; r++) {
            for (int c = 0; c < GUIMain.SIZE; c++) {
                Sphere s = sphere[r][c];
                switch (ctrl.b.board(r, c)) {
                    case Black -> s.setMaterial( black );
                    case White -> s.setMaterial( white );
                    case Empty -> s.setMaterial( empty );
                    default    -> System.err.println( "repaint: Something is very wrong!" );
                }
            }
        }
    }
    //-----------------------------------------------------------------------
    public synchronized void animateWinner ( ) {
        tpRoot.setCursor( Cursor.DEFAULT );
        final int r = ctrl.b.getWinRow();
        final int c = ctrl.b.getWinCol();
        Pentago.Compass dir = ctrl.b.getWinDir();
        //int len = 1;
        int dr, dc;
        switch (dir) {
            case N  -> { dr = -1; dc =  0; }
            case S  -> { dr =  1; dc =  0; }
            case E  -> { dr =  0; dc =  1; }
            case W  -> { dr =  0; dc = -1; }
            case NE -> { dr = -1; dc =  1; }
            case NW -> { dr = -1; dc = -1; }
            case SE -> { dr =  1; dc =  1; }
            case SW -> { dr =  1; dc = -1; }
            default -> { dr =  0; dc =  0; }
        }
        if (dr == 0 && dc == 0) {
            System.err.println( "animateWinner((): Bad direction." );
            return;
        }

        //int DR = dr, DC = dc;
        AnimationTimer at = new AnimationTimer() {
            private long lastUpdate = 0;
            private int count = 0, localR = r, localC = c;
            @Override public void handle ( long now ) {
                //250 ms delay
                if ((now-lastUpdate) < 250_000_000)    return;
                lastUpdate = now;

                if (count >= ctrl.b.getWinnerLength()) {
                    stop();
                    repaint();
                    return;
                }
                sphere[localR][localC].setMaterial( winMat );
                //get ready for the next one
                ++count;
                localR += dr;
                localC += dc;
            }
        };

        at.start();
    }
    //-----------------------------------------------------------------------
    /** step 1 of rotation: call when one wishes to start animation of
     * rotation. this animation sequence is a bit tricky.
     */
    public synchronized void scaleDown ( boolean cw, Node target, int quadrantPicked, boolean undo ) {
        ScaleTransition st = new ScaleTransition( Duration.millis(250), target );
        st.setByX( -0.2 );  // -0.2 pretty good
        st.setByY( -0.2 );
        st.setOnFinished( e -> rotate(cw, target, quadrantPicked, undo) );
        st.play();  //run the scale down transition
    }
    //-----------------------------------------------------------------------
    /** step 2 of rotation: in general, do not call directly (part of animation process). */
    private synchronized void rotate ( boolean cw, Node target, int quadrantPicked, boolean undo ) {
        RotateTransition rt = new RotateTransition( Duration.millis(500), target );
        //must be final to be used in callback below
        if (cw)    rt.setByAngle(  90 );
        else       rt.setByAngle( -90 );

        rt.setOnFinished( e -> {
            //call appropriate board function to actually rotate the data
            if (cw)    ctrl.b.rotateCW90(  screen_quad_to_math_quad[ quadrantPicked ] );
            else       ctrl.b.rotateCCW90( screen_quad_to_math_quad[ quadrantPicked ] );

            target.setRotate( 0 );
            if (!undo)    repaint();
            scaleUp( target, undo );
        } );
        rt.play();  //run the rotation transition
    }
    //-----------------------------------------------------------------------
    /** step 3 of rotation: in general, do not call directly. */
    private synchronized void scaleUp ( Node target, boolean undo ) {
        ScaleTransition st = new ScaleTransition( Duration.millis(250), target );
        st.setByX( 0.2 );  // 0.2 pretty good
        st.setByY( 0.2 );
        st.setOnFinished( e -> {
            if (rotateSnd != null)    rotateSnd.play();
            if (!undo) {
                ctrl.checkEndOfGame();  //check winner or draw (and play sound)
                setInfo();
            }
            ctrl.busy = false;
        });
        if (undo) {
            ctrl.b = ctrl.last.remove( 0 );
            setInfo();
            repaint();
        }
        st.play();  //run the scale up transition
    }

}
