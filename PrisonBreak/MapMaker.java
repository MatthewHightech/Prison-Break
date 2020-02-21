import java.io.*;

public class MapMaker {
	private int level;
	private char [][] tileMap = new char [32][40];
	private String [] fileContents;
	
	/*
	 * LEGEND
	 *
	 * 0 = empty tile
	 * 1 = tile
	 * p = player
	 * d = door
	 * c = cop
	 * l = cop's left boundary
	 * r = cop's right boundary
	 * k = key
	 */
	
	//places tiles and entities based on each levels txt file
	MapMaker(int level) {
		this.level = level;
        fileContents = getFileContents("maps/map" + level + ".txt"); // for some reason there is no ".txt"
        for(int i = 0; i < fileContents.length; i ++) { // file must be exactly 32 lines long
        	tileMap[i] = fileContents[i].toCharArray();
        } // for
    } // MapMaker
	
	public char[][] getTileMap() {
		return tileMap;
	} // getTileMap
	
    //  reads fileName and returns the contents as String array
    //  with each line of the file as an element of the array
    public static String [] getFileContents(String fileName){
    	
        String [] contents = null;
        int length = 0;
        try {
        	
            // input
            String folderName = "/subFolder/"; // if the file is contained in the same folder as the .class file, make this equal to the empty string
            String resource = fileName;

			// this is the path within the jar file
			InputStream input = MapMaker.class.getResourceAsStream(folderName + resource);
			if (input == null) {
				// this is how we load file within editor (eg eclipse)
				input = MapMaker.class.getClassLoader().getResourceAsStream(resource);
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(input));	
            
            in.mark(Short.MAX_VALUE);  // see api

            // count number of lines in file
            while (in.readLine() != null) {
            	length++;
            } // while

            in.reset(); // rewind the reader to the start of file
            contents = new String[length]; // give size to contents array

            // read in contents of file and print to screen
            for (int i = 0; i < length; i++) {
            	contents[i] = in.readLine();
            } // for
            in.close();
        } catch (Exception e) {
            System.out.println("File Input Error: " + fileName);
        } // catch

        return contents;

     } // getFileContents
} // MapMaker
