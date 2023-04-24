import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main (String[] args) {
        //writeSudokuIntoFile(3);
        Parseur p = new Parseur();
        Graph g = p.parse("TER/Sudoku/3_1");
        g.printValue();
        g.printAdjList();
        g.remplirGraph();
        g.printGraph();
    }

    public static void writeSudokuIntoFile (int size) {
        Graph g = new Graph(size);
        g.remplirRoot();
        for (int i = 1; i<10; i++) {
            try {
                FileWriter writer = new FileWriter("TER/Sudoku/" + size +"_"+String.valueOf((int) (i)));
                g.permutationAleatoire();

                Graph g2 = new Graph(g);
                g2.emptySudoku((double)i /10);
                
                writer.write(size + "\n");
                for (int row = 0; row < size*size; row++) {
                    for (int col = 0; col < size*size; col++) {
                        writer.write(g2.puzzle[row][col] + " ");
                    }
                    writer.write("\n");
                }
                writer.close();
                System.out.println("fini");
            } catch (IOException e) {
                System.out.println("Error writing sudoku to file ");
                e.printStackTrace();
            }
        }
    }
}   
