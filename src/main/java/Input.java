import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Input implements KeyListener, MouseListener, MouseWheelListener, MouseMotionListener {

    Timer tl, tr;
    boolean[] timerRunning = new boolean[]{false, false};
    Point mouseLocation = new Point(0,0);

    public Input(){
        Game.game.addKeyListener(this);
        Game.game.addMouseListener(this);
        Game.game.addMouseWheelListener(this);
        Game.game.addMouseMotionListener(this);
    }

    public void update(){
        mouseLocation.setLocation(MouseInfo.getPointerInfo().getLocation().getX()-Game.game.getLocationOnScreen().getX()-Game.game.insets.left,
                MouseInfo.getPointerInfo().getLocation().getY()-Game.game.getLocationOnScreen().getY()-Game.game.insets.top);
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        switch (Game.game.gameState){
            case MENU:
                switch (keyEvent.getKeyCode()){
                    case KeyEvent.VK_ESCAPE:
                        System.exit(0);
                        break;
                }
                break;
            case LEVEL:
                switch (keyEvent.getKeyCode()){
                    case KeyEvent.VK_ESCAPE:
                        Game.game.togglePause();
                        break;
                    case KeyEvent.VK_LEFT:
                        Game.game.inputs[0] = 1;
                        if(!timerRunning[0]) {
                            Game.game.didHit[0]=0;
                            timerRunning[0] = true;
                            tl = new Timer(100, actionEvent -> {Game.game.canHit[0] = 0; timerRunning[0] = false; if(Game.game.didHit[0]==0)Game.game.combo=0;});
                            tl.setRepeats(false);
                            tl.start();
                        }
                        break;
                    case KeyEvent.VK_RIGHT:
                        Game.game.inputs[1] = 1;
                        if(!timerRunning[1]) {
                            Game.game.didHit[1]=0;
                            timerRunning[1] = true;
                            tr = new Timer(100, actionEvent -> {Game.game.canHit[1] = 0; timerRunning[1] = false; if(Game.game.didHit[1]==0)Game.game.combo=0;});
                            tr.setRepeats(false);
                            tr.start();
                        }
                        break;
                }
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        switch (Game.game.gameState){
            case MENU:
                break;
            case LEVEL:
                switch (keyEvent.getKeyCode()){
                    case KeyEvent.VK_LEFT:
                        Game.game.inputs[0] = 0;
                        tl.stop();
                        timerRunning[0] = false;
                        Game.game.canHit[0] = 1;
                        if(Game.game.didHit[0]==0)Game.game.combo=0;
                        break;
                    case KeyEvent.VK_RIGHT:
                        Game.game.inputs[1] = 0;
                        tr.stop();
                        timerRunning[1] = false;
                        Game.game.canHit[1] = 1;
                        if(Game.game.didHit[1]==0)Game.game.combo=0;
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
        for (int i = 0; i < Game.game.buttons.size(); i++){
            MButton b = Game.game.buttons.get(i);
            if(b.containsMouse()){
                b.performAction();
            }
        }
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
