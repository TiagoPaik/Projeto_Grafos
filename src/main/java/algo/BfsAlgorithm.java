package algo;

import model.Cell;
import model.GridMap;

import java.util.*;

public class BfsAlgorithm implements SearchAlgorithm {

    private GridMap map;
    private Cell start, goal;

    private boolean[][] visited;
    private int[][] dist;
    private Cell[][] parent;

    private Deque<Cell> q;
    private boolean finished = false;

    @Override
    public void init(GridMap map, Cell start, Cell goal) {
        this.map = map;
        this.start = start;
        this.goal = goal;

        int R = map.rows(), C = map.cols();
        visited = new boolean[R][C];
        dist = new int[R][C];
        parent = new Cell[R][C];
        q = new ArrayDeque<>();

        for (int r = 0; r < R; r++) Arrays.fill(dist[r], -1);

        if (!map.isFree(start.r, start.c) || !map.isFree(goal.r, goal.c)) {
            throw new IllegalStateException("Start/Goal em célula bloqueada.");
        }

        visited[start.r][start.c] = true;
        dist[start.r][start.c] = 0;
        q.addLast(start);

        finished = false;
    }

    @Override
    public SearchStep step() {
        SearchStep s = new SearchStep();

        if (finished) {
            s.finished = true;
            return s;
        }

        if (q.isEmpty()) {
            finished = true;
            s.finished = true;
            s.foundGoal = false;
            s.message = "Sem caminho (BFS).";
            return s;
        }

        Cell cur = q.pollFirst();
        s.visitedNow.add(cur);

        if (cur.equals(goal)) {
            finished = true;
            s.finished = true;
            s.foundGoal = true;
            s.path = buildPath();
            s.distToGoal = dist[goal.r][goal.c];
            s.message = "Achou o destino!";
            return s;
        }

        int[] dr = {1, -1, 0, 0};
        int[] dc = {0, 0, 1, -1};

        for (int k = 0; k < 4; k++) {
            int nr = cur.r + dr[k];
            int nc = cur.c + dc[k];

            if (!map.canMove(cur.r, cur.c, nr, nc)) continue;
            if (visited[nr][nc]) continue;

            visited[nr][nc] = true;
            dist[nr][nc] = dist[cur.r][cur.c] + 1;
            parent[nr][nc] = cur;

            Cell nxt = new Cell(nr, nc);
            q.addLast(nxt);
            s.frontierAddedNow.add(nxt);
        }

        s.distToGoal = dist[goal.r][goal.c];
        s.message = "BFS... dist(goal)=" + s.distToGoal;
        return s;
    }

    @Override
    public boolean isFinished() { return finished; }

    @Override
    public List<Cell> getPath() { return buildPath(); }

    @Override
    public int getDistToGoal() { return dist[goal.r][goal.c]; }

    private List<Cell> buildPath() {
        if (dist[goal.r][goal.c] == -1) return null;

        List<Cell> path = new ArrayList<>();
        Cell cur = goal;
        while (cur != null) {
            path.add(cur);
            if (cur.equals(start)) break;
            cur = parent[cur.r][cur.c];
        }
        Collections.reverse(path);
        return path;
    }
}