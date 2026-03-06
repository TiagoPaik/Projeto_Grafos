// ===== Arquivo: util/PoiGenerator.java =====
package util;

import model.Cell;
import model.Poi;
import model.PoiType;

import java.util.*;

/**
 * 1 POI por prédio:
 * - "Prédio" = componente conectado de células 0 (4-direções)
 * - "Entrada" = célula 1 adjacente ao prédio
 *
 * Agora com nomes ÚNICOS:
 * Casa 1, Casa 2... / Hospital 1...
 */
public final class PoiGenerator {

    private PoiGenerator() {}

    public static class Result {
        public final List<Poi> pois;
        public final int buildingCount;

        public Result(List<Poi> pois, int buildingCount) {
            this.pois = pois;
            this.buildingCount = buildingCount;
        }
    }

    public static Result generateOnePerBuilding(int[][] grid, long seed) {
        int R = grid.length;
        int C = grid[0].length;

        boolean[][] vis0 = new boolean[R][C];
        Random rnd = new Random(seed);

        // contadores para nomes únicos
        int casa = 1, escola = 1, hosp = 1, fac = 1, merc = 1;

        List<Poi> pois = new ArrayList<>();
        int buildingId = 0;

        for (int r = 0; r < R; r++) {
            for (int c = 0; c < C; c++) {
                if (grid[r][c] != 0 || vis0[r][c]) continue;

                Deque<Cell> q = new ArrayDeque<>();
                q.add(new Cell(r, c));
                vis0[r][c] = true;

                Set<Cell> entrances = new HashSet<>();

                while (!q.isEmpty()) {
                    Cell cur = q.pollFirst();

                    int[] dr = {1, -1, 0, 0};
                    int[] dc = {0, 0, 1, -1};

                    for (int k = 0; k < 4; k++) {
                        int nr = cur.r + dr[k];
                        int nc = cur.c + dc[k];
                        if (nr < 0 || nr >= R || nc < 0 || nc >= C) continue;

                        if (grid[nr][nc] == 0 && !vis0[nr][nc]) {
                            vis0[nr][nc] = true;
                            q.addLast(new Cell(nr, nc));
                        } else if (grid[nr][nc] == 1) {
                            entrances.add(new Cell(nr, nc));
                        }
                    }
                }

                if (!entrances.isEmpty()) {
                    Cell entrance = chooseEntranceDeterministic(entrances, rnd);

                    PoiType type = pickType(rnd);
                    String name = switch (type) {
                        case CASA -> "Casa " + (casa++);
                        case ESCOLA -> "Escola " + (escola++);
                        case HOSPITAL -> "Hospital " + (hosp++);
                        case FACULDADE -> "Faculdade " + (fac++);
                        case MERCADO -> "Mercado " + (merc++);
                    };

                    pois.add(new Poi(entrance, type, name, buildingId));
                }

                buildingId++;
            }
        }

        return new Result(pois, buildingId);
    }

    private static Cell chooseEntranceDeterministic(Set<Cell> entrances, Random rnd) {
        List<Cell> list = new ArrayList<>(entrances);
        list.sort(Comparator.<Cell>comparingInt(a -> a.r).thenComparingInt(a -> a.c));

        for (int i = list.size() - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            Cell tmp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, tmp);
        }
        return list.get(0);
    }

    private static PoiType pickType(Random rnd) {
        int x = rnd.nextInt(100);
        if (x < 55) return PoiType.CASA;       // 55%
        if (x < 70) return PoiType.MERCADO;    // 15%
        if (x < 85) return PoiType.ESCOLA;     // 15%
        if (x < 93) return PoiType.FACULDADE;  // 8%
        return PoiType.HOSPITAL;               // 7%
    }
}