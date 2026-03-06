package util;

import java.util.Random;

public final class CityGridGenerator {

    private CityGridGenerator() {}

    /**
     * Gera um mapa urbano (grid) onde:
     * 1 = rua (navegável)
     * 0 = quadra/prédio (bloqueio)
     */
    public static int[][] generate(int rows, int cols, long seed) {
        int[][] g = new int[rows][cols];
        Random rnd = new Random(seed);

        // Ajuste fino (mexe aqui pra ficar mais/menos "cidade")
        int AV_SPACING = 10;        // distância entre avenidas
        int AV_WIDTH = 2;           // largura da avenida
        int ST_SPACING = 5;         // ruas secundárias
        double ROAD_DAMAGE = 0.003; // "obras" bloqueando ruas (bem pequeno)

        // 1) tudo começa como bloqueio
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                g[r][c] = 0;

        // 2) avenidas horizontais
        for (int r = 0; r < rows; r++) {
            if (r % AV_SPACING == 0) {
                for (int w = 0; w < AV_WIDTH; w++) {
                    int rr = r + w;
                    if (rr < rows) for (int c = 0; c < cols; c++) g[rr][c] = 1;
                }
            }
        }

        // 3) avenidas verticais
        for (int c = 0; c < cols; c++) {
            if (c % AV_SPACING == 0) {
                for (int w = 0; w < AV_WIDTH; w++) {
                    int cc = c + w;
                    if (cc < cols) for (int r = 0; r < rows; r++) g[r][cc] = 1;
                }
            }
        }

        // 4) ruas secundárias
        for (int r = 0; r < rows; r++) {
            if (r % ST_SPACING == 0 && (r % AV_SPACING != 0) && ((r + 1) % AV_SPACING != 0)) {
                for (int c = 0; c < cols; c++) g[r][c] = 1;
            }
        }
        for (int c = 0; c < cols; c++) {
            if (c % ST_SPACING == 0 && (c % AV_SPACING != 0) && ((c + 1) % AV_SPACING != 0)) {
                for (int r = 0; r < rows; r++) g[r][c] = 1;
            }
        }

        // 5) pequenos becos/pátios conectados (pra não ficar bloco fechado demais)
        for (int r = 1; r < rows - 1; r++) {
            for (int c = 1; c < cols - 1; c++) {
                if (g[r][c] == 0 && rnd.nextDouble() < 0.01) {
                    if (g[r-1][c] == 1 || g[r+1][c] == 1 || g[r][c-1] == 1 || g[r][c+1] == 1) {
                        g[r][c] = 1;
                    }
                }
            }
        }

        // 6) obras (pouco)
        for (int r = 1; r < rows - 1; r++) {
            for (int c = 1; c < cols - 1; c++) {
                if (g[r][c] == 1 && rnd.nextDouble() < ROAD_DAMAGE) {
                    if (!isNearAvenue(r, c, AV_SPACING, AV_WIDTH)) g[r][c] = 0;
                }
            }
        }

        // 7) bordas navegáveis
        for (int r = 0; r < rows; r++) {
            g[r][0] = 1;
            g[r][cols - 1] = 1;
        }
        for (int c = 0; c < cols; c++) {
            g[0][c] = 1;
            g[rows - 1][c] = 1;
        }

        return g;
    }

    private static boolean isNearAvenue(int r, int c, int spacing, int width) {
        for (int w = 0; w < width; w++) {
            if ((r - w) % spacing == 0) return true;
            if ((c - w) % spacing == 0) return true;
        }
        return false;
    }
}