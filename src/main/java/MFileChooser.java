import com.mpatric.mp3agic.*;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MFileChooser extends Rectangle{

    public int page;
    public Target target;
    enum Target{
        DEFAULT,USER
    }

    public MFileChooser(int x, int y, int width, int height){
        setBounds(x,y,width,height);
        target = Target.DEFAULT;
        refresh();
    }

    ArrayList<MButton> buttons = new ArrayList<MButton>();
    ArrayList<String> files = new ArrayList<String>();

    void draw(Graphics2D g2){
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(x,y,width,height);
        for(int i = 0; i < Math.min((files.size()-(page*19)),19); i++){
            MButton b = buttons.get(i+(page*19));
            b.draw(g2);
        }
    }

    void refresh(){
        files.clear();
        buttons.clear();
        switch (target){
            case DEFAULT:
                loadDefaultSongs();
                break;
            case USER:
                loadUserSongs();
                break;
        }
    }

    public static ArrayList<String> search(final String pattern, final File folder){

        ArrayList<String> result = new ArrayList<String>();

        for (final File f : folder.listFiles()) {

            if (f.isDirectory()) {
                search(pattern, f);
            }

            if (f.isFile()) {
                if (f.getName().matches(pattern)) {
                    result.add(f.getAbsolutePath());
                }
            }

        }
        return result;
    }

    public Target getTarget() {
        return target;
    }

    public void loadDefaultSongs() {
        files.add("awake.mp3");
        files.add("colossus.mp3");
        files.add("dreamraid.mp3");
        files.add("fight.mp3");
        files.add("heroicdemise.mp3");
        files.add("orbital.mp3");
        files.add("tension.mp3");


        for (int i = 0; i < files.size(); i++) {
            String fname = files.get(i);
                File file = Game.getResourceAsFile(fname);
                makeButton(file, i);
        }
    }

    public void makeButton(File file, int i){
        try {
            Mp3File mp3File = new Mp3File(file.getPath());
            String artist = null;
            String title = null;

            if(mp3File.hasId3v1Tag()) {
                ID3v1 id3v1Tag = mp3File.getId3v1Tag();
                artist = id3v1Tag.getArtist();
                title = id3v1Tag.getTitle();
            }else if(mp3File.hasId3v2Tag()){
                if(mp3File.hasId3v2Tag()) {
                    ID3v2 id3v2Tag = mp3File.getId3v2Tag();
                    artist = id3v2Tag.getArtist();
                    title = id3v2Tag.getTitle();
                }
            }

            if(title == null){
                 title = file.getName();
            }else if(artist!=null){
                title = new String(title + " - " + artist);
            }

            float durationInSeconds = mp3File.getLengthInSeconds();
            int finalI = i;
            String finalTitle = title;
            buttons.add(
                    new MButton(x + 10, y + 10 + (finalI%19) * 30, width - 20, 30) {

                        {
                            setText(finalTitle + " (" + (int) Math.round(durationInSeconds) / 60 + ":" + String.format("%02d",(int) Math.round(durationInSeconds) % 60) + ")");
                            setFnt(Game.game.def);
                        }

                        @Override
                        public void performAction() {
                            if(i/19==page) {
                                Game.game.startGame(file);
                            }
                        }
                    }
            );
        } catch (UnsupportedTagException | InvalidDataException | IOException e) {
            e.printStackTrace();
        }
    }

    void loadUserSongs(){
        files = search(".*\\.mp3", Game.game.userDir);
        for(int i = 0; i < files.size(); i++){
            File file = new File(files.get(i));
            makeButton(file, i);
        }
        if(files.size()>19){
            page = 0;
        }
    }
}

