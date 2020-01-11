import java.awt.*;
import java.awt.event.*;

public class Input implements KeyListener, MouseListener, MouseWheelListener, MouseMotionListener {

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
        switch (keyEvent.getKeyCode()){
            case KeyEvent.VK_ESCAPE:
                System.exit(0);
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {

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
