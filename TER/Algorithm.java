import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Algorithm {
    Graph g;

    public Algorithm (Graph g) {
        this.g = g;
    }

    public void preprocess () {
        int v = -1;
        int k = 0;
        while (true) {
            v = -1;
            for (Map.Entry<Integer, List<Integer>> entry: g.values.entrySet()) {
                if(entry.getValue().size() == 1) {
                    v = entry.getKey();
                    k= entry.getValue().get(0);
                    g.puzzle[v/(g.n*g.n)][v%(g.n*g.n)] = k;
                    break;
                }
            }
            if (v == -1) return;
            System.out.println("v = " + v + " k = " + k);
            g.values.remove(v);
            g.adjList.remove(v);
            for (Map.Entry<Integer, List<Integer>> entry: g.adjList.entrySet()) {
                if (entry.getValue().contains(v)) {
                    int key = entry.getKey();
                    g.values.get(key).remove((Object) k);
                    g.adjList.get(key).remove((Object) v);
                }               
                
            }
            
        }
    }

    public void MMCOL () {
        // Générer p solutions initiales à G.
        ArrayList<HashMap<Integer, Integer>> P = population_Initialisation(20);
        // Enregistrer la solution initiale de P avec le meilleur score.
        int best_fitness = fitness(P.get(0));
        HashMap<Integer, Integer> best_solution = P.get(0);
        for (int i=1; i< P.size(); i++) {
            int fitness = fitness(P.get(i));
            if (fitness > best_fitness) {
                best_fitness = fitness;
                best_solution = P.get(i);
            }
        }
        do {
            // Sélectionner deux solutions de P au hasard.
            int i1 = (int) (Math.random() * P.size());
            int i2 = (int) (Math.random() * P.size());
            while (i1 == i2) {
                i2 = (int) (Math.random() * P.size());
            }
            HashMap<Integer, Integer> s1 = P.get(i1);           
            HashMap<Integer, Integer> s2 = P.get(i2);
            
            // Appliquer l'algorithme MAGX pour faire un mélange des deux solutions.
            HashMap<Integer, Integer> s = MAGX(s1, s2);  
            // Améliorer la solution avec l'algorithme Tabu                    
            s = ITS(s);
            if (fitness(s) < best_fitness) {
                best_solution = s;
                best_fitness = fitness(s);
            }
        } while (best_fitness != 0);
    }
    
    private HashMap<Integer, Integer> ITS (HashMap<Integer, Integer> c, int alpha) {
        HashMap<Integer, Integer> best_color = c;
        int best_fitness = fitness(c);
        do {
            
        } while ()
    }
    public HashMap <Integer, Integer> MAGX (HashMap<Integer, Integer> s1, HashMap<Integer, Integer> s2) {
        int cc = 0;
        ArrayList<Integer> residualCapacity = getResidualCapacity();        
        HashMap<Integer, ArrayList<Integer>> c0 = new HashMap<>();
        HashMap<Integer, ArrayList<Integer>> c1 = new HashMap<>();
            HashMap<Integer, ArrayList<Integer>> c2 = new HashMap<>();
            for (Map.Entry<Integer,Integer> entry: s1.entrySet()) {
                if (c1.get(entry.getValue()) == null) {
                    c1.put(entry.getValue(), new ArrayList<>());
                    c1.get(entry.getValue()).add(entry.getKey());
                } else {
                    c1.get(entry.getValue()).add(entry.getKey());
                }
            }
            for (Map.Entry<Integer,Integer> entry: s2.entrySet()) {
                if (c2.get(entry.getValue()) == null) {
                    c2.put(entry.getValue(), new ArrayList<>());
                    c2.get(entry.getValue()).add(entry.getKey());
                } else {
                    c2.get(entry.getValue()).add(entry.getKey());
                }
            }
        while (cc < g.n*g.n) {
            
            boolean b = false;
            int key = -1;
            int max = 0;
            // Prendre la plus grande classe-couleur de s1 et s2
            for (Map.Entry<Integer, ArrayList<Integer>> entry: c1.entrySet()) {
                if (entry.getValue().size() > max && entry.getValue().size() <= (g.n*g.n - residualCapacity.get(entry.getKey()-1)) && c0.get(entry.getKey()) == null ) {
                    b = false;
                    max = entry.getValue().size();
                    key = entry.getKey();
                }
            }
            for (Map.Entry<Integer, ArrayList<Integer>> entry: c2.entrySet()) {
                if (entry.getValue().size() > max && entry.getValue().size() <= (g.n*g.n - residualCapacity.get(entry.getKey()-1)) && c0.get(entry.getKey()) == null ) {
                    b = true;
                    max = entry.getValue().size();
                    key = entry.getKey();
                }
            }
            if (key > -1) {
            // Ajouter la classe-couleur à c0 et supprimer les éléments de la classe-couleur dans les deux.
                if (b == true) {
                    c0.put(key, c2.get(key));
                    for (int i=0; i< c2.get(key).size(); i++) {
                        for (Map.Entry<Integer, ArrayList<Integer>> entry: c1.entrySet()) {
                            if (entry.getValue().contains(c2.get(key).get(i))) {
                                entry.getValue().remove((Object) c2.get(key).get(i));
                            }
                        }
                    }
                    c2.remove(key);                
                } else {
                    c0.put(key, c1.get(key));
                    for (int i=0; i< c1.get(key).size(); i++) {
                        for (Map.Entry<Integer, ArrayList<Integer>> entry: c2.entrySet()) {
                            if (entry.getValue().contains(c1.get(key).get(i))) {
                                entry.getValue().remove((Object) c1.get(key).get(i));
                            }
                        }
                    }
                    c1.remove(key);                
                }
            }
            cc++;
        }
        // Remplir les classes vides avec les éléments restants communs.
        // Puis supprimer les éléments restant
        for (int i =0; i< g.n*g.n; i++) {
            if (c0.get(i+1) == null) {
                if (c1.get(i+1) != null && c2.get(i+1) != null) {
                    c0.put(i+1, intersection(c1.get(i+1), c2.get(i+1)));
                    for (int j=0; j< c0.get(i+1).size(); j++) {
                        c1.remove(c0.get(i+1).get(j));
                        c2.remove(c0.get(i+1).get(j));
                    }
                }
            }
        }
        // assigner une couleur aléatoire au sommets restants.
        for (Map.Entry <Integer, ArrayList<Integer>> entry: c1.entrySet()) {
            for (int i=0; i< entry.getValue().size(); i++) {
                int val = chooseRandomColor(entry.getValue().get(i));
                if (c0.get(val) == null) c0.put(val, new ArrayList<>());
                c0.get(val).add(entry.getValue().get(i));
            }
        }
        // remettre sous la bonne forme
        HashMap <Integer, Integer> result = new HashMap<>();
        for (Map.Entry <Integer, ArrayList<Integer>> entry: c0.entrySet()) {
            for (int i=0; i< entry.getValue().size(); i++) {
                result.put(entry.getValue().get(i), entry.getKey());
            }
        }        
        return result;
    }

    private int chooseRandomColor (int value) {
        int resultat = (int) (Math.random() * g.values.get(value).size());
        return g.values.get(value).get(resultat);
    }
    private ArrayList<Integer> intersection(ArrayList<Integer> arrayList, ArrayList<Integer> arrayList2) {
        ArrayList<Integer> intersection = new ArrayList<>();
        for (int i =0; i< arrayList.size(); i++) {
            if (arrayList2.contains(arrayList.get(i))) {
                intersection.add(arrayList.get(i));
            }
        }
        return intersection;
    }

    public ArrayList<Integer> getResidualCapacity () {
        ArrayList<Integer> residualCapacity = new ArrayList<>();
        for (int i=0; i< g.n*g.n; i++) {
            residualCapacity.add(0);
        }
        for (int i=0; i< g.n*g.n; i++) {
            for (int j=0; j< g.n*g.n; j++) {
                if (g.puzzle[i][j] != 0) {
                    int val = residualCapacity.get(g.puzzle[i][j]-1) +1;
                    residualCapacity.set(g.puzzle[i][j]-1,val);                    
                }
            }
        }
        return residualCapacity;
    }
    public ArrayList<HashMap<Integer, Integer>> population_Initialisation (int p) {
        ArrayList<HashMap<Integer, Integer>> P = new ArrayList<>();
        for (int i=0; i< p; i++) {
            P.add(new HashMap<>());
            for (int j=0; j < g.values.size(); j++) {
                for (Map.Entry<Integer, List<Integer>> entry: g.values.entrySet()) {
                    int val = chooseRandomColor(entry.getKey());
                    P.get(i).put(entry.getKey(), val);
                }
            }
        }
        return P;
    }

    public int fitness (HashMap<Integer, Integer> solution) {
        int fitness = 0;
        for (Map.Entry<Integer, Integer> entry: solution.entrySet()) {  

                if (g.adjList.get(entry.getKey()) != null) {
                    for (int k = 0; k< g.adjList.get(entry.getKey()).size(); k++) {
                        if (g.adjList.get(entry.getKey()).contains(entry.getValue())) fitness++;
                    }
                }            
        }
        return fitness;
    }


    private void printHashMap (HashMap <Integer, Integer> map) {
        for (Map.Entry <Integer, Integer> entry : map.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());            
        }
    }

    private void printHashMap2 (HashMap <Integer, ArrayList<Integer>> map) {
        for (Map.Entry <Integer, ArrayList<Integer>> entry : map.entrySet()) {
            System.out.print(entry.getKey() + ": ");
            for (int i=0; i< entry.getValue().size();i++) {
                System.out.print(entry.getValue().get(i) + " ");
            }   
            System.out.println();         
        }
    }
}
