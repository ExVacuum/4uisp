import com.mpatric.mp3agic.*;
import ddf.minim.AudioListener;
import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;
import ddf.minim.analysis.FourierTransform;
import ddf.minim.analysis.WindowFunction;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Conductor{
    private Minim minim;
    private AudioPlayer foregroundSong, backgroundSong;
    private BeatListener fgBeatListener, bgBeatListener;
    private FFT fgfft, bgfft;
    private BeatDetect fgbeatDetect, bgbeatDetect;
    private double kflare, sflare;
    private boolean[] ranges;
    private boolean[] canDetectFG, canDetectBG;
    private File currentFile;
    private Mp3File currentMp3;
    private String title;
    private String artist;
    private BGMThread musicThread;
    private int songsize;
    public boolean paused, finished;
    public ArrayList<Note> notes = new ArrayList<Note>();
    public ArrayList<Note> trashNotes = new ArrayList<Note>();

    public void setup(){
        finished = false;
        minim = new Minim(new MinimHelper());
        foregroundSong = minim.loadFile(currentFile.getPath(),1024);
        backgroundSong = minim.loadFile(currentFile.getPath(),1024);
        fgfft = new FFT(foregroundSong.bufferSize(), foregroundSong.sampleRate());
        bgfft = new FFT(backgroundSong.bufferSize(), backgroundSong.sampleRate());
        fgbeatDetect = new BeatDetect();
        bgbeatDetect = new BeatDetect();
        fgbeatDetect.detectMode(BeatDetect.FREQ_ENERGY);
        bgbeatDetect.detectMode(BeatDetect.FREQ_ENERGY);
        fgBeatListener = new BeatListener(fgbeatDetect, foregroundSong);
        bgBeatListener = new BeatListener(bgbeatDetect, backgroundSong);
        bgbeatDetect.setSensitivity(400);
        fgbeatDetect.setSensitivity(400);
        songsize = foregroundSong.length();
        canDetectFG = canDetectBG = new boolean[]{true,true};
        beginPlayback();
    }

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

            if(title == null){
                title = currentFile.getName();
            }else if(artist!=null){
                title = (title + " - " + artist);
            }
        }catch (IOException | UnsupportedTagException | InvalidDataException e){
            e.printStackTrace();
        }
    }

    public File getCurrentFile() {
        return currentFile;
    }

    public void beginPlayback(){
        musicThread = new BGMThread();
        musicThread.setDaemon(true);
        musicThread.start();
    }

    public void togglePlayback(){
        paused^=true;
        if(paused){
            musicThread.pauseSong();
        }else{
            musicThread.resumeSong();
        }
    }

    public void drawSong(Graphics2D g2){
        if(foregroundSong !=null&&backgroundSong!=null&&fgfft!=null&&bgbeatDetect!=null&&fgbeatDetect!=null) {
            g2.setStroke(Game.game.defaultStroke);

            g2.setColor(new Color(Color.HSBtoRGB(0f, 0.9f, (float) (kflare / 30.0))));
            g2.fillOval(600 - (int) kflare, 100 - (int) kflare, 400 + 2 * (int) kflare, 400 + 2 * (int) kflare);
            g2.fillOval(1050 - (int) kflare, 200 - (int) kflare, 200 + 2 * (int) kflare, 200 + 2 * (int) kflare);
            g2.setColor(new Color(Color.HSBtoRGB(0.05f, 1f, (float) (sflare / 45.0))));
            g2.fillOval(600 - (int) sflare, 100 - (int) sflare, 50 + 2 * (int) sflare, 50 + 2 * (int) sflare);
            g2.fillOval(900 - (int) sflare, 500 - (int) sflare, 100 + 2 * (int) sflare, 100 + 2 * (int) sflare);
            for (int i = 0; i < fgfft.specSize(); i++) {
                g2.setColor(new Color(Color.HSBtoRGB(0f, 0f, 0.5f)));
                g2.drawLine((int) (i / (double) fgfft.specSize() * (Game.game.getWidth())), Game.game.getHeight(), (int) (i / (double) fgfft.specSize() * (Game.game.getWidth())), (int) (Game.game.getHeight() - fgfft.getBand(i) * 10));
            }

            g2.setFont(Game.game.def);
            g2.setColor(Color.BLACK);
            g2.drawString(
                    String.format(
                            "%s (%.0f%%)",
                            (int) (getCurrentMp3().getLengthInSeconds() * Conductor.this.getProgress() / 100) / 60 + ":"
                                    + String.format("%02d", (int) (getCurrentMp3().getLengthInSeconds() * Conductor.this.getProgress() / 100) % 60) + " / "
                                    + (int) getCurrentMp3().getLengthInSeconds() / 60 + ":"
                                    + String.format("%02d", (int) (getCurrentMp3().getLengthInSeconds() % 60)
                            ),
                            Conductor.this.getProgress()
                    ),
                    (int) Math.max(((Game.game.getWidth() - Game.game.insets.right - Game.game.insets.left) * Conductor.this.getProgress() / 100) - 125, 10),
                    Game.game.getHeight() - 120
            );
            g2.fillRect(0, Game.game.getHeight() - 110, (int) ((Game.game.getWidth() - Game.game.insets.right - Game.game.insets.left) * Conductor.this.getProgress() / 100), 50);

            g2.setColor(Color.DARK_GRAY);
            for (int i = 0; i < (int) ((Game.game.getWidth() - Game.game.insets.right - Game.game.insets.left) * Conductor.this.getProgress() / 100) - 1; i++) {
                g2.drawLine(i, (int) (Game.game.getHeight() - 85 + foregroundSong.left.get(i/2) * 25), i + 1, (int) (Game.game.getHeight() - 85 + foregroundSong.left.get(i/2 + 1) * 25));
                g2.drawLine(i, (int) (Game.game.getHeight() - 85 + foregroundSong.right.get(i/2) * 25), i + 1, (int) (Game.game.getHeight() - 85 + foregroundSong.right.get(i/2 + 1) * 25));
            }
        }
    }

    public void drawNotes(Graphics2D g2){
        for (int i = 0; i< notes.size(); i++){
            Note note = notes.get(i);
            note.draw(g2);
        }
    }

    public void stop(){
        if(paused) togglePlayback();
        musicThread.kill();
    }

    public Mp3File getCurrentMp3() {
        return currentMp3;
    }

    public String getTitle() {
        return title;
    }

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

            if(foregroundSong.length()-musicThread.getProgress()<foregroundSong.length()/500&&!finished){
                finished = true;
                Timer endTimer = new Timer(1000, actionEvent -> Game.game.setLevelState(Game.LevelState.FINISHED));
                endTimer.setRepeats(false);
                endTimer.start();
            }
        }
    }

    public double getProgress(){
       return (musicThread.getProgress()/(double)songsize)*100.0;
    }

    class BGMThread extends Thread {

        @Override
        public void run() {
            backgroundSong.play();
            backgroundSong.mute();
            Timer startTimer = new Timer(2500, actionEvent -> foregroundSong.play());
            startTimer.setRepeats(false);
            startTimer.start();
        }

        public void pauseSong(){
            backgroundSong.pause();
            foregroundSong.pause();
        }

        public void resumeSong(){
            backgroundSong.play();
            foregroundSong.play();
        }

        public void kill(){
            foregroundSong.close();
        }

        public int getProgress(){
            return foregroundSong !=null? foregroundSong.position()+1:0;
        }
    }

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

        void detect(){
            beat.detect(source.mix);
            if (backgroundSong.equals(source)) {
                if (bgbeatDetect.isKick()) {
                    notes.add(new Note(0));
                }

                if (bgbeatDetect.isSnare()) {
                    notes.add(new Note(1));
                }
            }

            if(foregroundSong.equals(source)){

                fgfft.forward(foregroundSong.mix);

                if (fgbeatDetect.isKick()) {
                    kflare = 30;
                }

                if (fgbeatDetect.isSnare()){
                    sflare = 45;
                }
            }
        }
    }
}
