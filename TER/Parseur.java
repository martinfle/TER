import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Parseur {
    public static Graph parse (String filename) {
        try {
            BufferedReader reader = new BufferedReader (new FileReader(filename));
            String line;
            int size = Integer.parseInt(reader.readLine());
            Graph g = new Graph(size);
            int row = 0;
            while ((line = reader.readLine())!= null) {
                String[] values = line.split(" ");
                for (int col = 0; col< size*size; col++) {
                    g.puzzle[row][col] = Integer.parseInt(values[col]);
                }
                row++;
            }
            reader.close();
            return g;
        } catch (IOException e) {
            e.printStackTrace();
        }
     return null;   
    }
}
