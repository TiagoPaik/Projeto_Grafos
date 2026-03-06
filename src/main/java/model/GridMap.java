package model;

public class GridMap {

    public static final int EDGE_DISTANCE_METERS = 100;

    public int edgeDistanceMeters(int r, int c, int nr, int nc) {
        return EDGE_DISTANCE_METERS;
    }

    public double edgeTimeSeconds(int r, int c, int nr, int nc) {
        // velocidade (km/h) varia de 20 a 60
        int v = edgeSpeedKmh(r, c, nr, nc); // 20..60
        double metersPerSecond = (v * 1000.0) / 3600.0;
        return EDGE_DISTANCE_METERS / metersPerSecond;
    }

    private int edgeSpeedKmh(int r, int c, int nr, int nc) {
        // hash simples e determinístico
        int h = 17;
        h = 31 * h + r;
        h = 31 * h + c;
        h = 31 * h + nr;
        h = 31 * h + nc;

        // força positivo
        h = h & 0x7fffffff;

        // 20..60
        return 20 + (h % 41);
    }

    // 1 = rua (livre), 0 = bloqueio
    private final int[][] grid;

    public GridMap(int[][] grid) {
        this.grid = grid;
    }

    public int rows() { return grid.length; }
    public int cols() { return grid[0].length; }

    public boolean inBounds(int r, int c) {
        return r >= 0 && r < rows() && c >= 0 && c < cols();
    }

    public boolean isFree(int r, int c) {
        return grid[r][c] == 1;
    }

    public boolean canMove(int r, int c, int nr, int nc) {
        // só se destino é dentro e é rua
        if (!inBounds(nr, nc) || !isFree(nr, nc)) return false;

        int dr = nr - r;
        int dc = nc - c;

        // movimento deve ser 1 passo (4-dir)
        if (Math.abs(dr) + Math.abs(dc) != 1) return false;

        // regra de mão única urbana:
        // horizontais: linha par -> leste, linha ímpar -> oeste
        if (dc == 1)  return (r % 2 == 0); // indo para direita
        if (dc == -1) return (r % 2 == 1); // indo para esquerda

        // verticais: coluna par -> sul, coluna ímpar -> norte
        if (dr == 1)  return (c % 2 == 0); // indo para baixo
        if (dr == -1) return (c % 2 == 1); // indo para cima

        return false;
    }
    public boolean isWall(int r, int c) {
        return grid[r][c] == 0;
    }
}