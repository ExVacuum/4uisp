import javax.swing.*;
import java.awt.*;

public class Note extends Rectangle {

    //Which bar the note is on
    private int bar;

    //delay left on timer, determined if game is paused
    private int delayLeft;

    private float alpha = 255;

    //Timer for synchronized movement
    private Timer timer = null;
    private boolean wasPaused =  false;

    //Point value, determined once activated by player hit
    public int pointValue;

    //State of note
    public NoteState state;
    public enum NoteState {
        FALLING,
        HIT,
        PERFECT
    }

    /**
     * Create a new note on a specified bar
     * @param bar bar to put note on (should be 0 or 1)
     */
    public Note(int bar){
        this.bar = bar;
        setBounds(400+bar*100-12,32,24,24);

        state = NoteState.FALLING;

        // Move 2px every 10ms, meaning the notes will hit the end of the 500px long bars exactly 2.5s after they are created, in sync with the foreground song.
        if(!Game.game.conductor.paused) {
            timer = new Timer(10, actionEvent -> y += 2);
            timer.setRepeats(true);
            timer.start();
        }
    }

    /**
     * Draw the note
     * @param g2 Graphics2D object for drawing
     */
    void draw(Graphics2D g2){
        switch (state) {
            case FALLING:

                //Draw notes red on left, blue on right
                g2.setColor(new Color(Color.HSBtoRGB(bar / 1.5f, 1f, 0.8f)));
                g2.fillRect(getBounds().x, getBounds().y, getBounds().width, getBounds().height);
                break;
            case HIT:

                //Normal hits will be translucent white, and larger than falling notes
                g2.setColor(new Color(255,255,255, (int)Math.max(alpha,0)));
                g2.fillRect(getBounds().x-8, getBounds().y-8, getBounds().width+16, getBounds().height+16);
                g2.setFont(Game.game.def);
                g2.drawString(String.format("%+d",pointValue), x+48,y+16);
                break;
            case PERFECT:

                //Perfect hits will be translucent green, and larger than falling notes
                g2.setColor(new Color(100,255,100, (int)Math.max(alpha,0)));
                g2.fillRect(getBounds().x-8, getBounds().y-8, getBounds().width+16, getBounds().height+16);
                g2.setFont(Game.game.def);
                g2.drawString(String.format("%+d",pointValue), x+48,y+16);
                break;
        }
    }

    /**
     * Update notes as they fall. This mostly checks for pauses and modifies timers accordingly.
     */
    void update(){
        switch (state) {
            case FALLING:

                //If newly paused
                if (Game.game.conductor.paused && !wasPaused) {

                    //Record time left for resuming, and stop timer
                    delayLeft = timer.getDelay();
                    timer.stop();
                    wasPaused = true;
                }

                //If newly resumed
                if (!Game.game.conductor.paused && wasPaused) {

                    //Creates new timer with the delay left over from the previous, then sets it back to normal afterwards
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

                //Prevent collisions with other notes
                for (int i = 0; i < Game.game.conductor.notes.size(); i++) {
                    Note n = Game.game.conductor.notes.get(i);
                    if (!this.equals(n) && getBounds().intersects(n.getBounds()) && n.state==NoteState.FALLING) {
                        Game.game.conductor.trashNotes.add(this);
                        if (Game.game.conductor.trashNotes.contains(n)) Game.game.conductor.trashNotes.remove(n);
                    }
                }

                //Destroy notes that leave the screen, and break combo
                if (!new Rectangle(0, 0, Game.game.getWidth(), Game.game.getHeight()).contains(getLocation())) {
                    Game.game.conductor.trashNotes.add(this);
                    Game.game.combo = 0;
                }
                break;
            case HIT:
            case PERFECT:

                //Once hit, stop falling and fade out
                alpha-=0.01f;
                if(timer.isRunning())timer.stop();
                if(alpha<=0){
                    Game.game.conductor.trashNotes.add(this);
                }
                break;
        }
    }
}
