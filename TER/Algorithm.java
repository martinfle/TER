import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;


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
            for (Map.Entry<Integer, HashSet<Integer>> entry: graph.values.entrySet()) {
                if(entry.getValue().size() == 1) {
                    v = entry.getKey();
                    k= entry.getValue().iterator().next();
                    graph.puzzle[v/(graph.n* graph.n)][v%(graph.n*graph.n)] = k;
                    break;
                }
            }
            if (v == -1) return;
            graph.values.remove(v);
            graph.adjList.remove(v);
            for (Map.Entry<Integer, HashSet<Integer>> entry: graph.adjList.entrySet()) {
                if (entry.getValue().contains(v)) {
                    int key = entry.getKey();
                    graph.values.get(key).remove((Object) k);
                    graph.adjList.get(key).remove((Object) v);
                }               
                
            }
            
        }
    }

    public int MMCOL (int p, int maxGenerations, int maxIter, int alpha) {        
        // Générer p solutions initiales à G.
        ArrayList<HashMap<Integer, Integer>> P = population_Initialisation(p);            
        // Enregistrer la solution initiale de P avec le meilleur score.
        int best_fitness = fitness(P.get(0), graph);        
        HashMap<Integer, Integer> best_solution = P.get(0);
        for (int i=1; i< P.size(); i++) {
            int fitness = fitness(P.get(i), graph);
            
            if (fitness < best_fitness) {
                best_fitness = fitness;
                best_solution = P.get(i);
            }
        }
        if (best_fitness == 0) {
            return 100;
        }
        
        do {           
            
            // Sélectionner deux solutions de P au hasard.
            int i1 = (int) (Math.random() * P.size());
            int i2 = (int) (Math.random() * P.size());
            while (i1 == i2) {
                i2 = (int) (Math.random() * P.size());
            }
            //System.out.println("i1 : " + i1 + " i2 : " + i2);
            HashMap<Integer, Integer> s1 = P.get(i1);           
            HashMap<Integer, Integer> s2 = P.get(i2);
           
            // Appliquer l'algorithme MAGX pour faire un mélange des deux solutions.
            HashMap<Integer, Integer> s = MAGX(s1, s2, graph);
            
             
            // Améliorer la solution avec l'algorithme Tabu                    
            s = ITS(s, alpha, maxIter); 

            int fitness_s = fitness(s, graph);            
            if (fitness_s < best_fitness) {
                best_solution = s;
                best_fitness = fitness_s;                
            }
            // Population updating
            P.add(s);
            ArrayList<Double> score = new ArrayList<>();
            for (int i=0; i< P.size(); i++) {
                score.add(score(P, i));
            }    
                    
            double lowest1 = Integer.MIN_VALUE;
            double lowest2 = Integer.MIN_VALUE;
            int cw = -1;
            int csw = -1;
            for (int i=0; i< score.size(); i++) {
                if (score.get(i) > lowest1) {
                    lowest2 = lowest1;
                    lowest1 = score.get(i);
                    csw = cw;
                    cw = i;
                } else if (i > lowest2 && i != lowest1) {
                    lowest2 = score.get(i);
                    csw = i;
                }
            }
            if (cw == -1) return -1;
            if (cw != P.size()-1) P.remove(cw);
            else {
                double d = Math.random();
                if (d < 0.8) P.remove(cw);
                else P.remove(csw);
            }
            maxGenerations --;            
        } while (best_fitness != 0 && maxGenerations >= 0);
        
        
        // On remplace le résultat par la solution trouvé
        for (Map.Entry<Integer, Integer> entry: best_solution.entrySet()) {
            graph.puzzle[entry.getKey()/(graph.n*graph.n)][entry.getKey()%(graph.n*graph.n)] = entry.getValue();
        }
        if (best_fitness == 0) return maxGenerations+1;
        return maxGenerations+1;
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

    private HashMap<Integer, Integer> ITS (HashMap<Integer, Integer> c, int alpha, int maxLSIters) {
        HashMap<Integer, Integer> best_color = c;
        int best_fitness = fitness(c, graph);       
        
             
        do {
            
            // Faire une recherche Tabou avec la solution c.
            c = TS(c, alpha, graph);


            int new_fitness = fitness(c, graph);
            
            if (new_fitness < best_fitness) {
                best_fitness = new_fitness;
                best_color = new HashMap<>(c);
            }
            // si c1 n'est pas une coloration légale, faire une perturbation.
            if (new_fitness != 0) {
                c = perturbation_procedure(c, alpha);

            } else {
                return best_color;
            }
            maxLSIters --;
        } while (maxLSIters > 0 && best_fitness > 0);
        return best_color;
    }

    // Fonction pour partir d'un minimum local
    private HashMap<Integer, Integer> perturbation_procedure(HashMap<Integer, Integer> c1, int alpha) {
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
        HashMap<Integer, Integer> c = new HashMap<>(c1);        
        for (Integer i : X) {
            g.values.remove(i);
            g.adjList.remove(i);
            for (Map.Entry<Integer, HashSet<Integer>> entry : g.adjList.entrySet()) {
                if (entry.getValue().contains(i)) entry.getValue().remove(i);
            }
            c.remove(i);
        }

        for (Map.Entry<Integer, HashSet<Integer>> entry : g.adjList.entrySet()) {
            if (entry.getValue().removeAll(X));
        }
       


        // Faire le TS avec ce nouveau graph'
        HashMap<Integer, Integer> amelioration = TS(c, alpha, g);
        

        // Fusionner G' et G
        for (Map.Entry<Integer, Integer> entry : amelioration.entrySet()) {
            c1.put(entry.getKey(), entry.getValue());
        }
        
        return c1;
    }

    // Tabou Search    
    private HashMap<Integer, Integer> TS(HashMap<Integer, Integer> c, int alpha, Graph g) {        
        //System.out.println("Valeurs a améliorer : " + c);
        HashMap<Map.Entry<Integer, Integer>, Integer> tabuList = new HashMap<>();     
        HashMap<Integer, Integer> sBest = new HashMap<>(c);
        HashMap<Integer, Integer> bestCandidate = new HashMap<>(c);
        ArrayList<Integer> sVoisins = conflicting_vertex(bestCandidate, g); 

        Map.Entry<Integer, Integer> sommet = null;
        int best_f = fitness(c,g);  
            
        
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
            int f_sommet = Integer.MAX_VALUE;
            // Recherche meilleur mouvement non tabou
            for (Integer entry : sVoisins) {
                sommet = new AbstractMap.SimpleEntry<>(entry, c.get(entry));
                int currentColor = sommet.getValue();
                
                // Parcours les couleurs possibles

                for (int j : g.values.get(sommet.getKey())) {
                    if (j != currentColor) {                        
                        c.put(sommet.getKey(),j);                        
                        int fitness = fitnessSommet(c, g, sommet.getKey());
                                            
                        if (fitness < f_sommet && !isTabu(sommet, tabuList)) {                            
                            bestCandidate = new HashMap<>(c);
                            b = true;
                            bestMove = sommet;
                            f_sommet = fitness;
                               

                        }
                    }
                   
                }  
                            
                // On réinitialise la couleur du sommet
                c.put(sommet.getKey(), currentColor);
                
            }

            if (b) { 
                best_fc = fitness(bestCandidate, g);                              
                if (best_fc < best_f) {                
                    sBest = new HashMap<>(bestCandidate);
                    best_f = best_fc;
                    if (best_f == 0) return sBest;
                }

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
        int couleur = 0;
        Iterator<Integer> it = g.values.get(value).iterator();
        for (int i=0; i< resultat; i++) {
            it.next();
        }
        return it.next();
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
                for (Map.Entry<Integer, HashSet<Integer>> entry: graph.values.entrySet()) {
                    int val = chooseRandomColor(entry.getKey(), graph);
                    P.get(i).put(entry.getKey(), val);
                }
            }
        }
        return P;
    }

    public int fitness (HashMap<Integer, Integer> solution, Graph g)  {

        int fitness = 0;        
        for (Map.Entry<Integer, HashSet<Integer>> entry: g.adjList.entrySet()) {
                int sommet = entry.getKey();
                int couleur = solution.get(sommet);              

                for (int voisin : entry.getValue()) {
                        if (solution.get(voisin) == couleur) {
                            fitness ++;
                            break;
                        }
                        
                    }   
                                
        }        
        return fitness;
    }

    public int fitnessSommet (HashMap<Integer, Integer> solution, Graph g, int sommet) {
        int fitness = 0;       
        int couleur = solution.get(sommet);                
        for (int voisin : g.adjList.get(sommet)) {
            if (solution.get(voisin) == couleur) fitness ++;
        }
        return fitness;

    }

    public ArrayList<Integer> conflicting_vertex (HashMap<Integer, Integer> solution, Graph g) {
        ArrayList<Integer> resultat = new ArrayList<>();
        for (Map.Entry<Integer, HashSet<Integer>> entry: g.adjList.entrySet()) {
            int sommet = entry.getKey();
            int couleur = solution.get(sommet);
            for (int voisin : entry.getValue()) {
                if (solution.get(voisin) == couleur && !resultat.contains(sommet)) resultat.add(sommet); 
            }
        }
        return resultat;
    }   

    
}
