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

    //Buttons
    ArrayList<MButton> buttons = new ArrayList<MButton>();

    //File Chooser
    MFileChooser fileChooser;

    //Current Music File
    File currentFile;

    //Directory for default content
    File defaultDir;
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

        defaultDir = new File(myDocuments+ "\\My Games\\NADRG\\default");
        defaultDir.mkdirs();

        try{

            File afile =new File(getClass().getClassLoader().getResource("2OCT-Above-The-Ashes.wav").getPath());
            File bfile =new File(defaultDir.getPath()+"\\2OCT-Above-The-Ashes.wav");

            if(!bfile.isFile()) {

                InputStream inStream = new FileInputStream(afile);
                OutputStream outStream = new FileOutputStream(bfile);

                byte[] buffer = new byte[1024];

                int length;
                //copy the file content in bytes
                while ((length = inStream.read(buffer)) > 0) {

                    outStream.write(buffer, 0, length);

                }

                inStream.close();
                outStream.close();
            }
                afile = new File(getClass().getClassLoader().getResource("2OCT-Those-Moves.wav").getPath());
                bfile = new File(defaultDir.getPath() + "\\2OCT-Those-Moves.wav");

            if(!bfile.isFile()){

                InputStream inStream = new FileInputStream(afile);
                OutputStream outStream = new FileOutputStream(bfile);

                byte[] buffer = new byte[1024];

                int length;
                //copy the file content in bytes
                while ((length = inStream.read(buffer)) > 0) {

                    outStream.write(buffer, 0, length);

                }

                inStream.close();
                outStream.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

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

        userDir = new File(myDocuments+ "\\My Games\\NADRG\\user");
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
        MAIN, SETUP, BROWSER, OPTIONS
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
        setVisible(true);
        insets = getInsets();
        setGameState(GameState.MENU);
        fileChooser = new MFileChooser(getWidth()-500,50,400,600);
        input = new Input();
    }

    void step(){
        input.update();
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
                g2.drawString("NQN RDANRYH FLOG OSWQHH TMFK", 50, 50);
                g2.setColor(Color.RED);
                g2.fillRect((int)input.mouseLocation.getX(), (int)input.mouseLocation.getY(),10,10);
                break;
            case SETUP:
                g2.drawString("G4M3 S37UP!!1!", 50, 50);
                fileChooser.draw(g2);
                break;
            case BROWSER:
                break;
            case OPTIONS:
                g2.drawString("VLNDOFN RN TDA MYOKTSY YTNNKX", 50, 50);
                break;
        }
    }

    void refreshGUI(){
        buttons.clear();
        switch (gameState){
            case MENU:
                gPane.setBackground(notBlack);
                switch (menuScreen){
                    case MAIN:
                        buttons.add(new MButton(50,70, 200, 50){

                            {
                                setText("BEGIN");
                            }

                            @Override
                            public void performAction(){
                                setMenuScreen(MenuScreen.SETUP);
                            }
                        });
                        buttons.add(new MButton(50,130, 200, 50){

                            {
                                setText("PREFERENCES");
                            }

                            @Override
                            public void performAction(){
                                setMenuScreen(MenuScreen.OPTIONS);
                            }
                        });
                        buttons.add(new MButton(50,190, 200, 50){

                            {
                                setText("EXIT");
                            }

                            @Override
                            public void performAction(){
                                System.exit(0);
                            }
                        });
                        break;
                    case SETUP:
                        buttons.add(new MButton(50,70, 200, 50){

                            {
                                setText("TYPE: " + fileChooser.target.toString());
                            }

                            @Override
                            public void performAction(){
                                if(fileChooser.target==MFileChooser.Target.DEFAULT){
                                    fileChooser.target = MFileChooser.Target.USER;
                                    setText("TYPE: " + fileChooser.target.toString());
                                    fileChooser.refresh();
                                    return;
                                }
                                fileChooser.target = MFileChooser.Target.DEFAULT;
                                setText("TYPE: " + fileChooser.target.toString());
                                fileChooser.refresh();
                                return;
                            }
                        });
                        buttons.add(new MButton(50,130, 200, 50){

                            {
                                setText("BACK");
                            }

                            @Override
                            public void performAction(){
                                setMenuScreen(pMenuScreen);
                            }
                        });
                        break;
                    case BROWSER:
                        buttons.add(new MButton(50,70, 200, 50){

                        {
                            setText("BACK");
                        }

                        @Override
                        public void performAction(){
                            setMenuScreen(pMenuScreen);
                        }
                    });
                        break;
                    case OPTIONS:
                        buttons.add(new MButton(50,70, 200, 50){

                            {
                                setText("BACK");
                            }

                            @Override
                            public void performAction(){
                                setMenuScreen(pMenuScreen);
                            }
                        });
                        break;
                }
                break;
            case LEVEL:
                gPane.setBackground(notWhite);
                switch (levelState){
                    case MAIN:

                        break;
                    case PAUSE:
                        break;
                    case FINISHED:
                        break;
                }
                break;
        }
    }

    void drawLevel(Graphics2D g2){
        g2.setColor(Color.BLACK);
        g2.setStroke(mainStroke);
        for(int i = 0; i < 4; i ++){
            g2.drawLine(200+i*100, 0, 200+i*100, getHeight());
        }
        g2.setFont(h1);
        g2.drawString(currentFile.getName(),600, 50);
        switch (levelState){
            case MAIN:
                break;
            case PAUSE:
                break;
            case FINISHED:
                break;
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
        setGameState(GameState.LEVEL);
        setLevelState(LevelState.MAIN);
        currentFile = file;
        refreshGUI();
    }
}
