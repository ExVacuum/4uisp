import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Input implements KeyListener, MouseListener, MouseWheelListener, MouseMotionListener {

    //Timers for handling player inputs
    Timer tl, tr;
    boolean[] timerRunning = new boolean[]{false, false};

    //Inputs down, ability to hit, and success of current hit
    int[] inputs = new int[]{0,0};
    int[] canHit = new int[]{1,1};
    int[] didHit = new int[]{0,0};

    //Mouse location
    Point mouseLocation = new Point(0,0);

    /**
     * Create a new input handler, which contains keyboard and mouse listeners.
     */
    public Input(){
        Game.game.addKeyListener(this);
        Game.game.addMouseListener(this);
        Game.game.addMouseWheelListener(this);
        Game.game.addMouseMotionListener(this);
    }

    /**
     * Updates mouse location, taking window insets into account.
     */
    public void update(){
        mouseLocation.setLocation(MouseInfo.getPointerInfo().getLocation().getX()-Game.game.getLocationOnScreen().getX()-Game.game.insets.left,
                MouseInfo.getPointerInfo().getLocation().getY()-Game.game.getLocationOnScreen().getY()-Game.game.insets.top);
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {

        //Handle pretty much every keypress depending on the game states
        switch (Game.game.gameState){
            case MENU:
                switch (keyEvent.getKeyCode()){
                    case KeyEvent.VK_ESCAPE:

                        //Quit Game
                        System.exit(0);
                        break;
                }
                break;
            case LEVEL:
                switch (keyEvent.getKeyCode()){
                    case KeyEvent.VK_ESCAPE:

                        //Toggle pause
                        Game.game.togglePause();
                        break;
                    case KeyEvent.VK_LEFT:

                        //Left bar input
                        inputs[0] = 1;

                        //If timer is not already running
                        if(!timerRunning[0]) {

                            //Reset success to false
                            didHit[0]=0;

                            //Start timer which limits length of input to 100ms, and discourages spamming, as combo breaks if hit is unsuccessful.
                            tl = new Timer(100, actionEvent -> {canHit[0] = 0; timerRunning[0] = false; if(didHit[0]==0)Game.game.combo=0;});
                            tl.setRepeats(false);
                            tl.start();

                            //Timer is now running
                            timerRunning[0] = true;
                        }
                        break;
                    case KeyEvent.VK_RIGHT:

                        //Right bar input
                        inputs[1] = 1;

                        //If timer is not already running
                        if(!timerRunning[1]) {

                            //Reset success to false
                            didHit[1]=0;

                            //Start timer which limits length of input to 100ms, and discourages spamming, as combo breaks if hit is unsuccessful.
                            tr = new Timer(100, actionEvent -> {canHit[1] = 0; timerRunning[1] = false; if(didHit[1]==0)Game.game.combo=0;});
                            tr.setRepeats(false);
                            tr.start();

                            //Timer is now running
                            timerRunning[1] = true;
                        }
                        break;
                }
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {

        //Cancel inputs early
        switch (Game.game.gameState){
            case MENU:
                break;
            case LEVEL:
                switch (keyEvent.getKeyCode()){
                    case KeyEvent.VK_LEFT:

                        //Input is now false
                        inputs[0] = 0;

                        //Stop timer
                        tl.stop();
                        timerRunning[0] = false;

                        //Enable hitting again
                        canHit[0] = 1;

                        //Break combo if failed
                        if(didHit[0]==0)Game.game.combo=0;
                        break;
                    case KeyEvent.VK_RIGHT:

                        //Input is now false
                        inputs[1] = 0;

                        //Stop timer
                        tr.stop();
                        timerRunning[1] = false;

                        //Enable hitting again
                        canHit[1] = 1;

                        //Break combo if failed
                        if(didHit[1]==0)Game.game.combo=0;
                        break;
                }
                break;
        }
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {

        //Check for button presses
        for (int i = 0; i < Game.game.buttons.size(); i++){
            MButton b = Game.game.buttons.get(i);
            if(b.containsMouse()){
                b.performAction();
            }
        }

        //In case of SETUP screen, check the file chooser buttons as well
        if (Game.game.gameState == Game.GameState.MENU && Game.game.menuScreen == Game.MenuScreen.SETUP){
            for (int i = 0; i < Game.game.fileChooser.buttons.size(); i++){
                MButton b = Game.game.fileChooser.buttons.get(i);
                if(b.containsMouse()){
                    b.performAction();
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
    }
}
