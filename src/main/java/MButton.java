import java.awt.*;

public class MButton extends Rectangle{

    //Colors
    private Color bgC =  Color.WHITE;
    private Color textC =  Color.BLACK;
    private Color borderC = Color.BLACK;

    //Text & Font
    private String text = "Text";
    private Font fnt = Game.game.h2;

    //Allows borderless buttons
    private boolean bordered = true;

    /**
     * Creates a button with specified dimensions
     * @param x X coordinate
     * @param y Y coordinate
     * @param width width
     * @param height height
     */
    public MButton(int x, int y, int width, int height){
        setBounds(x,y,width, height);
    }

    /**
     * Draw this button
     * @param g2 Graphics2D object for drawing
     */
    public void draw(Graphics2D g2){

        //Background
        g2.setColor(bgC);
        g2.fillRect(x,y,width,height);

        //Hover
        if(containsMouse()){
            g2.setColor(Game.game.transBlack);
            g2.fillRect(x,y,width,height);
        }

        //Border
        if(bordered) {
            g2.setColor(borderC);
            g2.drawRect(x, y, width, height);
        }

        //Text
        g2.setFont(fnt);
        g2.setColor(textC);
        g2.drawString(text, (float)getX()+10, (float)getMaxY()-10);
    }

    /**
     * Action to perform, should be overridden when creating.
     */
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
