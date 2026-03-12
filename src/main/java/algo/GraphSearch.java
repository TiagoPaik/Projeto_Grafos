package algo;

import model.Graph;

import java.util.*;

/**
 * BFS e DFS sobre o modelo genérico Graph.
 * Retorna resultado com caminho, ordem de visita e estatísticas.
 */
public class GraphSearch {

    // =========================================================================
    public static class Result {
        public final String algo;
        public final boolean found;
        public final List<Integer> path;          // caminho s -> t (ids de vértices)
        public final List<Integer> visitOrder;    // ordem de visita completa (para animação)
        public final int hops;
        public final double totalWeight;

        public Result(String algo, boolean found, List<Integer> path,
                      List<Integer> visitOrder, double totalWeight) {
            this.algo        = algo;
            this.found       = found;
            this.path        = path;
            this.visitOrder  = visitOrder;
            this.hops        = (path == null) ? 0 : path.size() - 1;
            this.totalWeight = totalWeight;
        }

        /** Gera relatório textual completo */
        public String report(Graph g, int s, int t) {
            StringBuilder sb = new StringBuilder();
            sb.append("=== RESULTADOS (s -> t) ===\n");
            sb.append("Algoritmo : ").append(algo).append("\n");
            sb.append("Vértices  : ").append(g.vertexCount()).append("\n");
            sb.append("Arestas   : ").append(g.edgeCount()).append("\n");
            sb.append("Tipo      : ")
                    .append(g.getType() == Graph.Type.DIRECTED ? "Direcionado" : "Não-direcionado")
                    .append("\n");
            sb.append("Ponderado : ").append(g.isWeighted() ? "Sim" : "Não").append("\n");
            sb.append("Origem  s : ").append(s).append("\n");
            sb.append("Destino t : ").append(t).append("\n\n");

            if (!found) {
                sb.append("Sem caminho de ").append(s).append(" até ").append(t).append(".\n");
                return sb.toString();
            }

            sb.append("Hops      : ").append(hops).append("\n");
            if (g.isWeighted()) {
                sb.append("Peso total: ").append(String.format("%.4f", totalWeight)).append("\n");
            }
            sb.append("Visitados : ").append(visitOrder.size()).append("\n\n");

            sb.append("Caminho (").append(path.size()).append(" vértices):\n");
            for (int i = 0; i < path.size(); i++) {
                sb.append(path.get(i));
                if (i < path.size() - 1) sb.append(" -> ");
                if (i > 0 && i % 12 == 0) sb.append('\n');
            }
            sb.append("\n");
            return sb.toString();
        }
    }

    // =========================================================================
    /** BFS — garante caminho mínimo em número de hops */
    public static Result bfs(Graph g, int s, int t) {
        int n = g.vertexCount();
        boolean[] visited  = new boolean[n];
        int[]     parent   = new int[n];
        Arrays.fill(parent, -1);

        List<Integer> visitOrder = new ArrayList<>();
        Deque<Integer> queue = new ArrayDeque<>();

        visited[s] = true;
        queue.addLast(s);

        while (!queue.isEmpty()) {
            int u = queue.pollFirst();
            visitOrder.add(u);

            if (u == t) {
                List<Integer> path = buildPath(parent, s, t);
                double w = pathWeight(g, path);
                return new Result("BFS", true, path, visitOrder, w);
            }

            for (Graph.Edge e : g.neighbors(u)) {
                if (!visited[e.v]) {
                    visited[e.v] = true;
                    parent[e.v] = u;
                    queue.addLast(e.v);
                }
            }
        }

        return new Result("BFS", false, null, visitOrder, 0);
    }

    // =========================================================================
    /** DFS — explora em profundidade (caminho pode não ser mínimo) */
    public static Result dfs(Graph g, int s, int t) {
        int n = g.vertexCount();
        boolean[] visited = new boolean[n];
        int[]     parent  = new int[n];
        Arrays.fill(parent, -1);

        List<Integer> visitOrder = new ArrayList<>();
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(s);

        while (!stack.isEmpty()) {
            int u = stack.pop();
            if (visited[u]) continue;

            visited[u] = true;
            visitOrder.add(u);

//            if (u == t) {
//                List<Integer> path = buildPath(parent, s, t);
//                double w = pathWeight(g, path);
//                return new Result("DFS", true, path, visitOrder, w);
//            }

            // ordem reversa para processar vizinhos na ordem natural da lista
            List<Graph.Edge> nbrs = g.neighbors(u);
            for (int k = nbrs.size() - 1; k >= 0; k--) {
                Graph.Edge e = nbrs.get(k);
                if (!visited[e.v]) {
                    if (parent[e.v] == -1) parent[e.v] = u;
                    stack.push(e.v);
                }
            }
        }

        return new Result("DFS", false, null, visitOrder, 0);
    }

    // =========================================================================
    private static List<Integer> buildPath(int[] parent, int s, int t) {
        List<Integer> path = new ArrayList<>();
        for (int v = t; v != -1; v = parent[v]) {
            path.add(v);
            if (v == s) break;
        }
        Collections.reverse(path);
        if (path.isEmpty() || path.get(0) != s) return null;
        return path;
    }

    private static double pathWeight(Graph g, List<Integer> path) {
        if (path == null || path.size() < 2) return 0;
        double total = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            int u = path.get(i), v = path.get(i + 1);
            for (Graph.Edge e : g.neighbors(u)) {
                if (e.v == v) { total += e.weight; break; }
            }
        }
        return total;
    }
}