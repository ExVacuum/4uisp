import com.mpatric.mp3agic.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MFileChooser extends Rectangle{

    //Page of songs, necessary to avoid overflow with user songs
    public int page;

    //Target directory
    public Target target;
    enum Target{
        DEFAULT,USER
    }

    /**
     * Create new file chooser with defined size
     * @param x X coordinate
     * @param y Y coordinate
     * @param width width
     * @param height height
     */
    public MFileChooser(int x, int y, int width, int height){
        setBounds(x,y,width,height);
        target = Target.DEFAULT;
        refresh();
    }

    //Lists of buttons and files
    ArrayList<MButton> buttons = new ArrayList<MButton>();
    ArrayList<String> files = new ArrayList<String>();

    /**
     * Draw file chooser
     * @param g2 Graphics2D object for drawing
     */
    void draw(Graphics2D g2){

        //Background
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(x,y,width,height);

        //Draw page of results
        for(int i = 0; i < Math.min((files.size()-(page*19)),19); i++){
            MButton b = buttons.get(i+(page*19));
            b.draw(g2);
        }
    }

    /**
     * Refreshes list of songs, called when target directory is changed
     */
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

    /**
     * Recursive search method that returns all files in a folder that match a pattern.
     * @param pattern pattern to search for, e.g. ".*\.mp3"
     * @param folder folder to search
     * @return list of matching files
     */
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

    /**
     * Loads the default songs from the resource folder
     */
    public void loadDefaultSongs() {

        //Add default songs to list
        files.add("awake.mp3");
        files.add("colossus.mp3");
        files.add("dreamraid.mp3");
        files.add("fight.mp3");
        files.add("heroicdemise.mp3");
        files.add("orbital.mp3");
        files.add("tension.mp3");

        //Retrieve all files in list, and make buttons for them.
        for (int i = 0; i < files.size(); i++) {
            String fname = files.get(i);
                File file = Game.getResourceAsFile(fname);
                makeButton(file, i);
        }
    }

    /**
     * Create a button that will start the game with a target file when pressed
     * @param file song to play when pressed
     * @param i number in list
     */
    public void makeButton(File file, int i){
        try {

            //mp3agic mp3 for id3 tags
            Mp3File mp3File = new Mp3File(file.getPath());
            String artist = null;
            String title = null;

            //Get artist and title
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

            //Default to filename if no title found
            if(title == null){
                 title = file.getName();
            }else if(artist!=null){

                //Combine artist and title into one string for button text
                title = new String(title + " - " + artist);
            }

            //Get song length
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

    /**
     * Search for mp3 files in user directory, and load them in.
     */
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

