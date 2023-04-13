import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Collections;

public class Graph {

    int[][] puzzle;
    List<Integer>[] adjList;
    final int n;

    public Graph (int n) {
        this.n = n;
        puzzle = new int[n*n][n*n];
        adjList = new ArrayList[n*n*n*n];
        

        for (int i = 0; i< n*n*n*n; i++) {
            adjList[i] = new ArrayList<>();
        }

        for (int i = 0; i< n*n; i++) {
            for (int j = 0; j <n*n; j++) {
                int node = i*n*n+j;
                for (int k = 0; k< n*n; k++) {
                    if (k != j) {
                        int neighbor = i* n*n +k;
                        adjList[node].add(neighbor);
                    }
                }
                for (int k=0; k<n*n; k++) {
                    if (k != i ) {
                        int neighbor = k*n*n+j;
                        adjList[node].add(neighbor);
                    }
                }

                int boxRow = i / n*n;
                int boxCol = j / n*n;

                for (int k= boxRow; k < boxRow + n; k++) {
                    for (int l = boxCol; l < boxCol+n; l++) {
                        if (k!=i && l!= j) {
                            int neighbor = k*n*n+l;
                            adjList[node].add(neighbor);
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
        for (int i = 0; i< adjList.length; i++) {
            System.out.print(i + " : ");
            for (int j = 0; j< adjList[i].size(); j++) {
                System.out.print(adjList[i].get(j) + " ");
            }
            System.out.println();
        }
    }

/* Fonctions de permutations */
    public void permutationAleatoire () {
        Random random = new Random();
        for (int j = 0; j< random.nextInt(100); j++) {
            for (int i = 0; i< random.nextInt(n*n) + 1; i++) {
                permutationColonneCarré(getPermutation(n));           
            }   
            
            for (int i = 0; i< random.nextInt(n*n) + 1; i++) {
                permutationLigneCarré(getPermutation(n));           
            } 
            for (int i = 0; i< random.nextInt(n*n) + 1; i++) {
                permutationColonne(getPermutation(n), random.nextInt(n));           
            }   
            
            for (int i = 0; i< random.nextInt(n*n) + 1; i++) {
                permutationLigne(getPermutation(n), random.nextInt(n));           
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
                int tmp = puzzle[j][i*col];
                puzzle[j][i*col] = puzzle[j][permCol*col];
                puzzle[j][permCol*col] = tmp;
            }
        }
       }
    }

    public void permutationLigne (List<Integer> permutation, int col) {
        for (int i = 0; i< n; i++) {
            int permCol = permutation.get(i) -1;
            if (i != permCol) {
                for (int j = 0; j< n*n; j++) {
                    int tmp = puzzle[i*col][j];
                    puzzle[i*col][j] = puzzle[permCol*col][j];
                    puzzle[permCol*col][j] = tmp;
                }
            }
        }
    }

    // Fonctions pour créer un sudoku a partir d'une grille remplie

    public void emptySudoku (double epsilon) {
        Random random = new Random();
        for (int i = 0; i< n*n; i++) {
            for (int j= 0; j< n*n; j++) {
                if (random.nextDouble() < epsilon) {
                    puzzle[i][j] = 0;
                }
            }
        }
    }

}
