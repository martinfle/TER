public class Main {
    public static void main (String[] args) {
        Graph g = new Graph(2);
        g.remplirRoot();
        g.printValue();
        
       g.permutationAleatoire();
        System.out.println();
            g.printValue();
    
        g.emptySudoku(0.5);
        System.out.println();
        g.printValue();
        //g.printAdjList();
        
    }
}   
