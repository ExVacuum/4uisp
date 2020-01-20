import java.io.FileInputStream;
import java.io.InputStream;

public class MinimHelper {

    /**
     * Minim wants a single path for resources, but I don't want that so this method returns what it takes in, meaning you must pass the correct path in
     * @param fileName path to file
     * @return path to file
     */
    public String sketchPath( String fileName ) {
        return fileName;
    }

    //This is required for Minim, not entirely sure how it's used but ¯\_(ツ)_/¯
    public InputStream createInput(String fileName) {
        InputStream is = null;
        try{
            is = new FileInputStream(sketchPath(fileName));
        }
        catch(Exception e){
            System.out.println(e.toString());
        }
        return is;
    }
}
