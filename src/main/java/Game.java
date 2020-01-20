import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class Game extends JFrame {

    //Game instance, can be referenced outside of this class easily.
    public static Game game = null;

    //Graphics Panel
    MPanel gPane = null;

    //Window Borders
    Insets insets;

    //Input
    Input input;
    int[] inputs = new int[]{0,0};
    int[] canHit = new int[]{1,1};
    int[] didHit = new int[]{0,0};

    //Score
    int score = 0;
    int combo = 0;
    int comboFactor = 1;

    //Buttons
    ArrayList<MButton> buttons = new ArrayList<MButton>();

    //File Chooser
    MFileChooser fileChooser;

    //Current Music File
    Conductor conductor;

    //Directory for user content
    File userDir;

    {
        String myDocuments = null;

        try {
            Process p =  Runtime.getRuntime().exec("reg query \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v personal");
            p.waitFor();

            InputStream in = p.getInputStream();
            byte[] b = new byte[in.available()];
            in.read(b);
            in.close();

            myDocuments = new String(b);
            myDocuments = myDocuments.split("\\s\\s+")[4];

        } catch(Throwable t) {
            t.printStackTrace();
        }

        userDir = new File(myDocuments+ "\\My Games\\AnotherRhythmGame\\");
        userDir.mkdirs();
    }



    /* Game State, Menu Screen, and Level State enums */
    GameState gameState;
    GameState pGameState;
    enum GameState{
        MENU, LEVEL
    }

    MenuScreen menuScreen;
    MenuScreen pMenuScreen;
    enum MenuScreen{
        MAIN, SETUP, BROWSER, CREDITS
    }

    LevelState levelState;
    LevelState pLevelState;
    enum LevelState{
        MAIN, PAUSE, FINISHED
    }

    /* Fonts */
    Font  h1  = new Font(Font.SERIF, Font.PLAIN,  32);
    Font  h2  = new Font(Font.SERIF, Font.PLAIN,  24);
    Font def = new Font(Font.SANS_SERIF, Font.PLAIN,  14);

    /* Images */
    BufferedImage bgImage;
    {
        try {
            bgImage = ImageIO.read(getClass().getResourceAsStream("bg.png"));
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /* Colors */
    Color transBlack = new Color(0,0,0, 0.5f);
    Color notBlack = new Color(5,5,5);
    Color notWhite = new Color(200,200,200);

    /* Strokes */
    BasicStroke defaultStroke = new BasicStroke(1);
    BasicStroke medStroke = new BasicStroke(3);
    BasicStroke mainStroke = new BasicStroke(10);

    public static void main(String[] args) {
        new Game();
    }

    private Game(){
        init();

        while (true){
            gPane.repaint();
            step();
        }
    }

    void init(){
        game = this;
        setVisible(false);
        setSize(1280,720);
        setLocationRelativeTo(null);
        setTitle("NADRG");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gPane = new MPanel();
        add(gPane);
        fileChooser = new MFileChooser(getWidth()-500,50,400,600);
        conductor = new Conductor();
        input = new Input();
        setGameState(GameState.MENU);
        setVisible(true);
        insets = getInsets();
    }

    void step(){
        input.update();
        switch (gameState){
            case MENU:
                break;
            case LEVEL:
                conductor.update();
                comboFactor= (int)Math.max(Math.log(combo)/Math.log(2), 1);
                break;
        }
    }

    class MPanel extends JPanel{

        {
            setBackground(notBlack);
        }

        @Override
        public void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D) g;
            super.paintComponent(g2);

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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

    void drawMenu(Graphics2D g2){
        g2.drawImage(bgImage,0,0,null);
        for(MButton b: buttons){
            b.draw(g2);
        }
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
            case BROWSER:
                break;
            case CREDITS:
                g2.drawString("Credits:", 50, 50);
                g2.setFont(h2);
                g2.drawString("Silas Bartha - Programming & Game Design", 50, 200);
                g2.drawString("Featuring Music by Matthew Pablo, Zander Noriega, and cynicmusic.",50,250);
                break;
        }
    }

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
                                setMenuScreen(MenuScreen.SETUP);
                            }
                        });
                        buttons.add(new MButton(50, 130, 200, 50) {

                            {
                                setText("Credits");
                            }

                            @Override
                            public void performAction() {
                                setMenuScreen(MenuScreen.CREDITS);
                            }
                        });
                        buttons.add(new MButton(50, 190, 200, 50) {

                            {
                                setText("Exit");
                            }

                            @Override
                            public void performAction() {
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
                                fileChooser.page = Math.min(fileChooser.page+1, fileChooser.buttons.size()/19);
                            }
                        });
                        break;
                    case BROWSER:
                        buttons.add(new MButton(50, 70, 200, 50) {

                            {
                                setText("Back");
                            }

                            @Override
                            public void performAction() {
                                setMenuScreen(pMenuScreen);
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

    void drawLevel(Graphics2D g2){
        conductor.drawSong(g2);
        g2.setColor(Color.BLACK);
        g2.setStroke(mainStroke);
        for(int i = 0; i < 2; i ++){
            g2.drawLine(400+i*100, 0, 400+i*100, 500);
        }
        g2.setFont(h1);
        g2.drawString(conductor.getTitle(),600, 50);
        g2.setStroke(defaultStroke);
        switch (levelState){
            case MAIN:
                g2.drawString("Score: " + String.format("%,d",score),200, 50);
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
        conductor.drawNotes(g2);
        for(MButton b: buttons){
            b.draw(g2);
        }

        g2.setStroke(mainStroke);
        for(int i = 0; i < 2; i ++){
            g2.setColor(new Color(Color.HSBtoRGB(0f, 0f, inputs[i]*canHit[i])));
            g2.drawRect(400+i*100-16, 500-16, 32, 32);
            for(int j = 0; j < conductor.notes.size(); j++) {
                Note n = conductor.notes.get(j);
                Rectangle paddleBounds = new Rectangle(400 + i * 100 - 16, 500-16, 32, 32);
                if (paddleBounds.intersects(n.getBounds())&&inputs[i]==1&&canHit[i]==1&&n.state==Note.NoteState.FALLING) {
                    combo++;
                    if(new Point((int)paddleBounds.getCenterX(), (int)paddleBounds.getCenterY()).distance(n.getCenterX(),n.getCenterY())<=10){
                        n.state = Note.NoteState.PERFECT;
                        score += 15*comboFactor;
                        n.pointValue = 15*comboFactor;
                    } else {
                        n.state = Note.NoteState.HIT;
                        score += 10*comboFactor;
                        n.pointValue = 10*comboFactor;
                    }
                    canHit[i]=0;
                    didHit[i]=1;
                }
            }
        }
    }

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

    public void setMenuScreen(MenuScreen menuScreen) {
        pMenuScreen = this.menuScreen;
        this.menuScreen = menuScreen;
        refreshGUI();
    }

    public void setLevelState(LevelState levelState) {
        pLevelState = this.levelState;
        this.levelState = levelState;
        refreshGUI();
    }

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

    public void togglePause() {
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

    public void exitLevel(){
        setGameState(GameState.MENU);
        setMenuScreen(MenuScreen.SETUP);
        conductor.stop();
    }
}
