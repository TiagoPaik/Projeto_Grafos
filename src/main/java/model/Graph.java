package model;

import java.util.*;

/**
 * Grafo genérico que suporta:
 *  - direcionado (D) ou não-direcionado (U)
 *  - ponderado ou não
 *  - representação interna por lista de adjacência
 */
public class Graph {

    public enum Type { DIRECTED, UNDIRECTED }

    public static class Edge {
        public final int u, v;
        public final double weight;

        public Edge(int u, int v, double weight) {
            this.u = u;
            this.v = v;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return u + " -> " + v + (weight != 1.0 ? " (" + weight + ")" : "");
        }
    }

    private final int n;                          // número de vértices (0..n-1)
    private final Type type;
    private final boolean weighted;
    private final List<List<Edge>> adj;           // lista de adjacência
    private final List<Edge> allEdges;            // todas as arestas originais

    public Graph(int n, Type type, boolean weighted) {
        this.n = n;
        this.type = type;
        this.weighted = weighted;
        this.adj = new ArrayList<>();
        this.allEdges = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
    }

    /** Adiciona aresta (respeita direcionamento automaticamente) */
    public void addEdge(int u, int v, double weight) {
        Edge e = new Edge(u, v, weight);
        adj.get(u).add(e);
        allEdges.add(e);

        if (type == Type.UNDIRECTED) {
            adj.get(v).add(new Edge(v, u, weight));
        }
    }

    public void addEdge(int u, int v) {
        addEdge(u, v, 1.0);
    }

    public List<Edge> neighbors(int u) {
        return adj.get(u);
    }

    public int vertexCount()      { return n; }
    public int edgeCount()        { return allEdges.size(); }
    public Type getType()         { return type; }
    public boolean isWeighted()   { return weighted; }
    public List<Edge> allEdges()  { return Collections.unmodifiableList(allEdges); }

    /** Retorna representação como matriz de adjacência */
    public double[][] toAdjMatrix() {
        double[][] mat = new double[n][n];
        for (List<Edge> list : adj) {
            for (Edge e : list) {
                mat[e.u][e.v] = e.weight;
            }
        }
        return mat;
    }

    /** Retorna string Formato A */
    public String toFormatA() {
        StringBuilder sb = new StringBuilder();
        sb.append(n).append(' ').append(allEdges.size())
                .append(' ').append(type == Type.DIRECTED ? 'D' : 'U').append('\n');
        for (Edge e : allEdges) {
            sb.append(e.u).append(' ').append(e.v);
            if (weighted) sb.append(' ').append(e.weight);
            sb.append('\n');
        }
        return sb.toString();
    }

    /** Retorna string Formato B */
    public String toFormatB() {
        double[][] mat = toAdjMatrix();
        StringBuilder sb = new StringBuilder();
        sb.append(n).append(' ').append(type == Type.DIRECTED ? 'D' : 'U').append('\n');
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (c > 0) sb.append(' ');
                if (weighted) sb.append(mat[r][c]);
                else sb.append((int) mat[r][c]);
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}