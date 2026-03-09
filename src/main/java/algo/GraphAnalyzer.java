package algo;

import model.Graph;

import java.util.*;

/**
 * Calcula e imprime todas as propriedades exigidas pela prova.
 * Usa apenas ASCII puro para compatibilidade com TextArea do JavaFX.
 *
 *  1.  Tipo do grafo
 *  2.  Ordem |V| e Tamanho |E|
 *  3.  Graus dos vertices
 *  4.  Densidade (com formula)
 *  5.  Conectividade
 *  6.  Altura a partir de s (BFS)
 *  7.  BFS completo
 *  8.  DFS completo + deteccao de ciclo
 *  9.  Consulta 1 - Top-3 hubs por grau
 * 10.  Consulta 2 - Vizinhanca por niveis ate k passos de s
 */
public class GraphAnalyzer {

    private static final String SEP =
            "============================================================\n";

    private final Graph g;
    private final int n;

    public GraphAnalyzer(Graph g) {
        this.g = g;
        this.n = g.vertexCount();
    }

    // =========================================================================
    // RELATORIO COMPLETO
    // =========================================================================
    public String fullReport(int s, int t, int kNeighborhood) {
        StringBuilder sb = new StringBuilder();
        sb.append("============================================================\n");
        sb.append("        RELATORIO DE ANALISE DE GRAFO\n");
        sb.append("============================================================\n\n");

        sb.append(section1_tipo());
        sb.append(section2_ordem());
        sb.append(section3_graus());
        sb.append(section4_densidade());
        sb.append(section5_conectividade(s));
        sb.append(section6_altura(s));
        sb.append(section7_bfs(s, t));
        sb.append(section8_dfs(s, t));
        sb.append(section9_hubs());
        sb.append(section10_vizinhanca(s, kNeighborhood));

        return sb.toString();
    }

    // =========================================================================
    // 1. TIPO DO GRAFO
    // =========================================================================
    public String section1_tipo() {
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
    public String section2_ordem() {
        StringBuilder sb = new StringBuilder();
        titulo(sb, "2. ORDEM E TAMANHO");
        sb.append("  |V| = ").append(n).append("  (vertices)\n");
        sb.append("  |E| = ").append(g.edgeCount()).append("  (arestas/arcos)\n\n");
        return sb.toString();
    }

    // =========================================================================
    // 3. GRAUS DOS VERTICES
    // =========================================================================
    public String section3_graus() {
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
    public String section4_densidade() {
        StringBuilder sb = new StringBuilder();
        titulo(sb, "4. DENSIDADE");

        int m = g.edgeCount();
        double densidade;
        String formula;

        if (g.getType() == Graph.Type.DIRECTED) {
            long maxArcos = (long) n * (n - 1);
            densidade = (maxArcos == 0) ? 0.0 : (double) m / maxArcos;
            formula = "|E| / (|V|*(|V|-1))  =  "
                    + m + " / (" + n + "*" + (n - 1) + ")  =  "
                    + m + " / " + ((long) n * (n - 1));
        } else {
            long maxArestas = (long) n * (n - 1) / 2;
            densidade = (maxArestas == 0) ? 0.0 : (double) m / maxArestas;
            formula = "2*|E| / (|V|*(|V|-1))  =  2*"
                    + m + " / (" + n + "*" + (n - 1) + ")  =  "
                    + (2 * m) + " / " + ((long) n * (n - 1));
        }

        sb.append("  Formula      : ").append(formula).append("\n");
        sb.append("  Densidade d  = ").append(String.format("%.6f", densidade))
                .append(String.format("  (%.3f%%)", densidade * 100)).append("\n");
        sb.append("  Classificacao: ").append(densidade < 0.1 ? "ESPARSO" : "DENSO").append("\n\n");
        return sb.toString();
    }

    // =========================================================================
    // 5. CONECTIVIDADE
    // =========================================================================
    public String section5_conectividade(int s) {
        StringBuilder sb = new StringBuilder();
        titulo(sb, "5. CONECTIVIDADE");

        if (g.getType() == Graph.Type.UNDIRECTED) {
            int[] comp = new int[n];
            Arrays.fill(comp, -1);
            int numComp = 0;

            for (int start = 0; start < n; start++) {
                if (comp[start] != -1) continue;
                Deque<Integer> queue = new ArrayDeque<>();
                queue.add(start);
                comp[start] = numComp;
                while (!queue.isEmpty()) {
                    int u = queue.poll();
                    for (Graph.Edge e : g.neighbors(u)) {
                        if (comp[e.v] == -1) {
                            comp[e.v] = numComp;
                            queue.add(e.v);
                        }
                    }
                }
                numComp++;
            }

            sb.append("  Grafo eh ")
                    .append(numComp == 1 ? "CONEXO" : "NAO-CONEXO")
                    .append("\n");
            sb.append("  Numero de componentes conexas: ").append(numComp).append("\n\n");

            Map<Integer, List<Integer>> comps = new TreeMap<>();
            for (int v = 0; v < n; v++)
                comps.computeIfAbsent(comp[v], k -> new ArrayList<>()).add(v);
            for (Map.Entry<Integer, List<Integer>> entry : comps.entrySet()) {
                sb.append("  Componente ").append(entry.getKey())
                        .append(" (").append(entry.getValue().size()).append(" vertice(s)): ")
                        .append(entry.getValue()).append("\n");
            }

        } else {
            boolean[] visited = new boolean[n];
            Deque<Integer> queue = new ArrayDeque<>();
            queue.add(s);
            visited[s] = true;
            List<Integer> alcancaveis = new ArrayList<>();
            alcancaveis.add(s);

            while (!queue.isEmpty()) {
                int u = queue.poll();
                for (Graph.Edge e : g.neighbors(u)) {
                    if (!visited[e.v]) {
                        visited[e.v] = true;
                        alcancaveis.add(e.v);
                        queue.add(e.v);
                    }
                }
            }

            List<Integer> inalcancaveis = new ArrayList<>();
            for (int v = 0; v < n; v++) if (!visited[v]) inalcancaveis.add(v);

            sb.append("  Origem s = ").append(s).append("\n");
            sb.append("  Alcancaveis (").append(alcancaveis.size()).append("): ")
                    .append(alcancaveis).append("\n\n");
            sb.append("  Inalcancaveis a partir de s (").append(inalcancaveis.size()).append("): ");
            sb.append(inalcancaveis.isEmpty()
                            ? "[nenhum -- todos alcancaveis]"
                            : inalcancaveis.toString())
                    .append("\n");
        }

        sb.append("\n");
        return sb.toString();
    }

    // =========================================================================
    // 6. ALTURA (maior nivel BFS a partir de s)
    // =========================================================================
    public String section6_altura(int s) {
        StringBuilder sb = new StringBuilder();
        titulo(sb, "6. ALTURA DA ARVORE BFS A PARTIR DE s");
        sb.append("  Definicao adotada: altura(s) = maior distancia (em numero\n");
        sb.append("  de arestas) na arvore BFS enraizada em s.\n\n");

        int[] dist = bfsDistancias(s);
        int altura = 0, verticeMaisLonge = s, inalcancaveis = 0;

        for (int v = 0; v < n; v++) {
            if (dist[v] == Integer.MAX_VALUE) { inalcancaveis++; continue; }
            if (dist[v] > altura) { altura = dist[v]; verticeMaisLonge = v; }
        }

        sb.append("  Raiz s             : ").append(s).append("\n");
        sb.append("  altura(s)          = ").append(altura).append("\n");
        sb.append("  Vertice mais longe : ").append(verticeMaisLonge)
                .append("  (dist = ").append(altura).append(")\n");
        if (inalcancaveis > 0)
            sb.append("  Vertices inalcancaveis (fora da arvore): ").append(inalcancaveis).append("\n");
        sb.append("\n");
        return sb.toString();
    }

    // =========================================================================
    // 7. BFS COMPLETO
    // =========================================================================
    public String section7_bfs(int s, int t) {
        StringBuilder sb = new StringBuilder();
        titulo(sb, "7. BFS -- Busca em Largura");
        sb.append("  s = ").append(s).append("   t = ").append(t).append("\n\n");

        int[] dist   = new int[n];
        int[] parent = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(parent, -1);

        List<Integer> visitOrder = new ArrayList<>();
        Deque<Integer> queue = new ArrayDeque<>();
        dist[s] = 0;
        queue.add(s);

        while (!queue.isEmpty()) {
            int u = queue.poll();
            visitOrder.add(u);
            for (Graph.Edge e : g.neighbors(u)) {
                if (dist[e.v] == Integer.MAX_VALUE) {
                    dist[e.v] = dist[u] + 1;
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

        // Distancias
        sb.append("  Distancias dist(s, v) a partir de s=").append(s).append(":\n");
        sb.append(String.format("  %-6s  %-14s%n", "Vert.", "dist(s,v)"));
        sb.append("  ------  --------------\n");
        for (int v = 0; v < n; v++) {
            String d = (dist[v] == Integer.MAX_VALUE) ? "INF (inacessivel)" : String.valueOf(dist[v]);
            sb.append(String.format("  %-6d  %s%n", v, d));
        }
        sb.append("\n");

        // Caminho s -> t
        sb.append("  Caminho minimo BFS  s=").append(s).append(" -> t=").append(t).append(":\n");
        if (dist[t] == Integer.MAX_VALUE) {
            sb.append("  [SEM CAMINHO] Nao existe caminho de ").append(s)
                    .append(" ate ").append(t).append(".\n");
        } else {
            List<Integer> path = new ArrayList<>();
            for (int v = t; v != -1; v = parent[v]) {
                path.add(v);
                if (v == s) break;
            }
            Collections.reverse(path);
            sb.append("  ");
            for (int i = 0; i < path.size(); i++) {
                sb.append(path.get(i));
                if (i < path.size() - 1) sb.append(" -> ");
            }
            sb.append("\n  Hops: ").append(dist[t]).append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    // =========================================================================
    // 8. DFS COMPLETO + DETECCAO DE CICLO
    // =========================================================================
    public String section8_dfs(int s, int t) {
        StringBuilder sb = new StringBuilder();
        titulo(sb, "8. DFS -- Busca em Profundidade");
        sb.append("  s = ").append(s).append("   t = ").append(t).append("\n\n");

        boolean[] visited = new boolean[n];
        boolean[] onStack = new boolean[n];
        int[]     parent  = new int[n];
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

        // Deteccao de ciclo
        sb.append("  Deteccao de Ciclo:\n");
        if (g.getType() == Graph.Type.DIRECTED) {
            sb.append("  Regra usada: aresta de retorno (back-edge) -- ocorre quando\n");
            sb.append("  DFS visita (u,v) e v ainda esta na pilha de recursao (CINZA).\n");
            sb.append("  Isso indica que v eh ancestral de u, formando um ciclo.\n\n");
        } else {
            sb.append("  Regra usada: aresta (u,v) onde v ja foi visitado e v != pai(u).\n");
            sb.append("  Em grafo nao-direcionado, isso caracteriza um ciclo.\n\n");
        }
        if (hasCycle[0]) {
            sb.append("  Resultado   : [CICLO DETECTADO]\n");
            sb.append("  Evidencia   : ").append(backEdge[0]).append("\n");
        } else {
            sb.append("  Resultado   : [SEM CICLO] -- aciclico a partir de s\n");
        }
        sb.append("\n");

        // Caminho DFS s -> t
        sb.append("  Caminho DFS  s=").append(s).append(" -> t=").append(t).append(":\n");
        if (!visited[t]) {
            sb.append("  t=").append(t).append(" nao alcancado por DFS a partir de s.\n");
        } else {
            List<Integer> path = new ArrayList<>();
            for (int v = t; v != -1; v = parent[v]) {
                path.add(v);
                if (v == s) break;
            }
            Collections.reverse(path);
            sb.append("  ");
            for (int i = 0; i < path.size(); i++) {
                sb.append(path.get(i));
                if (i < path.size() - 1) sb.append(" -> ");
            }
            sb.append("\n  (Atencao: caminho DFS pode nao ser o menor)\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    // =========================================================================
    // 9. CONSULTA 1 - Top-3 hubs por grau
    // =========================================================================
    public String section9_hubs() {
        StringBuilder sb = new StringBuilder();
        titulo(sb, "9. CONSULTA 1 -- Top-3 Vertices por Grau (Hubs)");
        sb.append("  Vertices com maior grau = pontos mais centrais do cenario\n");
        sb.append("  (ex: cruzamentos movimentados, roteadores criticos).\n\n");

        int[] outDeg = new int[n];
        int[] inDeg  = new int[n];
        for (Graph.Edge e : g.allEdges()) {
            outDeg[e.u]++;
            inDeg[e.v]++;
        }

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
                        i + 1, v, total[v], outDeg[v], inDeg[v]));
            }
        } else {
            Arrays.sort(verts, (a, b) -> outDeg[b] - outDeg[a]);
            sb.append(String.format("  %-4s  %-6s  %-10s%n", "Pos.", "Vert.", "Grau"));
            sb.append("  ----  ------  ----------\n");
            for (int i = 0; i < Math.min(3, n); i++) {
                int v = verts[i];
                sb.append(String.format("  %-4d  %-6d  %-10d%n", i + 1, v, outDeg[v]));
            }
        }
        sb.append("\n");
        return sb.toString();
    }

    // =========================================================================
    // 10. CONSULTA 2 - Vizinhanca por niveis ate k passos de s
    // =========================================================================
    public String section10_vizinhanca(int s, int k) {
        StringBuilder sb = new StringBuilder();
        titulo(sb, "10. CONSULTA 2 -- Vizinhanca por Niveis (k=" + k + " passos de s=" + s + ")");
        sb.append("  Vertices acessiveis em ate ").append(k)
                .append(" passos a partir de s.\n");
        sb.append("  Representa o raio de influencia de s no cenario.\n\n");

        int[] dist = bfsDistancias(s);
        int total = 0;
        for (int nivel = 0; nivel <= k; nivel++) {
            List<Integer> nivelVerts = new ArrayList<>();
            for (int v = 0; v < n; v++)
                if (dist[v] == nivel) nivelVerts.add(v);
            total += nivelVerts.size();
            String label = (nivel == 0) ? "Nivel 0 (origem)" : "Nivel " + nivel + "         ";
            sb.append(String.format("  %-18s (%3d vertice(s)): %s%n",
                    label, nivelVerts.size(), nivelVerts));
        }
        sb.append("\n  Total de vertices em ate k=").append(k)
                .append(" passos: ").append(total).append("\n\n");
        return sb.toString();
    }

    // =========================================================================
    // Helpers privados
    // =========================================================================

    private int[] bfsDistancias(int s) {
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[s] = 0;
        Deque<Integer> queue = new ArrayDeque<>();
        queue.add(s);
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (Graph.Edge e : g.neighbors(u)) {
                if (dist[e.v] == Integer.MAX_VALUE) {
                    dist[e.v] = dist[u] + 1;
                    queue.add(e.v);
                }
            }
        }
        return dist;
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
                    parent[v] = u;
                    order.add(v);
                    stack.push(new int[]{v, 0});
                } else if (onStack[v] && !hasCycle[0]) {
                    if (g.getType() == Graph.Type.DIRECTED || v != parent[u]) {
                        hasCycle[0] = true;
                        String arrow = (g.getType() == Graph.Type.DIRECTED) ? " -> " : " -- ";
                        backEdge[0] = u + arrow + v
                                + "  (back-edge: " + v + " eh ancestral de " + u + ")";
                    }
                }
            } else {
                onStack[u] = false;
                stack.pop();
            }
        }
    }

    private void titulo(StringBuilder sb, String title) {
        sb.append(SEP);
        sb.append("  ").append(title).append("\n");
        sb.append(SEP);
    }
}