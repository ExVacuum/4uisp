import java.awt.*;
import java.awt.event.ActionListener;

public class MButton extends Rectangle{

    private Color bgC =  Color.WHITE;
    private Color textC =  Color.BLACK;
    private Color borderC = Color.BLACK;
    private String text = "Text";
    private Font fnt = Game.game.h2;

    private boolean bordered = true;

    public MButton(int x, int y, int width, int height){
        setBounds(x,y,width, height);
    }

    public void draw(Graphics2D g2){
        g2.setColor(bgC);
        g2.fillRect(x,y,width,height);
        if(containsMouse()){
            g2.setColor(Game.game.transBlack);
            g2.fillRect(x,y,width,height);
        }
        if(bordered) {
            g2.setColor(borderC);
            g2.drawRect(x, y, width, height);
        }
        g2.setFont(fnt);
        g2.setColor(textC);
        g2.drawString(text, (float)getX()+10, (float)getMaxY()-10);
    }

    public void performAction(){};

    public void setBordered(boolean bordered){
        this.bordered = bordered;
    }

     public boolean containsMouse(){
        return contains(Game.game.input.mouseLocation);
     }

     public void setText(String text){
        this.text = text;
     }

    public void setBgC(Color bgC){
        this.bgC = bgC;
    }

    public void setTextC(Color textC){
        this.textC = textC;
    }

    public void setBorderC(Color borderC){
        this.borderC = borderC;
    }

    public void setFnt(Font fnt){
        this.fnt = fnt;
    }
}
