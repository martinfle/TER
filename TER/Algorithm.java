import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
}
