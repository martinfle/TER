import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Main {
    public static Algorithm a;
    public static long time;
    public static void main (String[] args) {
        
        //writeSudokuIntoFile(9);


    
/*
        Parseur p = new Parseur();

        Graph g = p.parse("TER/Sudoku/4_7_0");

        //Graph g = new Graph(3);
        //g.remplirRoot();
        //g.permutationAleatoire();
        //g.emptySudoku(0.1);
        
        Algorithm a = new Algorithm(g);       
        
        a.graph.remplirGraph();
        
        a.preprocess(); 
        a.graph.countCarac();       
        //a.graph.countCarac();
        
        long start = System.currentTimeMillis();
        a.MMCOL();
        System.out.println((System.currentTimeMillis()-start));
        a.graph.printValue();
        System.out.println("*********");


       */
              


    for (int k =3; k< 4; k++) {  
        for (int i = 3; i< 9; i++) {
            double temps = 0;
            int réussite = 0;
            int nbGénération = 0;            
            for (int j=0; j< 100; j++) {
                
                Graph g = Parseur.parse("TER/Sudoku/"+Integer.toString(k)+"_" +Integer.toString(i)+"_"+Integer.toString(j));
                a = new Algorithm(g);
                a.graph.remplirGraph();
                long start = System.currentTimeMillis();
                ///*              
                a.preprocess();                
                int result = a.MMCOL(20,100,10,100); 
                long end = System.currentTimeMillis() - start;
                if (result > 0) {
                    réussite ++;
                    temps += end;
                    nbGénération += result;
                }               
                
               
            }            
            
            System.out.println("Sudoku de type " + Integer.toString(k) + " " + Integer.toString(i) + " temps moyen :" + Double.toString((double) (temps/(réussite*1000)))  + " réussite : " + Integer.toString(réussite)+ "/100" + " nbGénération moyen : " + Double.toString((double) (nbGénération/(réussite)))); 
            

        }
        
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
