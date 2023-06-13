import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

public class Algorithm {
    Graph graph;

    public Algorithm (Graph g) {
        graph = g;
    }

    public void preprocess () {
        int v = -1;
        int k = 0;
        while (true) {
            v = -1;
            for (Map.Entry<Integer, List<Integer>> entry: graph.values.entrySet()) {
                if(entry.getValue().size() == 1) {
                    v = entry.getKey();
                    k= entry.getValue().get(0);
                    graph.puzzle[v/(graph.n* graph.n)][v%(graph.n*graph.n)] = k;
                    break;
                }
            }
            if (v == -1) return;
            graph.values.remove(v);
            graph.adjList.remove(v);
            for (Map.Entry<Integer, List<Integer>> entry: graph.adjList.entrySet()) {
                if (entry.getValue().contains(v)) {
                    int key = entry.getKey();
                    graph.values.get(key).remove((Object) k);
                    graph.adjList.get(key).remove((Object) v);
                }               
                
            }
            
        }
    }

    public void MMCOL () {        
        // Générer p solutions initiales à G.
        ArrayList<HashMap<Integer, Integer>> P = population_Initialisation(20);        
        // Enregistrer la solution initiale de P avec le meilleur score.
        int best_fitness = fitness(P.get(0), graph);
        if (best_fitness == -1) return;
        HashMap<Integer, Integer> best_solution = P.get(0);
        for (int i=1; i< P.size(); i++) {
            int fitness = fitness(P.get(i), graph);
            if (fitness == -1) return;
            if (fitness < best_fitness) {
                best_fitness = fitness;
                best_solution = P.get(i);
            }
        }
        if (best_fitness == 0) {
            return;
        }
        //System.out.println("best so far : " + best_solution + " " + best_fitness);
        do {
            if (Thread.currentThread().isInterrupted()) return;
            // Sélectionner deux solutions de P au hasard.
            int i1 = (int) (Math.random() * P.size());
            int i2 = (int) (Math.random() * P.size());
            while (i1 == i2) {
                i2 = (int) (Math.random() * P.size());
            }
            HashMap<Integer, Integer> s1 = P.get(i1);           
            HashMap<Integer, Integer> s2 = P.get(i2);
           // System.out.println("s1 : " + s1);
            //System.out.println("s2 : " +s2);
            // Appliquer l'algorithme MAGX pour faire un mélange des deux solutions.
            HashMap<Integer, Integer> s = MAGX(s1, s2, graph); 
            if (Thread.currentThread().isInterrupted()) return;
            //System.out.println("MAGX : " +s); 
            // Améliorer la solution avec l'algorithme Tabu                    
            s = ITS(s, 100000); 
            if (Thread.currentThread().isInterrupted()) return;
            /* 
            System.out.println("ITS : " + s);
            System.out.println("*********");
            graph.printGraph();
            System.out.println("*********");
            graph.printAdjList();
            */
            int fitness_s = fitness(s, graph);
            if (fitness_s == -1) return;
            if (fitness_s < best_fitness) {
                best_solution = s;
                best_fitness = fitness_s;
                //System.out.println("new fitness : " + best_fitness);
            }
            // Population updating
            P.add(s);
            ArrayList<Double> score = new ArrayList<>();
            for (int i=0; i< P.size(); i++) {
                score.add(score(P, i));
            }
            if (Thread.currentThread().isInterrupted()) return;
            double lowest1 = Integer.MAX_VALUE;
            double lowest2 = Integer.MAX_VALUE;
            int cw = -1;
            int csw = -1;
            for (int i=0; i< score.size(); i++) {
                if (score.get(i) < lowest1) {
                    lowest2 = lowest1;
                    lowest1 = score.get(i);
                    csw = cw;
                    cw = i;
                } else if (i < lowest2 && i != lowest1) {
                    lowest2 = score.get(i);
                    csw = i;
                }
            }
            if (cw == -1) return;
            if (cw == P.size()-1) P.remove(cw);
            else {
                double d = Math.random();
                if (d < 0.8) P.remove(cw);
                else P.remove(csw);
            }
        } while (best_fitness != 0 && !Thread.currentThread().isInterrupted());
        // On remplace le résultat par la solution trouvé
        for (Map.Entry<Integer, Integer> entry: best_solution.entrySet()) {
            graph.puzzle[entry.getKey()/(graph.n*graph.n)][entry.getKey()%(graph.n*graph.n)] = entry.getValue();
        }
    }
    
    private int distance (HashMap<Integer, Integer> c1, HashMap<Integer, Integer> c2) {
        int distance = 0;
        for (Map.Entry<Integer, Integer> entry : c1.entrySet()) {
            if (entry.getValue() != c2.get(entry.getKey())) distance++;
        }
        return distance;
    }

    private Integer diversity (ArrayList<HashMap<Integer, Integer>> p, int i) {
        Integer div = Integer.MAX_VALUE;
        for (int j=0; j< p.size(); j++) {
            if (j != i ) {
                int tmp = distance(p.get(j), p.get(i));
                if (div > tmp) div = tmp;
            }
        }

        return div;
    } 

    private double score (ArrayList<HashMap<Integer, Integer>> p, int i) {
        return fitness(p.get(i), graph) + Math.exp((0.08 * graph.n * graph.n)/ diversity(p, i));
    }

    private HashMap<Integer, Integer> ITS (HashMap<Integer, Integer> c, int alpha) {
        HashMap<Integer, Integer> best_color = c;
        int best_fitness = fitness(c, graph);
        if (best_fitness == -1) return null;
        int maxLSIters = 100;  
        //HashMap<Integer, Integer> c1 = new HashMap<>();      
        do {
            
            // Faire une recherche Tabou avec la solution c.
            c = TS(c, alpha, graph);
            if(Thread.currentThread().isInterrupted()) return null;

            int new_fitness = fitness(c, graph);
            if (new_fitness == -1) return null;
            if (new_fitness < best_fitness) {
                best_fitness = new_fitness;
                best_color = new HashMap<>(c);
            }
            // si c1 n'est pas une coloration légale, faire une perturbation.
            if (new_fitness != 0) {
                c = perturbation_procedure(c);
                if (Thread.currentThread().isInterrupted()) return null;
            } else {
                return best_color;
            }
            maxLSIters --;
        } while (maxLSIters > 0 && best_fitness > 0);
        return best_color;
    }

    // Fonction pour partir d'un minimum local
    private HashMap<Integer, Integer> perturbation_procedure(HashMap<Integer, Integer> c1) {
        // Ne garder que les sommets qui posent problème
        ArrayList<Integer> X = new ArrayList<>();
        
        for (Map.Entry<Integer, Integer> entry: c1.entrySet()) {
                if (graph.adjList.get(entry.getKey()) != null) {
                    for (int k = 0; k< graph.adjList.get(entry.getKey()).size(); k++) {
                        if (graph.adjList.get(entry.getKey()).contains(entry.getValue()) && !X.contains(entry.getValue())) X.add(entry.getKey());
                    }
                }            
        }
        
        // Créer un sous graph en enlevant la moitié des graphes qui posent problèmes
        Graph g = new Graph(graph);
        Collections.shuffle(X);
        int midSize = X.size()/2;
        for (int i=0; i< midSize; i++) {
            X.remove(0);
        }
        HashMap<Integer, Integer> c = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : c1.entrySet()) {
            c.put(entry.getKey(), entry.getValue());
        }
        for (Integer i : X) {
            g.values.remove(i);
            g.adjList.remove(i);
            c.remove(i);
        }

        for (Map.Entry<Integer, List<Integer>> entry : g.adjList.entrySet()) {
            if (entry.getValue().removeAll(X));
        }
       
        // Faire le TS avec ce nouveau graph
        HashMap<Integer, Integer> amelioration = TS(c, 100, g);
        if (Thread.currentThread().isInterrupted()) return null;
        // Fusionner G' et G
        for (Map.Entry<Integer, Integer> entry : amelioration.entrySet()) {
            c1.put(entry.getKey(), entry.getValue());
        }
        
        return c1;
    }

    // Tabou Search
    // TODO: Ajouter le fait de déplacer un sommet en conflit
    private HashMap<Integer, Integer> TS(HashMap<Integer, Integer> c, int alpha, Graph g) {
        
        //System.out.println("Valeurs a améliorer : " + c);
        HashMap<Map.Entry<Integer, Integer>, Integer> tabuList = new HashMap<>();     
        HashMap<Integer, Integer> sBest = new HashMap<>(c);
        HashMap<Integer, Integer> bestCandidate = new HashMap<>(c);
        ArrayList<Integer> sVoisins = conflicting_vertex(bestCandidate, g); 
        int best_f = fitness(c,g);        
        //System.out.println("On commence le tabou :" + best_f);
        boolean b = false;
        int maxTabuSize =(int) (0.6 * best_f + Math.random()*10);
        // boucle principale
        for (int i = 0; i< alpha; i ++) { 
            c = new HashMap<>(bestCandidate);         
            if (b) {
                sVoisins = conflicting_vertex(bestCandidate, g);
                b = false;
            }
            Map.Entry<Integer, Integer> bestMove = null;
            int best_fc = Integer.MAX_VALUE;
            // Recherche meilleur mouvement non tabou
            for (Integer entry : sVoisins) {
                Map.Entry<Integer, Integer> sommet = new AbstractMap.SimpleEntry<>(entry, c.get(entry));
                int currentColor = sommet.getValue();
               
                // Parcours les couleurs possibles
                for (int j = 0; j< g.values.get(sommet.getKey()).size(); j++) {
                    if (g.values.get(sommet.getKey()).get(j) != currentColor) {                        
                        c.put(sommet.getKey(),g.values.get(sommet.getKey()).get(j));                        
                        int fitness = fitness(c,g);   
                        if (fitness == -1) return null;                     
                        if (fitness < best_fc && !isTabu(sommet, tabuList)) {                            
                            bestCandidate = new HashMap<>(c);
                            b = true;
                            bestMove = sommet;  
                            best_fc = fitness;
                            if (best_fc == 0) return bestCandidate;
                            //System.out.println("meilleur : " + best_c + " fitness : "+ best_f + " sommet changé : "+ sommet.getKey());    
                        }
                    }
                   
                }                
                // On réinitialise la couleur du sommet
                c.put(sommet.getKey(), currentColor);
                
            }
            if (best_fc < best_f) {                
                sBest = new HashMap<>(bestCandidate);
                best_f = best_fc;
                //System.out.println("new record: " + best_f);
            }

            if (bestMove != null) {
                // On ajoute le meilleur mouvement
                bestMove.setValue(bestCandidate.get(bestMove.getKey()));
                tabuList.put(bestMove, maxTabuSize);                
            }
            HashMap<Map.Entry<Integer, Integer>, Integer> cpTAbu = new HashMap<>(tabuList);
            for (Map.Entry<Map.Entry<Integer, Integer>, Integer> entry : cpTAbu.entrySet()) {
                if (entry.getValue() > 0) tabuList.put(entry.getKey(), entry.getValue()-1);
                else {
                    tabuList.remove(entry.getKey());                
                }
            }
            
        }

        return sBest;
    }
    public boolean isTabu (Map.Entry<Integer, Integer> solution, HashMap<Map.Entry<Integer, Integer>, Integer> tabuList) {
        for (Map.Entry<Integer, Integer> entry : tabuList.keySet()) {
            if (solution.getKey() == entry.getKey() && solution.getValue() == entry.getValue()) return true;
        }
        return false;
    }
    public HashMap <Integer, Integer> MAGX (HashMap<Integer, Integer> s1, HashMap<Integer, Integer> s2, Graph g) {
        int cc = 0;
        //System.out.println("s1: " + s1);
        //System.out.println("s2: " + s2);
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
                            for (int j=0; j< entry.getValue().size(); j++) {
                                if (entry.getValue().get(j) == c2.get(key).get(i)) entry.getValue().remove(j);
                            }
                        }
                    }
                    c2.remove(key);                
                } else {
                    c0.put(key, c1.get(key));
                    for (int i=0; i< c1.get(key).size(); i++) {
                        for (Map.Entry<Integer, ArrayList<Integer>> entry: c2.entrySet()) {
                            for (int j=0; j< entry.getValue().size(); j++) {
                                if (entry.getValue().get(j) == c1.get(key).get(i)) entry.getValue().remove(j);
                            }
                        }
                    }
                    c1.remove(key);                
                }
            }
            cc++;
            //System.out.println("c0 : " + c0);
        }
        // Remplir les classes vides avec les éléments restants communs.
        // Puis supprimer les éléments restant
        for (int i =0; i< g.n*g.n; i++) {
            if (c0.get(i+1) == null) {
                if (c1.get(i+1) != null && c2.get(i+1) != null) {
                    c0.put(i+1, intersection(c1.get(i+1), c2.get(i+1)));                    
                }
            }
        }
        //System.out.println("Remplissage des cases vides " + c0);
        // assigner une couleur aléatoire au sommets restants.
        /*
        for (Map.Entry <Integer, ArrayList<Integer>> entry: c1.entrySet()) {
            for (int i=0; i< entry.getValue().size(); i++) {
                int val = chooseRandomColor(entry.getValue().get(i), g);
                if (c0.get(val) == null) c0.put(val, new ArrayList<>());
                c0.get(val).add(entry.getValue().get(i));
            }
        }
        
        System.out.println("On fait les reste : " + c0);
        */
        // remettre sous la bonne forme
        HashMap <Integer, Integer> result = new HashMap<>();
        for (Map.Entry <Integer, ArrayList<Integer>> entry: c0.entrySet()) {
            for (int i=0; i< entry.getValue().size(); i++) {
                result.put(entry.getValue().get(i), entry.getKey());
            }
        }

        for (Map.Entry <Integer, ArrayList<Integer>> entry: c1.entrySet()) {            
            for (int i=0; i< entry.getValue().size(); i++) {
                if (result.get(entry.getValue().get(i)) == null) {
                    int val = chooseRandomColor(entry.getValue().get(i), g);                    
                    result.put(entry.getValue().get(i), val);
                }
            }
        } 
        for (Map.Entry <Integer, ArrayList<Integer>> entry: c2.entrySet()) {            
            for (int i=0; i< entry.getValue().size(); i++) {
                if (result.get(entry.getValue().get(i)) == null) {
                    int val = chooseRandomColor(entry.getValue().get(i), g);                    
                    result.put(entry.getValue().get(i), val);
                }
            }
        }
        
        return result;
    }

    private int chooseRandomColor (int value, Graph g) {
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
        for (int i=0; i< graph.n*graph.n; i++) {
            residualCapacity.add(0);
        }
        for (int i=0; i< graph.n*graph.n; i++) {
            for (int j=0; j< graph.n*graph.n; j++) {
                if (graph.puzzle[i][j] != 0) {
                    int val = residualCapacity.get(graph.puzzle[i][j]-1) +1;
                    residualCapacity.set(graph.puzzle[i][j]-1,val);                    
                }
            }
        }
        return residualCapacity;
    }
    public ArrayList<HashMap<Integer, Integer>> population_Initialisation (int p) {
        ArrayList<HashMap<Integer, Integer>> P = new ArrayList<>();
        for (int i=0; i< p; i++) {
            P.add(new HashMap<>());
            for (int j=0; j < graph.values.size(); j++) {
                for (Map.Entry<Integer, List<Integer>> entry: graph.values.entrySet()) {
                    int val = chooseRandomColor(entry.getKey(), graph);
                    P.get(i).put(entry.getKey(), val);
                }
            }
        }
        return P;
    }

    public int fitness (HashMap<Integer, Integer> solution, Graph g) {
        int fitness = 0;
        for (Map.Entry<Integer, List<Integer>> entry: g.adjList.entrySet()) {
                int sommet = entry.getKey();
                int couleur = solution.get(sommet);
                if (Thread.currentThread().isInterrupted()) return -1;
                for (int voisin : entry.getValue()) {
                        if (solution.get(voisin) == couleur) fitness ++;
                    }
                           
        }
        return fitness;
    }

    public ArrayList<Integer> conflicting_vertex (HashMap<Integer, Integer> solution, Graph g) {
        ArrayList<Integer> resultat = new ArrayList<>();
        for (Map.Entry<Integer, List<Integer>> entry: g.adjList.entrySet()) {
            int sommet = entry.getKey();
            int couleur = solution.get(sommet);
            for (int voisin : entry.getValue()) {
                if (solution.get(voisin) == couleur && !resultat.contains(sommet)) resultat.add(sommet); 
            }
        }
        return resultat;
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
