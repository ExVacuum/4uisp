import com.mpatric.mp3agic.*;
import ddf.minim.AudioListener;
import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Conductor{

    //Instance for Minim audio processing library, very useful
    private Minim minim;

    /*Minim audio players for foreground (audible/visualizers) and background (note pre-placement).
    Necessary for processing .mp3 audio files.*/
    private AudioPlayer foregroundSong, backgroundSong;

    /*Minim Fourier Transform object,
    used in spectrum visualization.*/
    private FFT fgfft;

    //Minim beat detection objects for foreground (audible/visualizers) and background (note pre-placement).
    private BeatDetect fgbeatDetect, bgbeatDetect;

    /*"Flares" for beat visualization.
    * kflare = kick
    * sflare = snare
    * */
    private double kflare, sflare;

    //Actual file for processing
    private File currentFile;

    //mp3agic Mp3File object for reading id3 tags, and strings for title and artist id3 tags
    private Mp3File currentMp3;
    private String title;
    private String artist;

    /*Background Music Thread
    * This game uses multithreading to allow the player to play while the audio plays in a separate thread.
    * */
    private BGMThread musicThread;

    //Length of song in ms
    private int songsize;

    public boolean paused, canPause, finished;

    //List of notes, and notes to be destroyed
    public ArrayList<Note> notes = new ArrayList<Note>();
    public ArrayList<Note> trashNotes = new ArrayList<Note>();

    /**
     * Set up the conductor
     * - Create all of the Minim objects
     * - Configure beat detection objects
     * - Get song size
     * Start level
     */
    public void setup(){
        minim = new Minim(new MinimHelper());
        foregroundSong = minim.loadFile(currentFile.getPath(),1024);
        backgroundSong = minim.loadFile(currentFile.getPath(),1024);
        fgfft = new FFT(foregroundSong.bufferSize(), foregroundSong.sampleRate());
        fgbeatDetect = new BeatDetect();
        bgbeatDetect = new BeatDetect();

        /* Put beat detectors into frequency energy mode, allowing detection of hats, snares, and kicks.
        * Hats are finicky so I don't use them in this game.
        * Set sensitivity to 400ms, so there will be a delay between detection of beats.
        * */
        fgbeatDetect.detectMode(BeatDetect.FREQ_ENERGY);
        bgbeatDetect.detectMode(BeatDetect.FREQ_ENERGY);
        bgbeatDetect.setSensitivity(400);
        fgbeatDetect.setSensitivity(400);

        /*Beat detection helper objects,
        ensure maximal number of samples are processed in beat detection.*/
        new BeatListener(fgbeatDetect, foregroundSong);
        new BeatListener(bgbeatDetect, backgroundSong);

        songsize = foregroundSong.length();

        beginPlayback();
    }

    /**
     * Sets the file to be used and gets the id3 tags from it
     * @param currentFile target file
     */
    public void setCurrentFile(File currentFile) {
        this.currentFile = currentFile;
        try {
            currentMp3 = new Mp3File(currentFile.getPath());
            if(currentMp3.hasId3v1Tag()){
                ID3v1 id3v1Tag = currentMp3.getId3v1Tag();
                artist = id3v1Tag.getArtist();
                title = id3v1Tag.getTitle();
            }else if(currentMp3.hasId3v2Tag()){
                if(currentMp3.hasId3v2Tag()) {
                    ID3v2 id3v2Tag = currentMp3.getId3v2Tag();
                    artist = id3v2Tag.getArtist();
                    title = id3v2Tag.getTitle();
                }
            }

            //Default title to filename if none found
            if(title == null){
                title = currentFile.getName();
            }else if(artist!=null){

                //Compound artist onto title
                title = (title + " - " + artist);
            }
        }catch (IOException | UnsupportedTagException | InvalidDataException e){
            e.printStackTrace();
        }
    }

    /**
     * Get current file
     * @return current file
     */
    public File getCurrentFile() {
        return currentFile;
    }

    /**
     * Create and start background music thread
     */
    public void beginPlayback(){
        musicThread = new BGMThread();
        musicThread.start();
    }

    /**
     * Toggle playback of song
     */
    public void togglePlayback(){
        paused ^= true;
        if (paused) {
            musicThread.pauseSong();
        } else {
            musicThread.resumeSong();
        }
    }

    /**
     * Draw visualization (Waveform, Progress Bar, Frequency Spectrum Visualization, and Beat Circles)
     * @param g2 Graphics2D object for drawing
     */
    public void drawSong(Graphics2D g2){

        //Make sure everything needed exists
        if(foregroundSong !=null&&backgroundSong!=null&&fgfft!=null&&bgbeatDetect!=null&&fgbeatDetect!=null) {

            //Kick circles
            g2.setColor(new Color(Color.HSBtoRGB(0f, 0.9f, (float) (kflare / 30.0))));
            g2.fillOval(600 - (int) kflare, 100 - (int) kflare, 400 + 2 * (int) kflare, 400 + 2 * (int) kflare);
            g2.fillOval(1050 - (int) kflare, 200 - (int) kflare, 200 + 2 * (int) kflare, 200 + 2 * (int) kflare);

            //Snare circles
            g2.setColor(new Color(Color.HSBtoRGB(0.05f, 1f, (float) (sflare / 45.0))));
            g2.fillOval(600 - (int) sflare, 100 - (int) sflare, 50 + 2 * (int) sflare, 50 + 2 * (int) sflare);
            g2.fillOval(900 - (int) sflare, 500 - (int) sflare, 100 + 2 * (int) sflare, 100 + 2 * (int) sflare);

            //Get amplitude of each band from fft and draw a scaled spectrum visualization
            g2.setStroke(Game.game.defaultStroke);
            for (int i = 0; i < fgfft.specSize(); i++) {
                g2.setColor(new Color(Color.HSBtoRGB(0f, 0f, 0.5f)));
                g2.drawLine((int) (i / (double) fgfft.specSize() * (Game.game.getWidth())), Game.game.getHeight(), (int) (i / (double) fgfft.specSize() * (Game.game.getWidth())), (int) (Game.game.getHeight() - fgfft.getBand(i) * 10));
            }

            //Draw position xx:xx/xx:xx (xxx%)
            g2.setFont(Game.game.def);
            g2.setColor(Color.BLACK);
            g2.drawString(
                    String.format(
                            "%s (%.0f%%)",
                            (int) (getCurrentMp3().getLengthInSeconds() * getProgressInPercent() / 100) / 60 + ":"
                                    + String.format("%02d", (int) (getCurrentMp3().getLengthInSeconds() * getProgressInPercent() / 100) % 60) + " / "
                                    + (int) getCurrentMp3().getLengthInSeconds() / 60 + ":"
                                    + String.format("%02d", (int) (getCurrentMp3().getLengthInSeconds() % 60)
                            ),
                            getProgressInPercent()
                    ),
                    (int) Math.max(((Game.game.getWidth() - Game.game.insets.right - Game.game.insets.left) *getProgressInPercent() / 100) - 125, 10),
                    Game.game.getHeight() - 120
            );

            //Progress bar across screen
            g2.fillRect(0, Game.game.getHeight() - 110, (int) ((Game.game.getWidth() - Game.game.insets.right - Game.game.insets.left) * getProgressInPercent() / 100), 50);

            /* Waveform
            * - Draw left and right waveforms overlapping
            * - Draw each portion of the sample twice in order to fit across the whole screen
             */
            g2.setColor(Color.DARK_GRAY);
            for (int i = 0; i < (int) ((Game.game.getWidth() - Game.game.insets.right - Game.game.insets.left) * getProgressInPercent() / 100) - 1; i++) {
                g2.drawLine(i, (int) (Game.game.getHeight() - 85 + foregroundSong.left.get(i/2) * 25), i + 1, (int) (Game.game.getHeight() - 85 + foregroundSong.left.get(i/2 + 1) * 25));
                g2.drawLine(i, (int) (Game.game.getHeight() - 85 + foregroundSong.right.get(i/2) * 25), i + 1, (int) (Game.game.getHeight() - 85 + foregroundSong.right.get(i/2 + 1) * 25));
            }
        }
    }

    /**
     * Draw notes on screen
     * @param g2 Graphics2D object for drawing
     */
    public void drawNotes(Graphics2D g2){
        for (int i = 0; i< notes.size(); i++){
            Note note = notes.get(i);
            note.draw(g2);
        }
    }

    /**
     * Stop music (unpause first if necessary)
     */
    public void stop(){
        if(paused) togglePlayback();
        musicThread.kill();
    }

    /**
     * Get current mp3 file associated with target file
     * @return current mp3
     */
    public Mp3File getCurrentMp3() {
        return currentMp3;
    }

    /**
     * Get title used in header
     * @return "title - artist"
     */
    public String getTitle() {
        return title;
    }

    /**
     * Update notes, visualization flares, and handle the end of a song.
     */
    public void update(){
        if(musicThread != null) {
            for (int i = 0; i < notes.size(); i++) {
                Note note = notes.get(i);
                note.update();
            }
            notes.removeAll(trashNotes);
            trashNotes.clear();

            if (kflare > 0) {
                kflare -= 0.005;
            }

            if (sflare > 0) {
                sflare -= 0.005;
            }

            //For some reason, the song always ends with a tiny bit left that scales with song length, so a small corrective factor has been implemented.
            if(foregroundSong.length()-musicThread.getProgress()<foregroundSong.length()/500&&!finished){
                finished = true;
                Timer endTimer = new Timer(1000, actionEvent -> Game.game.setLevelState(Game.LevelState.FINISHED));
                endTimer.setRepeats(false);
                endTimer.start();
            }
        }
    }

    /**
     * Get progess of song, in %
     * @return song progress, in percent.
     */
    public double getProgressInPercent(){
       return (musicThread.getProgress()/(double)songsize)*100.0;
    }

    class BGMThread extends Thread {

        @Override
        public void run() {

            //Start background song, and mute it, as it is only needed for note pre-placement.
            backgroundSong.play();
            backgroundSong.mute();

            //In 2.5 seconds, the foreground song will play, and so the game will be able to be paused.
            Timer startTimer = new Timer(2500, actionEvent -> {foregroundSong.play(); canPause=true;});
            startTimer.setRepeats(false);
            startTimer.start();
        }

        /**
         * Pauses the current song, both tracks
         */
        public void pauseSong(){
            backgroundSong.pause();
            foregroundSong.pause();
        }

        /**
         * Resumes the current song, both tracks
         */
        public void resumeSong(){
            backgroundSong.play();
            foregroundSong.play();
        }

        /**
         * Closes the current song, both tracks
         */
        public void kill(){
            foregroundSong.close();
            backgroundSong.close();
        }

        /**
         * Get progress of song
         * @return position of song, in ms
         */
        public int getProgress(){
            return foregroundSong !=null? foregroundSong.position():0;
        }
    }

    /**
     * Beat listener is triggered every time the source gets a new sample, and thus reduces any discrepancies between the two tracks caused by random error
     */
    class BeatListener implements AudioListener
    {
        private BeatDetect beat;
        private AudioPlayer source;

        public BeatListener(BeatDetect beat, AudioPlayer source)
        {
            this.source = source;
            this.source.addListener(this);
            this.beat = beat;
        }

        @Override
        public void samples(float[] samps) {
            detect();
        }

        @Override
        public void samples(float[] sampsL, float[] sampsR) {
            detect();
        }

        /**
         * Detects beats, and performs appropriate actions
         */
        void detect(){

            //Check for beats
            beat.detect(source.mix);

            if (backgroundSong.equals(source)) {

                //Add notes to left bar if a kick is detected
                if (bgbeatDetect.isKick()) {
                    notes.add(new Note(0));
                }

                //Add notes to right bar if a snare is detected
                if (bgbeatDetect.isSnare()) {
                    notes.add(new Note(1));
                }
            }

            if(foregroundSong.equals(source)){

                //Update FFT every sample
                fgfft.forward(foregroundSong.mix);

                //Kick circle flare
                if (fgbeatDetect.isKick()) {
                    kflare = 30;
                }

                //Snare circle flare
                if (fgbeatDetect.isSnare()){
                    sflare = 45;
                }
            }
        }
    }
}
