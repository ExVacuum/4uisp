import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class Game extends JFrame {

    //Game instance, can be referenced outside of this class easily.
    public static Game game = null;

    //Graphics panel
    MPanel gPane = null;

    //Window insets
    Insets insets;

    //Input handler
    Input input;

    //Score
    int score = 0;
    int combo = 0;
    int comboFactor = 1;

    //List for buttons
    ArrayList<MButton> buttons = new ArrayList<MButton>();

    //File chooser
    MFileChooser fileChooser;

    //Music handler
    Conductor conductor;

    //Directory for user content
    File userDir;

    //Set user file directory to Documents\My Games\AnotherRhythmGame
    {
        String myDocuments = null;

        try {
            //Query registry for Documents folder
            Process p =  Runtime.getRuntime().exec("reg query \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v personal");
            p.waitFor();

            InputStream in = p.getInputStream();
            byte[] b = new byte[in.available()];
            in.read(b);
            in.close();

            myDocuments = new String(b);
            myDocuments = myDocuments.split("\\s\\s+")[4];

        } catch(IOException | InterruptedException e) {
            e.printStackTrace();
        }

        //Append \My Games\AnotherRhythmGame\ and make directory.
        userDir = new File(myDocuments+ "\\My Games\\AnotherRhythmGame\\");
        userDir.mkdirs();
    }

    /**
    * Game State, Menu Screen, and Level State enums
    * Current and previous instances of each
    */
    GameState gameState;
    GameState pGameState;
    enum GameState{
        MENU, LEVEL
    }

    MenuScreen menuScreen;
    MenuScreen pMenuScreen;
    enum MenuScreen{
        MAIN, SETUP, CREDITS
    }

    LevelState levelState;
    LevelState pLevelState;
    enum LevelState{
        MAIN, PAUSE, FINISHED
    }

    // Fonts
    Font  h1  = new Font(Font.SERIF, Font.PLAIN,  32);
    Font  h2  = new Font(Font.SERIF, Font.PLAIN,  24);
    Font def = new Font(Font.SANS_SERIF, Font.PLAIN,  14);

    // Images
    BufferedImage bgImage;
    {
        try {
            bgImage = ImageIO.read(getClass().getResourceAsStream("bg.png"));
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    // Colors
    Color transBlack = new Color(0,0,0, 0.5f);
    Color notBlack = new Color(5,5,5);
    Color notWhite = new Color(200,200,200);

    // Strokes
    BasicStroke defaultStroke = new BasicStroke(1);
    BasicStroke medStroke = new BasicStroke(3);
    BasicStroke heavyStroke = new BasicStroke(10);

    /**
    * Main
     */
    public static void main(String[] args) {
        new Game();
    }

    /*
    * Constructor, initializes and runs game.
    */
    private Game(){
        init();

        while (true){
            gPane.repaint();
            step();
        }
    }

    /**
    * Initialize window and important objects
    */
    void init(){

        //Set static instance to this
        game = this;

        //Set window properties
        setVisible(false);
        setSize(1280,720);
        setLocationRelativeTo(null);
        setTitle("Another Rhythm Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add custom JPanel
        gPane = new MPanel();
        add(gPane);

        //Initialize file chooser
        fileChooser = new MFileChooser(getWidth()-500,50,400,600);

        //Initialize music controller
        conductor = new Conductor();

        //Initialize input controller
        input = new Input();

        //Set the game state to the main menu
        setGameState(GameState.MENU);

        //Show window, then grab its insets
        setVisible(true);
        insets = getInsets();
    }

    /**
    * Main game step
    */
    void step(){

        //Update input (calculate mouse location)
        input.update();

        //If currently in a level, update music-related game objects and combo scoring factor.
        if(gameState==GameState.LEVEL){
            conductor.update();
            comboFactor = (int)Math.max(Math.log(combo)/Math.log(2), 1);
            for(int i = 0; i < 2; i ++) {
                for (int j = 0; j < conductor.notes.size(); j++) {
                    Note n = conductor.notes.get(j);
                    Rectangle paddleBounds = new Rectangle(400 + i * 100 - 16, 500 - 16, 32, 32);
                    if (paddleBounds.intersects(n.getBounds()) && input.inputs[i] == 1 && input.canHit[i] == 1 && n.state == Note.NoteState.FALLING) {
                        combo++;
                        if (new Point((int) paddleBounds.getCenterX(), (int) paddleBounds.getCenterY()).distance(n.getCenterX(), n.getCenterY()) <= 10) {
                            n.state = Note.NoteState.PERFECT;
                            score += 15 * comboFactor;
                            n.pointValue = 15 * comboFactor;
                        } else {
                            n.state = Note.NoteState.HIT;
                            score += 10 * comboFactor;
                            n.pointValue = 10 * comboFactor;
                        }
                        input.canHit[i] = 0;
                        input.didHit[i] = 1;
                    }
                }
            }
        }
    }

    /**
    * Custom graphics panel class
    */
    class MPanel extends JPanel{

        //By default, the background of this panel should be slightly lighter than black.
        {
            setBackground(notBlack);
        }

        @Override
        public void paintComponent(Graphics g){

            //Cast graphics to Graphics2D
            Graphics2D g2 = (Graphics2D) g;
            super.paintComponent(g2);

            //Antialias
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            //Draw game elements to screen depending on the game state
            switch (gameState){
                case MENU:
                    drawMenu(g2);
                    break;
                case LEVEL:
                    drawLevel(g2);
                    break;
            }
        }
    }

    /**
    * Handles drawing of everything in the Gamestate.MENU portion of the game
    * @param g2 the Graphics2D object for drawing
    */
    void drawMenu(Graphics2D g2){

        //Regardless of the current screen draw all of the active GUI buttons and the background image
        g2.drawImage(bgImage,0,0,null);
        for(MButton b: buttons){
            b.draw(g2);
        }

        //Draw extra info depending on the menu screen
        g2.setFont(h1);
        g2.setColor(Color.WHITE);
        switch (menuScreen){
            case MAIN:
                g2.drawString("Another Rhythm Game", 50, 50);
                break;
            case SETUP:
                g2.drawString("Game Setup", 50, 50);
                g2.setFont(h2);
                g2.drawString("Song List: ", 800, 40);
                fileChooser.draw(g2);
                break;
            case CREDITS:
                g2.drawString("Credits:", 50, 50);
                g2.setFont(h2);
                g2.drawString("Silas Bartha - Programming & Game Design", 50, 200);
                g2.drawString("Featuring Music by Matthew Pablo, Zander Noriega, and cynicmusic.",50,250);
                break;
        }
    }

    /**
     * Clears all buttons from the current list, and puts new buttons in depending on the game state/screen
     */
    void refreshGUI(){
        buttons.clear();
        switch (gameState) {
            case MENU:
                gPane.setBackground(notBlack);
                switch (menuScreen) {
                    case MAIN:
                        buttons.add(new MButton(50, 70, 200, 50) {

                            {
                                setText("Begin");
                            }

                            @Override
                            public void performAction() {

                                //Enter game setup
                                setMenuScreen(MenuScreen.SETUP);
                            }
                        });
                        buttons.add(new MButton(50, 130, 200, 50) {

                            {
                                setText("Credits");
                            }

                            @Override
                            public void performAction() {

                                //View credits
                                setMenuScreen(MenuScreen.CREDITS);
                            }
                        });
                        buttons.add(new MButton(50, 190, 200, 50) {

                            {
                                setText("Exit");
                            }

                            @Override
                            public void performAction() {

                                //Exit game
                                System.exit(0);
                            }
                        });
                        break;
                    case SETUP:
                        buttons.add(new MButton(50, 70, 200, 50) {

                            {
                                setText("Type: " + fileChooser.target.toString().toLowerCase());
                            }

                            @Override
                            public void performAction() {

                                //Reset file chooser, and toggle between user and default songs
                                fileChooser.page = 0;
                                if (fileChooser.target == MFileChooser.Target.DEFAULT) {
                                    fileChooser.target = MFileChooser.Target.USER;
                                    setText("Type: " + fileChooser.target.toString().toLowerCase());
                                    fileChooser.refresh();
                                    return;
                                }
                                fileChooser.target = MFileChooser.Target.DEFAULT;
                                setText("Type: " + fileChooser.target.toString().toLowerCase());
                                fileChooser.refresh();
                                return;
                            }
                        });
                        buttons.add(new MButton(50, 130, 200, 50) {

                            {
                                setText("Back");
                            }

                            @Override
                            public void performAction() {

                                //Return to the previous menu screen
                                setMenuScreen(pMenuScreen);
                            }
                        });
                        buttons.add(new MButton(1182, 50, 32, 32) {

                            {
                                setText("<");
                                setFnt(def);
                            }

                            @Override
                            public void performAction() {

                                //Scroll back a page
                                fileChooser.page = Math.max(fileChooser.page-1, 0);
                            }
                        });
                        buttons.add(new MButton(1182, 84, 32, 32) {

                            {
                                setText(">");
                                setFnt(def);
                            }

                            @Override
                            public void performAction() {

                                //Scroll ahead a page
                                fileChooser.page = Math.min(fileChooser.page+1, fileChooser.buttons.size()/19);
                            }
                        });
                        break;
                    case CREDITS:
                        buttons.add(new MButton(50, 70, 200, 50) {

                            {
                                setText("Back");
                            }

                            @Override
                            public void performAction() {

                                //Return to previous menu screen
                                setMenuScreen(pMenuScreen);
                            }
                        });
                        break;
                }
                break;
            case LEVEL:
                gPane.setBackground(notWhite);
                switch (levelState) {
                    case MAIN:
                        buttons.add(new MButton(10, 10, 150, 50) {

                            {
                                setText("Pause");
                            }

                            @Override
                            public void performAction() {
                                togglePause();
                            }
                        });
                        break;
                    case PAUSE:
                        buttons.add(new MButton(10, 10, 150, 50) {

                            {
                                setText("Resume");
                            }

                            @Override
                            public void performAction() {
                                togglePause();
                            }
                        });
                        buttons.add(new MButton(10, 70, 150, 50) {

                            {
                                setText("Quit");
                            }

                            @Override
                            public void performAction() {
                                exitLevel();
                            }
                        });
                        break;
                    case FINISHED:
                        buttons.add(new MButton(getWidth() / 2 -75-insets.right+insets.left, getHeight() / 2, 150, 50) {

                            {
                                setText("Replay");
                            }

                            @Override
                            public void performAction() {

                                //Reset level, play again
                                startGame(conductor.getCurrentFile());
                            }
                        });
                        buttons.add(new MButton(getWidth() / 2 -75-insets.right+insets.left, getHeight() / 2 + 75, 150, 50) {

                            {
                                setText("Quit");
                            }

                            @Override
                            public void performAction() {
                                exitLevel();
                            }
                        });
                        break;
                }
                break;
        }
    }

    /**
     * Handles drawing of game elements during the GameState.LEVEL segment of the game
     * @param g2 Graphics2D to draw with
     */
    void drawLevel(Graphics2D g2){

        //Draw background visualizers behind bars
        conductor.drawSong(g2);

        //Draw bars
        g2.setColor(Color.BLACK);
        g2.setStroke(heavyStroke);
        for(int i = 0; i < 2; i ++){
            g2.drawLine(400+i*100, 0, 400+i*100, 500);
        }

        //Draw all active notes on top of bars
        conductor.drawNotes(g2);

        //Draw title, along with extra information depending on the level state
        g2.setColor(Color.BLACK);
        g2.setFont(h1);
        g2.drawString(conductor.getTitle(),600, 50);
        g2.setStroke(defaultStroke);
        switch (levelState){
            case MAIN:
                g2.drawString(String.format("Score: %,d",score),200, 50);
                if(combo>1) g2.drawString(String.format("Combo: %,d (x%d)",combo, comboFactor),200, 100);
                break;
            case PAUSE:
                g2.setColor(transBlack);
                g2.fillRect(0,0,getWidth(),getHeight());
                break;
            case FINISHED:
                g2.setColor(transBlack);
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(Color.WHITE);
                g2.drawString("Score: " + String.format("%,d",score),getWidth()/2-75-insets.right+insets.left, getHeight()/2-10);
                break;
        }

        //Draw buttons
        for(MButton b: buttons){
            b.draw(g2);
        }

        //Draw "player"
        g2.setStroke(heavyStroke);
        for(int i = 0; i < 2; i ++){
            g2.setColor(new Color(Color.HSBtoRGB(0f, 0f, input.inputs[i]*Math.max(input.canHit[i],input.didHit[i]))));
            g2.drawRect(400+i*100-16, 500-16, 32, 32);
        }
    }

    /**
     * Sets the game's state
     * @param gameState Desired GameState
     */
    public void setGameState(GameState gameState) {
        pGameState = this.gameState;
        this.gameState = gameState;
        switch (gameState){
            case MENU:
                menuScreen = MenuScreen.MAIN;
                break;
            case LEVEL:
                levelState = LevelState.MAIN;
                break;
        }
        refreshGUI();
    }

    /**
     * Sets the screen while in the MENU GameState
     * @param menuScreen Desired MenuScreen
     */
    public void setMenuScreen(MenuScreen menuScreen) {
        pMenuScreen = this.menuScreen;
        this.menuScreen = menuScreen;
        refreshGUI();
    }

    /**
     * Sets the screen while in the LEVEL GameState
     * @param levelState Desired LevelState
     */
    public void setLevelState(LevelState levelState) {
        pLevelState = this.levelState;
        this.levelState = levelState;
        refreshGUI();
    }

    /**
     * Resets scoring variables, swaps the game state to LEVEL, sets up conductor and starts level
     * @param file
     */
    public void startGame(File file){
        score = 0;
        combo = 0;
        comboFactor = 1;
        setGameState(GameState.LEVEL);
        conductor = new Conductor();
        conductor.setCurrentFile(file);
        conductor.setup();
        conductor.beginPlayback();
    }

    /**
     * Utility function for getting a file from the resource folder, necessary to make a functional JAR
     * @param resourcePath path to resource
     * @return File from resources folder
     */
    public static File getResourceAsFile(String resourcePath) {
        try {
            InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(resourcePath);
            if (in == null) {
                return null;
            }

            File tempFile = File.createTempFile(String.valueOf(in.hashCode()), ".tmp");
            tempFile.deleteOnExit();

            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                //copy stream
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Toggle level between pause and play
     */
    public void togglePause() {

        //Don't pause before song starts
        if(conductor.canPause) {
            switch (levelState) {
                case MAIN:
                    setLevelState(LevelState.PAUSE);
                    break;
                case PAUSE:
                    setLevelState(LevelState.MAIN);
                    break;
            }
            conductor.togglePlayback();
        }
    }

    /**
     * Return to MENU state SETUP screen
     */
    public void exitLevel(){
        setGameState(GameState.MENU);
        setMenuScreen(MenuScreen.SETUP);
        conductor.stop();
    }
}
