import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static Algorithm a;
    public static long time;
    public static void main (String[] args) {
        
        //writeSudokuIntoFile(9);

      // /*
        Parseur p = new Parseur();
        Graph g = p.parse("TER/Sudoku/3_8_4");
        //Graph g = new Graph(3);
        //g.remplirRoot();
        //g.permutationAleatoire();
        //g.emptySudoku(0.1);
        
        Algorithm a = new Algorithm(g);
        a.graph.printValue();
        System.out.println("*********");
        a.graph.printAdjList();
        System.out.println("*********");
        a.graph.remplirGraph();
        a.graph.printGraph();
        System.out.println("*********");
        a.preprocess();
        a.graph.printValue();
        System.out.println("*********");
        a.graph.printAdjList();
        System.out.println("*********");
        a.graph.printGraph();
        System.out.println("*********");
        long start = System.currentTimeMillis();
        a.MMCOL();
        System.out.println((System.currentTimeMillis()-start));
        a.graph.printValue();
        System.out.println("*********");
       //*/ 
     /*         
    for (int k = 3; k< 4; k++) {  
        for (int i = 3; i< 9; i++) {
            double temps = 0;
            int réussite = 0;
            for (int j=0; j< 100; j++) {
                
                Graph g = Parseur.parse("TER/Sudoku/"+Integer.toString(k)+"_" +Integer.toString(i)+"_"+Integer.toString(j));
                a = new Algorithm(g);
                a.graph.remplirGraph();
                long start = System.currentTimeMillis();
                Thread programmThread = new Thread(new ProgramRunnable());
                programmThread.start();
                try {
                    programmThread.join(60000);
                    if (programmThread.isAlive()) {
                        programmThread.interrupt();                                     

                    }

                    else {                       
                        temps += time;
                        réussite++;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
            }
            System.out.println("Sudoku de type " + Integer.toString(k) + " " + Integer.toString(i) + " temps moyen :" + Double.toString((double) (temps/(réussite*1000)))  + " réussite : " + Integer.toString(réussite)+ "/100"); 
        }
    } 
    */
    }
    static class ProgramRunnable extends Thread {
        public void run () {
            
                long start = System.currentTimeMillis();
                a.preprocess();
                a.MMCOL();
                time = System.currentTimeMillis() - start;
            
           
        }
    }
    
    public static void writeSudokuIntoFile (int size) {
        Graph g = new Graph(size);
        g.remplirRoot();
        for (int j=0; j<100; j++) {
            for (int i = 3; i<9; i++) {
                try {
                    FileWriter writer = new FileWriter("TER/Sudoku/" + size +"_"+String.valueOf((int) (i)) + "_" + String.valueOf((int) (j)));
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
}   
