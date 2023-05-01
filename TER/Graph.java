import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Collections;
import java.util.HashMap;

public class Graph {

    int[][] puzzle;
    HashMap<Integer, List<Integer>> values;
    HashMap<Integer, List<Integer>> adjList;
    final int n;
    static Random rand;

    public Graph (Graph g) {
        this.puzzle = g.puzzle;
        this.adjList = g.adjList;
        this.n = g.n;        
    }

    public Graph (int n) {
        this.n = n;
        if (rand == null) rand = new Random();
        puzzle = new int[n*n][n*n];
        values = new HashMap<>();
        adjList = new HashMap<>();

        for (int i = 0; i< n*n*n*n; i++) {
            adjList.put(i, new ArrayList<>());
            values.put(i, new ArrayList<>());
        }

        for (int i = 0; i< n*n; i++) {
            for (int j = 0; j <n*n; j++) {
                int node = i*n*n+j;
                for (int k = 0; k< n*n; k++) {
                    if (k != j) {
                        int neighbor = i* n*n +k;
                        adjList.get(node).add(neighbor);
                    }
                }
                for (int k=0; k<n*n; k++) {
                    if (k != i ) {
                        int neighbor = k*n*n+j;
                        adjList.get(node).add(neighbor);
                    }
                }

                int boxRow = i / n*n;
                int boxCol = j / n*n;

                for (int k= boxRow; k < boxRow + n; k++) {
                    for (int l = boxCol; l < boxCol+n; l++) {
                        if (k!=i && l!= j) {
                            int neighbor = k*n*n+l;
                            adjList.get(node).add(neighbor);
                        }
                    }
                }
            }

        }
    }

// Remplissage du sudoku "root": un sudoku fonctionnel prix aléatoirement
    public void remplirRoot () {
        int x = 0;
        for (int i = 1; i< n+1; i++) {
            for (int j = 1; j<n+1; j++) {
                for (int k = 1; k< n*n +1; k++) {
                    puzzle[n*(i-1)+j-1][k-1] = x%(n*n) + 1;
                    x += 1;
                }
                x += n;
            }
            x+=1;
        }
    }

    public void printValue () {
        for (int i = 0; i< puzzle.length; i++) {
            for (int j= 0; j< puzzle.length; j++) {
                System.out.print(puzzle[i][j] + " ");
            }
            System.out.println();
        }
    }

    public void printAdjList () {
        for (Map.Entry<Integer, List<Integer>> entry: adjList.entrySet()) {
            System.out.print(entry.getKey() + " : ");
            for (int i = 0; i< entry.getValue().size(); i++) {
                System.out.print(entry.getValue().get(i) + " ");
            }
            System.out.println();
        }
    }

/* Fonctions de permutations */
    public void permutationAleatoire () {
        
        for (int j = 0; j< rand.nextInt(100); j++) {
            
            for (int i = 0; i< rand.nextInt(n*n) + 1; i++) {
                permutationColonneCarré(getPermutation(n));           
            }   
            
            for (int i = 0; i< rand.nextInt(n*n) + 1; i++) {
                permutationLigneCarré(getPermutation(n));           
            } 
            
            for (int i = 0; i< rand.nextInt(n*n) + 1; i++) {
                permutationColonne(getPermutation(n), rand.nextInt(n));           
            }   
            
            for (int i = 0; i< rand.nextInt(n*n) + 1; i++) {
                permutationLigne(getPermutation(n), rand.nextInt(n));           
            } 
            
        }   
    }

    public List<Integer> getPermutation (int n) {
        List<Integer> array  = new ArrayList<>();
        for (int i =1; i< n+1; i++) {
            array.add(i);
        }
        Collections.shuffle(array);
        
        return array;

    }

    public void permutationColonneCarré (List<Integer> permutation) {
        for (int i = 0; i< n; i++) {
            int permCol = permutation.get(i) -1;
            if (i != permCol) {
                for (int j = 0; j< n*n; j++) {
                    for (int k = 0; k< n; k++) {
                        int tmp = puzzle[j][i*n+k];
                        puzzle[j][i*n+k] = puzzle[j][permCol*n+k];
                        puzzle[j][permCol*n+k] = tmp;
                    }
                    
                }
            }
        }
    }

    public void permutationLigneCarré (List<Integer> permutation) {
        for (int i = 0; i< n; i++) {
            int permCol = permutation.get(i) -1;
            if (i != permCol) {
                for (int j = 0; j< n*n; j++) {
                    for (int k = 0; k< n; k++) {
                        int tmp = puzzle[i*n+k][j];
                        puzzle[i*n+k][j] = puzzle[permCol*n+k][j];
                        puzzle[permCol*n+k][j] = tmp;
                    }
                    
                }
            }
        }
    }

    public void permutationColonne (List<Integer> permutation, int col) {
       for (int i = 0; i< n; i++) {
        int permCol = permutation.get(i) -1;
        if (i != permCol) {
            for (int j = 0; j< n*n; j++) {
                int tmp = puzzle[j][n*col+i];
                puzzle[j][n*col + i] = puzzle[j][n*col + permCol];
                puzzle[j][n*col + permCol] = tmp;
            }
        }
       }
    }

    public void permutationLigne (List<Integer> permutation, int col) {
        for (int i = 0; i< n; i++) {
            int permCol = permutation.get(i) -1;
            if (i != permCol) {
                for (int j = 0; j< n*n; j++) {
                    int tmp = puzzle[n*col+i][j];
                    puzzle[n*col+i][j] = puzzle[n*col + permCol][j];
                    puzzle[n*col + permCol][j] = tmp;
                }
            }
        }
    }

    // Fonctions pour créer un sudoku a partir d'une grille remplie

    public void emptySudoku (double epsilon) {
        
        for (int i = 0; i< n*n; i++) {
            for (int j= 0; j< n*n; j++) {
                if (rand.nextDouble() < epsilon) {
                    puzzle[i][j] = 0;
                }
            }
        }
    }

    public void remplirGraph () {
        for (int i = 0; i< n*n; i++) {
            for (int j = 0; j< n*n; j++) {
                if (puzzle[i][j] != 0) values.get(i*n*n+j).add(puzzle[i][j]);
                else {
                    for (int k = 1; k< n*n +1; k++) {
                        values.get(i*n*n+j).add(k);
                    }
                }
            }
        }
    }

    public void printGraph () {
        for (Map.Entry<Integer, List<Integer>> entry: values.entrySet()) {
            System.out.print(entry.getKey() + " : ");
            for (int i = 0; i< entry.getValue().size(); i++) {
                System.out.print(entry.getValue().get(i) + " ");
            }
            System.out.println();
        }
    }

}
