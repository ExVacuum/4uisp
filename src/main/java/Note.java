import sun.net.util.IPAddressUtil;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

public class Note extends Rectangle {

    private int bar;
    private int delayLeft;
    private float alpha = 255;
    private Timer timer = null;
    private boolean wasPaused =  false;
    public int pointValue;

    public NoteState state;
    public enum NoteState {
        FALLING,
        HIT,
        PERFECT
    }

    public Note(int bar){
        this.bar = bar;
        setBounds(400+bar*100-12,32,24,24);

        state = NoteState.FALLING;

        if(!Game.game.conductor.paused) {
            timer = new Timer(10, actionEvent -> y += 2);
            timer.setRepeats(true);
            timer.start();
        }
    }

    void draw(Graphics2D g2){
        switch (state) {
            case FALLING:
                g2.setColor(new Color(Color.HSBtoRGB(bar / 1.5f, 1f, 0.8f)));
                g2.fillRect((int) getBounds().x, (int) getBounds().y, (int) getBounds().width, (int) getBounds().height);
                break;
            case HIT:
                g2.setColor(new Color(255,255,255, (int)Math.max(alpha,0)));
                g2.fillRect((int) getBounds().x-8, (int) getBounds().y-8, (int) getBounds().width+16, (int) getBounds().height+16);
                g2.setFont(Game.game.def);
                g2.drawString(String.format("%+d",pointValue), x+48,y+16);
                break;
            case PERFECT:
                g2.setColor(new Color(100,255,100, (int)Math.max(alpha,0)));
                g2.fillRect((int) getBounds().x-8, (int) getBounds().y-8, (int) getBounds().width+16, (int) getBounds().height+16);
                g2.setFont(Game.game.def);
                g2.drawString(String.format("%+d",pointValue), x+48,y+16);
                break;
        }
    }

    void update(){

        switch (state) {
            case FALLING:
                if (Game.game.conductor.paused && !wasPaused) {
                    delayLeft = timer.getDelay();
                    timer.stop();
                    wasPaused = true;
                }

                if (!Game.game.conductor.paused && wasPaused) {
                    timer = new Timer(delayLeft, actionEvent -> {
                        y += 2;
                        timer = new Timer(10, actionEvent1 -> y += 2);
                        timer.setRepeats(true);
                        timer.start();
                    });
                    timer.setRepeats(false);
                    timer.start();
                    wasPaused = false;
                }

                for (int i = 0; i < Game.game.conductor.notes.size(); i++) {
                    Note n = Game.game.conductor.notes.get(i);
                    if (!this.equals(n) && getBounds().intersects(n.getBounds()) && n.state==NoteState.FALLING) {
                        Game.game.conductor.trashNotes.add(this);
                        if (Game.game.conductor.trashNotes.contains(n)) Game.game.conductor.trashNotes.remove(n);
                    }
                }

                if (!new Rectangle(0, 0, Game.game.getWidth(), Game.game.getHeight()).contains(getLocation())) {
                    Game.game.conductor.trashNotes.add(this);
                    Game.game.combo = 0;
                }
                break;
            case HIT:
            case PERFECT:
                alpha-=0.01f;
                timer.stop();
                if(alpha<=0){
                    Game.game.conductor.trashNotes.add(this);
                }
                break;
        }
    }
}
