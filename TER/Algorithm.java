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
            for (int i = 0; i< g.values.size(); i++) {
                if( g.values.get(i).size() == 1) {
                    v = i;
                    k= g.values.get(i).get(0);
                    break;
                }
            }
            if (v == -1) return;
            g.values.remove(v);
            for (Map.Entry<Integer, List<Integer>> entry: g.adjList.entrySet()) {
                
            }
            
        }
    }

    
}
