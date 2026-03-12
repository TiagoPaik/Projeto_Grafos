package algo;

import model.Graph;

import java.util.*;

/**
 * Calcula todas as propriedades exigidas pela prova.
 * Recebe s (origem), t (destino) e k (vizinhanca por niveis).
 * Mostra distancia total em km nos caminhos BFS e DFS.
 */
public class GraphAnalyzer {

    private static final String SEP =
            "============================================================\n";

    private final Graph g;
    private final int   n;

    public GraphAnalyzer(Graph g) {
        this.g = g;
        this.n = g.vertexCount();
    }

    // =========================================================================
    // RELATORIO COMPLETO
    // =========================================================================
    public String fullReport(int s, int t, int k) {
        int[] distS = bfsDistancias(s);
        int   altura = calcAltura(distS);

        StringBuilder sb = new StringBuilder();
        sb.append("============================================================\n");
        sb.append("        RELATORIO DE ANALISE DE GRAFO\n");
        sb.append("============================================================\n\n");

        sb.append(section1_tipo());
        sb.append(section2_ordem());
        sb.append(section3_graus());
        sb.append(section4_densidade());
        sb.append(section5_conectividade(s, distS));
        sb.append(section6_altura(s, distS, altura));
        sb.append(section7_bfs(s, t));
        sb.append(section8_dfs(s, t));
        sb.append(section9_hubs());
        sb.append(section10_vizinhanca(s, distS, k));

        return sb.toString();
    }

    // =========================================================================
    // 1. TIPO DO GRAFO
    // =========================================================================
    private String section1_tipo() {
        StringBuilder sb = new StringBuilder();
        titulo(sb, "1. TIPO DO GRAFO");
        sb.append("  Direcionamento : ")
                .append(g.getType() == Graph.Type.DIRECTED
                        ? "DIRECIONADO (D)" : "NAO-DIRECIONADO (U)")
                .append("\n");
        sb.append("  Ponderacao     : ")
                .append(g.isWeighted()
                        ? "PONDERADO (pesos nas arestas)" : "NAO-PONDERADO (arestas = 1)")
                .append("\n\n");
        return sb.toString();
    }

    // =========================================================================
    // 2. ORDEM E TAMANHO
    // =========================================================================
    private String section2_ordem() {
        StringBuilder sb = new StringBuilder();
        titulo(sb, "2. ORDEM E TAMANHO");
        sb.append("  |V| = ").append(n).append("  (vertices)\n");
        sb.append("  |E| = ").append(g.edgeCount()).append("  (arestas/arcos)\n\n");
        return sb.toString();
    }

    // =========================================================================
    // 3. GRAUS DOS VERTICES
    // =========================================================================
    private String section3_graus() {
        StringBuilder sb = new StringBuilder();
        titulo(sb, "3. GRAUS DOS VERTICES");

        int[] outDeg = new int[n];
        int[] inDeg  = new int[n];
        for (Graph.Edge e : g.allEdges()) {
            outDeg[e.u]++;
            inDeg[e.v]++;
        }

        if (g.getType() == Graph.Type.DIRECTED) {
            sb.append(String.format("  %-6s  %-12s  %-12s  %-12s%n",
                    "Vert.", "Grau_saida", "Grau_entrada", "Grau_total"));
            sb.append("  ------  ------------  ------------  ------------\n");
            for (int v = 0; v < n; v++) {
                sb.append(String.format("  %-6d  %-12d  %-12d  %-12d%n",
                        v, outDeg[v], inDeg[v], outDeg[v] + inDeg[v]));
            }
            int maxOut = Arrays.stream(outDeg).max().getAsInt();
            int maxIn  = Arrays.stream(inDeg).max().getAsInt();
            sb.append("\n  Maior grau_saida   : ").append(maxOut).append("\n");
            sb.append("  Maior grau_entrada : ").append(maxIn).append("\n");
        } else {
            sb.append(String.format("  %-6s  %-10s%n", "Vert.", "Grau"));
            sb.append("  ------  ----------\n");
            for (int v = 0; v < n; v++) {
                sb.append(String.format("  %-6d  %-10d%n", v, outDeg[v]));
            }
            int maxG = Arrays.stream(outDeg).max().getAsInt();
            int minG = Arrays.stream(outDeg).min().getAsInt();
            sb.append("\n  Grau maximo: ").append(maxG).append("\n");
            sb.append("  Grau minimo: ").append(minG).append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    // =========================================================================
    // 4. DENSIDADE
    // =========================================================================
    private String section4_densidade() {
        StringBuilder sb = new StringBuilder();
        titulo(sb, "4. DENSIDADE");

        int m = g.edgeCount();
        double densidade;
        String formula;

        if (g.getType() == Graph.Type.DIRECTED) {
            long max = (long) n * (n - 1);
            densidade = (max == 0) ? 0.0 : (double) m / max;
            formula = "|E| / (|V|*(|V|-1))  =  "
                    + m + " / (" + n + "*" + (n-1) + ")  =  " + m + " / " + max;
        } else {
            long max = (long) n * (n - 1) / 2;
            densidade = (max == 0) ? 0.0 : (double) m / max;
            formula = "2*|E| / (|V|*(|V|-1))  =  2*"
                    + m + " / (" + n + "*" + (n-1) + ")  =  " + (2*m) + " / " + (max*2);
        }

        sb.append("  Formula      : ").append(formula).append("\n");
        sb.append("  Densidade d  = ").append(String.format("%.6f", densidade))
                .append(String.format("  (%.3f%%)", densidade * 100)).append("\n");
        return sb.toString();
    }

    // =========================================================================
    // 5. CONECTIVIDADE
    // =========================================================================
    private String section5_conectividade(int s, int[] distS) {
        StringBuilder sb = new StringBuilder();
        titulo(sb, "5. CONECTIVIDADE");

        if (g.getType() == Graph.Type.UNDIRECTED) {
            int[] comp = new int[n];
            Arrays.fill(comp, -1);
            int numComp = 0;
            for (int start = 0; start < n; start++) {
                if (comp[start] != -1) continue;
                Deque<Integer> q = new ArrayDeque<>();
                q.add(start); comp[start] = numComp;
                while (!q.isEmpty()) {
                    int u = q.poll();
                    for (Graph.Edge e : g.neighbors(u)) {
                        if (comp[e.v] == -1) { comp[e.v] = numComp; q.add(e.v); }
                    }
                }
                numComp++;
            }
            sb.append("  Grafo eh ").append(numComp == 1 ? "CONEXO" : "NAO-CONEXO").append("\n");
            sb.append("  Componentes conexas: ").append(numComp).append("\n\n");
            Map<Integer, List<Integer>> comps = new TreeMap<>();
            for (int v = 0; v < n; v++)
                comps.computeIfAbsent(comp[v], k -> new ArrayList<>()).add(v);
            for (Map.Entry<Integer, List<Integer>> e : comps.entrySet())
                sb.append("  Componente ").append(e.getKey())
                        .append(" (").append(e.getValue().size()).append(" vert.): ")
                        .append(e.getValue()).append("\n");
        } else {
            List<Integer> alcancaveis   = new ArrayList<>();
            List<Integer> inalcancaveis = new ArrayList<>();
            for (int v = 0; v < n; v++) {
                if (distS[v] != Integer.MAX_VALUE) alcancaveis.add(v);
                else inalcancaveis.add(v);
            }
            sb.append("  Origem s = ").append(s).append("\n");
            sb.append("  Alcancaveis (").append(alcancaveis.size()).append("): ")
                    .append(alcancaveis).append("\n\n");
            sb.append("  Inalcancaveis a partir de s (").append(inalcancaveis.size()).append("): ");
            sb.append(inalcancaveis.isEmpty()
                            ? "[nenhum -- todos alcancaveis]" : inalcancaveis.toString())
                    .append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    // =========================================================================
    // 6. ALTURA
    // =========================================================================
    private String section6_altura(int s, int[] distS, int altura) {
        StringBuilder sb = new StringBuilder();
        titulo(sb, "6. ALTURA DA ARVORE BFS A PARTIR DE s");
        sb.append("  Definicao adotada: altura(s) = maior distancia em numero\n");
        sb.append("  de arestas na arvore BFS enraizada em s.\n\n");

        int verticeMaisLonge = s, inalcancaveis = 0;
        for (int v = 0; v < n; v++) {
            if (distS[v] == Integer.MAX_VALUE) { inalcancaveis++; continue; }
            if (distS[v] == altura) verticeMaisLonge = v;
        }

        sb.append("  Raiz s             : ").append(s).append("\n");
        sb.append("  altura(s)          = ").append(altura).append("\n");
        sb.append("  Vertice mais longe : ").append(verticeMaisLonge)
                .append("  (dist = ").append(altura).append(" hops)\n");
        if (inalcancaveis > 0)
            sb.append("  Vertices inalcancaveis: ").append(inalcancaveis).append("\n");
        sb.append("\n");
        return sb.toString();
    }

    // =========================================================================
    // 7. BFS COMPLETO  -- mostra hops E distancia em km
    // =========================================================================
    private String section7_bfs(int s, int t) {
        StringBuilder sb = new StringBuilder();
        titulo(sb, "7. BFS -- Busca em Largura");
        sb.append("  s = ").append(s).append("   t = ").append(t).append("\n\n");

        int[]    dist   = new int[n];
        int[]    parent = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(parent, -1);

        List<Integer>  visitOrder = new ArrayList<>();
        Deque<Integer> queue      = new ArrayDeque<>();
        dist[s] = 0;
        queue.add(s);

        while (!queue.isEmpty()) {
            int u = queue.poll();
            visitOrder.add(u);
            for (Graph.Edge e : g.neighbors(u)) {
                if (dist[e.v] == Integer.MAX_VALUE) {
                    dist[e.v]   = dist[u] + 1;
                    parent[e.v] = u;
                    queue.add(e.v);
                }
            }
        }

        // Ordem de visita
        sb.append("  Ordem de visita BFS:\n  ");
        for (int i = 0; i < visitOrder.size(); i++) {
            sb.append(visitOrder.get(i));
            if (i < visitOrder.size() - 1) sb.append(" -> ");
            if ((i + 1) % 16 == 0 && i < visitOrder.size() - 1) sb.append("\n  ");
        }
        sb.append("\n\n");

        // Distancias em hops
        sb.append("  Distancias dist(s,v) a partir de s=").append(s).append(":\n");
        sb.append(String.format("  %-6s  %-10s%n", "Vert.", "dist(s,v)"));
        sb.append("  ------  ----------\n");
        for (int v = 0; v < n; v++) {
            String d = (dist[v] == Integer.MAX_VALUE) ? "INF" : String.valueOf(dist[v]);
            sb.append(String.format("  %-6d  %s%n", v, d));
        }
        sb.append("\n");

        // Caminho s -> t com hops e km
        sb.append("  Caminho minimo BFS  s=").append(s).append(" -> t=").append(t).append(":\n");
        if (dist[t] == Integer.MAX_VALUE) {
            sb.append("  [SEM CAMINHO] Nao existe caminho de ").append(s)
                    .append(" ate ").append(t).append(".\n");
        } else {
            List<Integer> path = reconstruirCaminho(parent, s, t);
            imprimirCaminho(sb, path);
            sb.append("  Hops            : ").append(dist[t]).append("\n");
            if (g.isWeighted()) {
                double km = pesoTotal(path);
                sb.append("  Distancia total : ").append(String.format("%.2f km", km)).append("\n");
            }
        }
        sb.append("\n");
        return sb.toString();
    }

    // =========================================================================
    // 8. DFS COMPLETO  -- mostra hops E distancia em km
    // =========================================================================
    private String section8_dfs(int s, int t) {
        StringBuilder sb = new StringBuilder();
        titulo(sb, "8. DFS -- Busca em Profundidade");
        sb.append("  s = ").append(s).append("   t = ").append(t).append("\n\n");

        boolean[]    visited = new boolean[n];
        boolean[]    onStack = new boolean[n];
        int[]        parent  = new int[n];
        Arrays.fill(parent, -1);

        List<Integer> order    = new ArrayList<>();
        boolean[]     hasCycle = {false};
        String[]      backEdge = {""};

        dfsIterativo(s, visited, onStack, order, hasCycle, backEdge, parent);

        // Ordem de descoberta
        sb.append("  Ordem de descoberta DFS (a partir de s=").append(s).append("):\n  ");
        for (int i = 0; i < order.size(); i++) {
            sb.append(order.get(i));
            if (i < order.size() - 1) sb.append(" -> ");
            if ((i + 1) % 16 == 0 && i < order.size() - 1) sb.append("\n  ");
        }
        sb.append("\n\n");

        // Caminho DFS s -> t com hops e km
        sb.append("  Caminho DFS  s=").append(s).append(" -> t=").append(t).append(":\n");
        if (!visited[t]) {
            sb.append("  t=").append(t).append(" nao alcancado por DFS a partir de s.\n");
        } else {
            List<Integer> path = reconstruirCaminho(parent, s, t);
            imprimirCaminho(sb, path);
            sb.append("  Hops            : ").append(path.size() - 1).append("\n");
            if (g.isWeighted()) {
                double km = pesoTotal(path);
                sb.append("  Distancia total : ").append(String.format("%.2f km", km)).append("\n");
            }
            sb.append("  (Atencao: caminho DFS pode nao ser o menor)\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    // =========================================================================
    // 9. CONSULTA 1 - Top-3 hubs
    // =========================================================================
    private String section9_hubs() {
        StringBuilder sb = new StringBuilder();
        titulo(sb, "9. CONSULTA 1 -- Top-3 Vertices por Grau (Hubs)");
        sb.append("  Vertices com maior grau = pontos mais centrais do cenario.\n\n");

        int[] outDeg = new int[n];
        int[] inDeg  = new int[n];
        for (Graph.Edge e : g.allEdges()) { outDeg[e.u]++; inDeg[e.v]++; }

        Integer[] verts = new Integer[n];
        for (int i = 0; i < n; i++) verts[i] = i;

        if (g.getType() == Graph.Type.DIRECTED) {
            int[] total = new int[n];
            for (int i = 0; i < n; i++) total[i] = outDeg[i] + inDeg[i];
            Arrays.sort(verts, (a, b) -> total[b] - total[a]);
            sb.append(String.format("  %-4s  %-6s  %-12s  %-8s  %-8s%n",
                    "Pos.", "Vert.", "Grau_total", "Saida", "Entrada"));
            sb.append("  ----  ------  ------------  --------  --------\n");
            for (int i = 0; i < Math.min(3, n); i++) {
                int v = verts[i];
                sb.append(String.format("  %-4d  %-6d  %-12d  %-8d  %-8d%n",
                        i+1, v, total[v], outDeg[v], inDeg[v]));
            }
        } else {
            Arrays.sort(verts, (a, b) -> outDeg[b] - outDeg[a]);
            sb.append(String.format("  %-4s  %-6s  %-10s%n", "Pos.", "Vert.", "Grau"));
            sb.append("  ----  ------  ----------\n");
            for (int i = 0; i < Math.min(3, n); i++) {
                int v = verts[i];
                sb.append(String.format("  %-4d  %-6d  %-10d%n", i+1, v, outDeg[v]));
            }
        }
        sb.append("\n");
        return sb.toString();
    }

    // =========================================================================
    // 10. CONSULTA 2 - Vizinhanca por niveis
    // =========================================================================
    private String section10_vizinhanca(int s, int[] distS, int k) {
        StringBuilder sb = new StringBuilder();
        titulo(sb, "10. CONSULTA 2 -- Vizinhanca por Niveis (k=" + k + " passos de s=" + s + ")");
        sb.append("  Vertices acessiveis em ate ").append(k)
                .append(" passos a partir de s=").append(s).append(".\n\n");

        int total = 0;
        for (int nivel = 0; nivel <= k; nivel++) {
            List<Integer> nivelVerts = new ArrayList<>();
            for (int v = 0; v < n; v++)
                if (distS[v] == nivel) nivelVerts.add(v);
            total += nivelVerts.size();
            String label = (nivel == 0) ? "Nivel 0 (origem)" : "Nivel " + nivel;
            sb.append(String.format("  %-18s (%3d vert.): %s%n",
                    label, nivelVerts.size(), nivelVerts));
        }
        sb.append("\n  Total em ate k=").append(k)
                .append(" passos: ").append(total).append("\n\n");
        return sb.toString();
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Reconstroi caminho de s ate t usando array de predecessores */
    private List<Integer> reconstruirCaminho(int[] parent, int s, int t) {
        List<Integer> path = new ArrayList<>();
        for (int v = t; v != -1; v = parent[v]) {
            path.add(v);
            if (v == s) break;
        }
        Collections.reverse(path);
        return path;
    }

    /** Imprime caminho no StringBuilder */
    private void imprimirCaminho(StringBuilder sb, List<Integer> path) {
        sb.append("  ");
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i));
            if (i < path.size() - 1) sb.append(" -> ");
        }
        sb.append("\n");
    }

    /**
     * Soma dos pesos das arestas do caminho.
     * Para grafos nao-ponderados retorna numero de hops (cada aresta = 1).
     */
    private double pesoTotal(List<Integer> path) {
        if (path == null || path.size() < 2) return 0.0;
        double total = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            int u = path.get(i), v = path.get(i + 1);
            for (Graph.Edge e : g.neighbors(u)) {
                if (e.v == v) { total += e.weight; break; }
            }
        }
        return total;
    }

    private int[] bfsDistancias(int s) {
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[s] = 0;
        Deque<Integer> q = new ArrayDeque<>();
        q.add(s);
        while (!q.isEmpty()) {
            int u = q.poll();
            for (Graph.Edge e : g.neighbors(u)) {
                if (dist[e.v] == Integer.MAX_VALUE) {
                    dist[e.v] = dist[u] + 1;
                    q.add(e.v);
                }
            }
        }
        return dist;
    }

    private int calcAltura(int[] dist) {
        int h = 0;
        for (int v = 0; v < n; v++)
            if (dist[v] != Integer.MAX_VALUE && dist[v] > h) h = dist[v];
        return h;
    }

    private void dfsIterativo(int s, boolean[] visited, boolean[] onStack,
                              List<Integer> order, boolean[] hasCycle,
                              String[] backEdge, int[] parent) {
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{s, 0});
        visited[s] = true;
        onStack[s] = true;
        order.add(s);

        while (!stack.isEmpty()) {
            int[] top = stack.peek();
            int u = top[0];
            List<Graph.Edge> nbrs = g.neighbors(u);

            if (top[1] < nbrs.size()) {
                Graph.Edge e = nbrs.get(top[1]++);
                int v = e.v;
                if (!visited[v]) {
                    visited[v] = true;
                    onStack[v] = true;
                    parent[v]  = u;
                    order.add(v);
                    stack.push(new int[]{v, 0});
                } else if (onStack[v] && !hasCycle[0]) {
                    if (g.getType() == Graph.Type.DIRECTED || v != parent[u]) {
                        hasCycle[0] = true;
                        String arrow = g.getType() == Graph.Type.DIRECTED ? " -> " : " -- ";
                        backEdge[0]  = u + arrow + v
                                + "  (back-edge: " + v + " eh ancestral de " + u + ")";
                    }
                }
            } else {
                onStack[u] = false;
                stack.pop();
            }
        }
    }

    private void titulo(StringBuilder sb, String t) {
        sb.append(SEP).append("  ").append(t).append("\n").append(SEP);
    }
}