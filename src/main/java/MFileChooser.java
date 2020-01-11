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

public class MFileChooser extends Rectangle{

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
    ArrayList<String> files;

    void draw(Graphics2D g2){
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(x,y,width,height);
        for(int i = 0; i < files.size(); i++){
            MButton b = buttons.get(i);
            b.draw(g2);
        }
    }

    void refresh(){
        File dir = null;
        switch (target){
            case DEFAULT:
                dir = Game.game.defaultDir;
                break;
            case USER:
                dir = Game.game.userDir;
                break;
        }
        files = search(".*\\.wav", dir);
        buttons.clear();
        for(int i = 0; i < files.size(); i++){
            try {
                File file = new File(files.get(i));
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
                AudioFormat format = audioInputStream.getFormat();
                long audioFileLength = file.length();
                int frameSize = format.getFrameSize();
                float frameRate = format.getFrameRate();
                float durationInSeconds = (audioFileLength / (frameSize * frameRate));
                int finalI = i;
                buttons.add(
                        new MButton(x + 10, y + 10 + finalI * 30, width - 20, 30) {

                            {
                                setText(file.getName() + " ("+(int)Math.round(durationInSeconds)/60+":"+(int)Math.round(durationInSeconds)%60+ ")");
                                setFnt(Game.game.def);
                            }

                            @Override
                            public void performAction(){
                                Game.game.startGame(file);
                            }
                        }
                );
            }catch (UnsupportedAudioFileException | IOException e){
                e.printStackTrace();
            }
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
}

