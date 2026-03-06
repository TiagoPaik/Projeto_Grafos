import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class BFSVisualizer extends Application {

    // 1 = livre, 0 = bloqueado
    int[][] grid = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,0,0,0,1,1,1,0,0,0,1,1,1,0,1,1,0,1,1,1},
            {1,0,1,0,1,0,0,1,1,1,0,0,1,0,1,0,1,0,0,1},
            {1,0,0,0,1,1,0,0,0,0,0,1,1,0,1,0,1,0,1,1},
            {1,0,1,1,1,0,0,1,1,1,0,1,1,0,1,0,0,1,0,1},
            {1,0,1,1,1,1,0,1,1,1,0,1,1,0,1,0,1,0,0,1},

            {1,1,0,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,0,1},

            {1,1,0,0,0,1,1,0,0,0,1,1,1,0,1,1,0,1,0,1},
            {1,1,0,1,0,1,0,1,1,1,0,1,1,0,1,0,1,0,0,1},
            {1,1,0,0,0,1,0,0,0,0,0,1,1,0,1,0,1,0,1,1},
            {1,1,0,1,1,1,0,1,1,1,0,1,1,0,1,0,0,1,0,1},
            {1,1,0,1,1,1,0,1,1,1,0,1,1,0,1,0,1,0,0,1},

            {1,0,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1},

            {1,0,0,0,1,1,1,0,0,0,1,1,1,0,1,1,0,1,0,1},
            {1,0,1,0,1,1,0,1,1,1,0,1,1,0,1,0,1,0,0,1},
            {1,0,0,0,1,1,0,0,0,0,0,1,1,0,1,0,1,0,1,1},
            {1,0,1,1,1,1,0,1,1,1,0,1,1,0,1,0,0,1,0,1},
            {1,0,1,1,1,1,0,1,1,1,0,1,1,0,1,0,1,0,0,1},

            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };
    private final int R = grid.length;
    private final int C = grid[0].length;

    private final int sr = 0,  sc = 0;   // é 1
    private final int gr = 19, gc = 19;  // é 1

    private Rectangle[][] cells;
    private Label status;

    private boolean[][] visited;
    private int[][] dist;
    private int[][] pr, pc;
    private Deque<int[]> queue;
    private Timeline timeline;

    private static final int CELL_SIZE = 20;

    private static final Color FREE = Color.web("#f6f7fb");
    private static final Color WALL = Color.web("#1f2937");
    private static final Color VISITED = Color.web("#60a5fa");
    private static final Color FRONTIER = Color.web("#fbbf24");
    private static final Color START = Color.web("#22c55e");
    private static final Color GOAL = Color.web("#ef4444");
    private static final Color PATH = Color.web("#a78bfa");

    @Override
    public void start(Stage stage) {
        cells = new Rectangle[R][C];

        GridPane gridPane = new GridPane();
        gridPane.setHgap(2);
        gridPane.setVgap(2);
        gridPane.setPadding(new Insets(14));
        gridPane.setAlignment(Pos.CENTER);

        for (int r = 0; r < R; r++) {
            for (int c = 0; c < C; c++) {
                Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE);
                rect.setArcWidth(10);
                rect.setArcHeight(10);
                rect.setStroke(Color.web("#e5e7eb"));
                rect.setStrokeWidth(1.0);

                cells[r][c] = rect;
                gridPane.add(rect, c, r);
            }
        }

        status = new Label("Pronto. Clique em Rodar.");
        status.setStyle("-fx-font-size: 14px; -fx-text-fill: #111827;");

        Button runBtn = new Button("Rodar BFS");
        Button resetBtn = new Button("Reset");
        runBtn.setStyle("-fx-font-weight: bold;");
        resetBtn.setStyle("-fx-font-weight: bold;");

        Slider speed = new Slider(10, 300, 80);
        speed.setShowTickLabels(true);
        speed.setShowTickMarks(true);
        speed.setMajorTickUnit(50);
        speed.setMinorTickCount(4);
        Label speedLabel = new Label("Velocidade (ms/frame):");

        HBox controls = new HBox(12, runBtn, resetBtn, speedLabel, speed, status);
        controls.setPadding(new Insets(12));
        controls.setAlignment(Pos.CENTER_LEFT);

        BorderPane root = new BorderPane();
        root.setCenter(gridPane);
        root.setBottom(controls);
        root.setStyle("-fx-background-color: #ffffff;");

        initBfs();
        paintBase();

        runBtn.setOnAction(e -> runAnimation((int) speed.getValue()));
        resetBtn.setOnAction(e -> {
            stopAnimation();
            initBfs();
            paintBase();
            status.setText("Resetado. Clique em Rodar.");
        });

        stage.setTitle("BFS Visualizer (Menor Caminho em Grid)");
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void initBfs() {
        visited = new boolean[R][C];
        dist = new int[R][C];
        pr = new int[R][C];
        pc = new int[R][C];
        queue = new ArrayDeque<>();

        for (int r = 0; r < R; r++) {
            Arrays.fill(dist[r], -1);
            Arrays.fill(pr[r], -1);
            Arrays.fill(pc[r], -1);
        }

        if (grid[sr][sc] == 0 || grid[gr][gc] == 0) {
            throw new IllegalStateException("Start/Goal está em célula bloqueada. Ajuste o grid ou coordenadas.");
        }

        visited[sr][sc] = true;
        dist[sr][sc] = 0;
        queue.add(new int[]{sr, sc});
    }

    private void paintBase() {
        for (int r = 0; r < R; r++) {
            for (int c = 0; c < C; c++) {
                cells[r][c].setFill(grid[r][c] == 0 ? WALL : FREE);
            }
        }
        cells[sr][sc].setFill(START);
        cells[gr][gc].setFill(GOAL);
    }

    private void runAnimation(int msPerFrame) {
        stopAnimation();
        status.setText("Rodando BFS...");

        timeline = new Timeline(new KeyFrame(Duration.millis(msPerFrame), e -> step()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.playFromStart();
    }

    private void stopAnimation() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }

    private void step() {
        if (queue.isEmpty()) {
            status.setText("Sem caminho até o destino.");
            stopAnimation();
            return;
        }

        int[] cur = queue.pollFirst();
        int r = cur[0], c = cur[1];

        if (!(r == sr && c == sc) && !(r == gr && c == gc)) {
            cells[r][c].setFill(VISITED);
        }

        if (r == gr && c == gc) {
            stopAnimation();
            drawPath();
            return;
        }

        int[] dr = {1, -1, 0, 0};
        int[] dc = {0, 0, 1, -1};

        for (int k = 0; k < 4; k++) {
            int nr = r + dr[k];
            int nc = c + dc[k];

            if (nr < 0 || nr >= R || nc < 0 || nc >= C) continue;
            if (grid[nr][nc] == 0) continue;
            if (visited[nr][nc]) continue;

            visited[nr][nc] = true;
            dist[nr][nc] = dist[r][c] + 1;
            pr[nr][nc] = r;
            pc[nr][nc] = c;

            queue.addLast(new int[]{nr, nc});

            if (!(nr == gr && nc == gc)) {
                cells[nr][nc].setFill(FRONTIER);
            }
        }

        cells[sr][sc].setFill(START);
        cells[gr][gc].setFill(GOAL);

        status.setText("Explorando... dist(goal)=" + dist[gr][gc]);
    }

    private void drawPath() {
        if (dist[gr][gc] == -1) {
            status.setText("Sem caminho.");
            return;
        }

        int r = gr, c = gc;
        int steps = dist[r][c];

        while (!(r == sr && c == sc)) {
            int rr = pr[r][c];
            int cc = pc[r][c];
            if (rr == -1) break;

            if (!((r == gr && c == gc) || (r == sr && c == sc))) {
                cells[r][c].setFill(PATH);
            }

            r = rr; c = cc;
        }

        cells[sr][sc].setFill(START);
        cells[gr][gc].setFill(GOAL);
        status.setText("Caminho mínimo! passos=" + steps);
    }

    public static void main(String[] args) {
        launch(args);
    }
}