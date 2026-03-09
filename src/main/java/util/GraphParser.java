package util;

import model.Graph;

import java.io.*;
import java.util.*;

/**
 * Parser para os dois formatos de entrada da prova:
 *
 * Formato A — Lista de adjacência (entrada por arestas)
 *   Linha 1 : n m tipo       (tipo = D ou U)
 *   Próximas m linhas: u v [w]
 *
 * Formato B — Matriz de adjacência
 *   Linha 1 : n tipo
 *   Próximas n linhas: n valores (0/1 ou pesos)
 *
 * O parser detecta o formato automaticamente:
 *   - Se a linha 1 tem 3 tokens  → Formato A
 *   - Se a linha 1 tem 2 tokens  → Formato B
 *
 * Linhas em branco e comentários (#, //) são ignorados.
 */
public class GraphParser {

    public static class ParseResult {
        public final Graph graph;
        public final String format;   // "A" ou "B"
        public final String summary;

        public ParseResult(Graph graph, String format, String summary) {
            this.graph = graph;
            this.format = format;
            this.summary = summary;
        }
    }

    /** Lê de arquivo */
    public static ParseResult parse(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            return parse(br);
        }
    }

    /** Lê de string (útil para testes) */
    public static ParseResult parseString(String text) throws IOException {
        try (BufferedReader br = new BufferedReader(new StringReader(text))) {
            return parse(br);
        }
    }

    // -------------------------------------------------------------------------
    private static ParseResult parse(BufferedReader br) throws IOException {
        String firstLine = readMeaningfulLine(br);
        if (firstLine == null) throw new IOException("Arquivo vazio.");

        String[] parts = firstLine.trim().split("\\s+");

        if (parts.length == 3) {
            return parseFormatA(br, parts);
        } else if (parts.length == 2) {
            return parseFormatB(br, parts);
        } else {
            throw new IOException(
                    "Formato inválido na linha 1: '" + firstLine + "'.\n" +
                            "Esperado Formato A: 'n m tipo'  ou  Formato B: 'n tipo'"
            );
        }
    }

    // =========================================================================
    // FORMATO A — lista de adjacência por arestas
    // =========================================================================
    private static ParseResult parseFormatA(BufferedReader br, String[] header)
            throws IOException {

        int n, m;
        Graph.Type type;

        try {
            n = Integer.parseInt(header[0]);
            m = Integer.parseInt(header[1]);
        } catch (NumberFormatException e) {
            throw new IOException("Formato A: n e m devem ser inteiros. Recebeu: '"
                    + header[0] + "' e '" + header[1] + "'");
        }

        type = parseType(header[2]);

        if (n <= 0) throw new IOException("Número de vértices deve ser > 0. Recebeu: " + n);
        if (m < 0)  throw new IOException("Número de arestas deve ser >= 0. Recebeu: " + m);

        // coleta as linhas de arestas primeiro para detectar se é ponderado
        List<String[]> edgeLines = new ArrayList<>();
        boolean weighted = false;

        for (int i = 0; i < m; i++) {
            String line = readMeaningfulLine(br);
            if (line == null) throw new IOException(
                    "Formato A: esperava " + m + " arestas, encontrou apenas " + i + ".");
            String[] tok = line.trim().split("\\s+");
            if (tok.length < 2) throw new IOException(
                    "Aresta " + (i + 1) + " malformada: '" + line + "'");
            edgeLines.add(tok);
            if (tok.length >= 3) weighted = true;
        }

        Graph g = new Graph(n, type, weighted);

        for (String[] tok : edgeLines) {
            int u = parseVertex(tok[0], n);
            int v = parseVertex(tok[1], n);
            double w = (tok.length >= 3) ? parseWeight(tok[2]) : 1.0;
            g.addEdge(u, v, w);
        }

        String summary = buildSummary("A", g, n, m);
        return new ParseResult(g, "A", summary);
    }

    // =========================================================================
    // FORMATO B — matriz de adjacência
    // =========================================================================
    private static ParseResult parseFormatB(BufferedReader br, String[] header)
            throws IOException {

        int n;
        Graph.Type type;

        try {
            n = Integer.parseInt(header[0]);
        } catch (NumberFormatException e) {
            throw new IOException("Formato B: n deve ser inteiro. Recebeu: '" + header[0] + "'");
        }

        type = parseType(header[1]);

        if (n <= 0) throw new IOException("Número de vértices deve ser > 0. Recebeu: " + n);

        double[][] mat = new double[n][n];
        boolean weighted = false;

        for (int r = 0; r < n; r++) {
            String line = readMeaningfulLine(br);
            if (line == null) throw new IOException(
                    "Formato B: esperava " + n + " linhas de matriz, faltou a linha " + r + ".");

            String[] tok = line.trim().split("\\s+");
            if (tok.length != n) throw new IOException(
                    "Formato B linha " + r + ": esperava " + n + " valores, encontrou " + tok.length
                            + " → '" + line + "'");

            for (int c = 0; c < n; c++) {
                mat[r][c] = parseWeight(tok[c]);
                if (mat[r][c] != 0 && mat[r][c] != 1) weighted = true;
            }
        }

        Graph g = new Graph(n, type, weighted);

        int edgeCount = 0;
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (mat[r][c] != 0) {
                    // Para não-direcionado evita aresta dupla: só triângulo superior
                    if (type == Graph.Type.UNDIRECTED && c < r) continue;
                    g.addEdge(r, c, mat[r][c]);
                    edgeCount++;
                }
            }
        }

        String summary = buildSummary("B", g, n, edgeCount);
        return new ParseResult(g, "B", summary);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Pula linhas em branco e comentários */
    private static String readMeaningfulLine(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#") && !line.startsWith("//")) {
                return line;
            }
        }
        return null;
    }

    private static Graph.Type parseType(String s) throws IOException {
        return switch (s.toUpperCase()) {
            case "D" -> Graph.Type.DIRECTED;
            case "U" -> Graph.Type.UNDIRECTED;
            default  -> throw new IOException(
                    "Tipo de grafo inválido: '" + s + "'. Use D (direcionado) ou U (não-direcionado).");
        };
    }

    private static int parseVertex(String s, int n) throws IOException {
        try {
            int v = Integer.parseInt(s);
            if (v < 0 || v >= n) throw new IOException(
                    "Vértice " + v + " fora do intervalo [0, " + (n - 1) + "].");
            return v;
        } catch (NumberFormatException e) {
            throw new IOException("Vértice inválido: '" + s + "'");
        }
    }

    private static double parseWeight(String s) throws IOException {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            throw new IOException("Peso inválido: '" + s + "'");
        }
    }

    private static String buildSummary(String fmt, Graph g, int n, int m) {
        return "Formato " + fmt
                + "  |  Vértices: " + n
                + "  |  Arestas: " + m
                + "  |  Tipo: " + (g.getType() == Graph.Type.DIRECTED ? "Direcionado" : "Não-direcionado")
                + "  |  Ponderado: " + (g.isWeighted() ? "Sim" : "Não");
    }
}