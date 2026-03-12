package app;

import algo.GraphAnalyzer;
import algo.GraphSearch;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Graph;
import util.GraphParser;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

/**
 * Tela de visualizacao de grafos genericos.
 * O usuario define s (origem), t (destino) e k (vizinhanca por niveis).
 */
public class GraphView {

    // ===== Cores =====
    private static final Color C_NODE_DEFAULT = Color.web("#e0e7ff");
    private static final Color C_NODE_VISITED = Color.web("#60a5fa");
    private static final Color C_NODE_PATH    = Color.web("#a78bfa");
    private static final Color C_NODE_START   = Color.web("#22c55e");
    private static final Color C_NODE_GOAL    = Color.web("#ef4444");
    private static final Color C_NODE_STROKE  = Color.web("#6366f1");
    private static final Color C_EDGE_DEFAULT = Color.web("#94a3b8");
    private static final Color C_EDGE_PATH    = Color.web("#7c3aed");

    private static final double NODE_R   = 18;
    private static final double CANVAS_W = 880;
    private static final double CANVAS_H = 620;

    // ===== Estado =====
    private Graph    graph;
    private double[] nx, ny;

    private Circle[]        nodeCircles;
    private Text[]          nodeLabels;
    private final List<Line>    edgeLines  = new ArrayList<>();
    private final List<Polygon> edgeArrows = new ArrayList<>();

    private Pane     canvas;
    private TextArea resultsArea;
    private Label    statusLabel;

    private Spinner<Integer> spinnerS, spinnerT, spinnerK;
    private ToggleGroup algoToggle;
    private Timeline    timeline;
    private int         animStep;
    private List<Integer> visitOrder;
    private List<Integer> currentPath;

    // =========================================================================
    public static void show(Stage stage, Consumer<Void> onBack) {
        new GraphView().buildAndShow(stage, onBack);
    }

    private void buildAndShow(Stage stage, Consumer<Void> onBack) {

        // ---- Canvas ----
        canvas = new Pane();
        canvas.setPrefSize(CANVAS_W, CANVAS_H);
        canvas.setStyle(
                "-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-width:1;");

        ScrollPane canvasScroll = new ScrollPane(canvas);
        canvasScroll.setFitToWidth(true);
        canvasScroll.setFitToHeight(true);

        // ---- Painel direito ----
        resultsArea = new TextArea(
                "Carregue um arquivo .txt para comecar.\n\n" +
                        "Formato A:\n  n m tipo\n  u v [w]\n  ...\n\n" +
                        "Formato B:\n  n tipo\n  (matriz n x n)\n\n" +
                        "Apos carregar, defina s, t e k e clique em:\n" +
                        "  [ Animar ]    -- visualiza BFS/DFS\n" +
                        "  [ Relatorio ] -- relatorio completo da prova\n\n" +
                        "  s = vertice de origem\n" +
                        "  t = vertice de destino\n" +
                        "  k = niveis de vizinhanca (Consulta 2)"
        );
        resultsArea.setEditable(false);
        resultsArea.setWrapText(false);
        resultsArea.setPrefWidth(420);
        resultsArea.setStyle("-fx-font-family: Consolas; -fx-font-size: 11px;");
        // TextArea cresce verticalmente para preencher todo o espaco disponivel no VBox
        VBox.setVgrow(resultsArea, javafx.scene.layout.Priority.ALWAYS);

        Label titleRight = new Label("Relatorio de Analise");
        titleRight.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        VBox rightPanel = new VBox(8, titleRight, resultsArea);
        rightPanel.setPadding(new Insets(10));
        rightPanel.setPrefWidth(440);
        // VBox ocupa toda a altura do BorderPane (coluna direita)
        rightPanel.setMaxHeight(Double.MAX_VALUE);
        rightPanel.setStyle(
                "-fx-background-color:#fff; -fx-border-color:#e2e8f0; -fx-border-width:0 0 0 1;");

        // ---- Botoes ----
        Button loadBtn   = new Button("Carregar .txt");
        Button animBtn   = new Button("Animar");
        Button reportBtn = new Button("Relatorio");
        Button resetBtn  = new Button("Reset");
        Button backBtn   = new Button("Menu");

        loadBtn.setStyle("-fx-font-weight: bold;");
        animBtn.setStyle("-fx-font-weight: bold;");
        resetBtn.setStyle("-fx-font-weight: bold;");
        backBtn.setStyle("-fx-font-weight: bold;");
        reportBtn.setStyle(
                "-fx-font-weight: bold; -fx-background-color:#4f46e5; -fx-text-fill:white;");

        RadioButton bfsRb = new RadioButton("BFS");
        RadioButton dfsRb = new RadioButton("DFS");
        algoToggle = new ToggleGroup();
        bfsRb.setToggleGroup(algoToggle);
        dfsRb.setToggleGroup(algoToggle);
        bfsRb.setSelected(true);

        spinnerS = new Spinner<>(0, 0, 0);
        spinnerT = new Spinner<>(0, 0, 0);
        spinnerK = new Spinner<>(1, 20, 3);
        spinnerS.setPrefWidth(70); spinnerS.setEditable(true);
        spinnerT.setPrefWidth(70); spinnerT.setEditable(true);
        spinnerK.setPrefWidth(65); spinnerK.setEditable(true);

        Slider speedSlider = new Slider(20, 800, 150);
        speedSlider.setPrefWidth(150);
        speedSlider.setShowTickMarks(true);
        speedSlider.setShowTickLabels(true);
        speedSlider.setMajorTickUnit(200);

        statusLabel = new Label("Pronto. Carregue um arquivo .txt.");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");

        HBox row1 = new HBox(10,
                backBtn, loadBtn,
                new Separator(),
                bfsRb, dfsRb,
                new Separator(),
                new Label("s:"), spinnerS,
                new Label("t:"), spinnerT,
                new Label("k:"), spinnerK,
                new Separator(),
                animBtn, reportBtn, resetBtn,
                new Separator(),
                new Label("ms/frame:"), speedSlider
        );
        row1.setAlignment(Pos.CENTER_LEFT);

        VBox bottomBar = new VBox(6, row1, statusLabel);
        bottomBar.setPadding(new Insets(10, 12, 12, 12));
        bottomBar.setStyle(
                "-fx-background-color:#f9fafb; -fx-border-color:#e5e7eb; -fx-border-width:1 0 0 0;");

        // ---- Root ----
        BorderPane root = new BorderPane();
        root.setCenter(canvasScroll);
        root.setRight(rightPanel);
        root.setBottom(bottomBar);

        // ---- Handlers ----
        loadBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Abrir grafo (.txt)");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Arquivo de texto", "*.txt"));
            File f = fc.showOpenDialog(stage);
            if (f != null) loadFile(f);
        });

        animBtn.setOnAction(e -> {
            if (graph == null) { statusLabel.setText("Carregue um arquivo primeiro."); return; }
            stopAnimation();
            runAnimation((int) speedSlider.getValue());
        });

        reportBtn.setOnAction(e -> {
            if (graph == null) { statusLabel.setText("Carregue um arquivo primeiro."); return; }
            gerarRelatorio();
        });

        resetBtn.setOnAction(e -> {
            stopAnimation();
            if (graph != null) { resetNodeColors(); resetEdgeColors(); }
            resultsArea.setText("Resetado. Rode novamente.");
            statusLabel.setText("Reset.");
        });

        backBtn.setOnAction(e -> { stopAnimation(); onBack.accept(null); });

        Scene scene = new Scene(root, 1280, 820);
        stage.setTitle("Grafo -- Visualizador (Formato A / B)");
        stage.setScene(scene);
    }

    // =========================================================================
    // Relatorio
    // =========================================================================
    private void gerarRelatorio() {
        int s = spinnerS.getValue();
        int t = spinnerT.getValue();
        int k = spinnerK.getValue();
        GraphAnalyzer analyzer = new GraphAnalyzer(graph);
        resultsArea.setText(analyzer.fullReport(s, t, k));
        statusLabel.setText("Relatorio gerado -- s=" + s + "  t=" + t + "  k=" + k);
    }

    // =========================================================================
    // Carregamento de arquivo
    // =========================================================================
    private void loadFile(File f) {
        try {
            GraphParser.ParseResult pr = GraphParser.parse(f);
            graph = pr.graph;
            int n = graph.vertexCount();

            spinnerS.setValueFactory(
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(0, n - 1, 0));
            spinnerT.setValueFactory(
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(0, n - 1, Math.min(n - 1, 1)));

            computeLayout();
            drawGraph();

            statusLabel.setText(pr.summary + "  |  " + f.getName());
            resultsArea.setText(pr.summary
                    + "\n\nGrafo carregado!\n"
                    + "Defina s, t e k e clique em:\n"
                    + "  [ Animar ]    -- visualiza BFS/DFS\n"
                    + "  [ Relatorio ] -- relatorio completo\n\n"
                    + "  k = niveis de vizinhanca para a Consulta 2");

        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro ao carregar arquivo");
            alert.setHeaderText("Falha no parse");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
            statusLabel.setText("Erro: " + ex.getMessage());
        }
    }

    // =========================================================================
    // Layout dos nos
    // =========================================================================
    private void computeLayout() {
        int n = graph.vertexCount();
        nx = new double[n];
        ny = new double[n];

        if (n == 1) { nx[0] = CANVAS_W / 2; ny[0] = CANVAS_H / 2; return; }

        if (n <= 60) {
            double cx = CANVAS_W / 2, cy = CANVAS_H / 2;
            double r  = Math.min(CANVAS_W, CANVAS_H) / 2.0 - 70;
            for (int i = 0; i < n; i++) {
                double angle = 2 * Math.PI * i / n - Math.PI / 2;
                nx[i] = cx + r * Math.cos(angle);
                ny[i] = cy + r * Math.sin(angle);
            }
        } else {
            int cols  = (int) Math.ceil(Math.sqrt(n));
            int rows  = (int) Math.ceil((double) n / cols);
            double sx = (CANVAS_W - 80) / (cols + 1);
            double sy = (CANVAS_H - 80) / (rows + 1);
            for (int i = 0; i < n; i++) {
                nx[i] = 40 + (i % cols + 1) * sx;
                ny[i] = 40 + (i / cols + 1) * sy;
            }
        }
    }

    // =========================================================================
    // Desenho do grafo
    // =========================================================================
    private void drawGraph() {
        canvas.getChildren().clear();
        edgeLines.clear();
        edgeArrows.clear();

        int    n        = graph.vertexCount();
        nodeCircles     = new Circle[n];
        nodeLabels      = new Text[n];
        double fontSize = Math.max(7, Math.min(13, 200.0 / n));

        for (Graph.Edge e : graph.allEdges()) {
            double x1 = nx[e.u], y1 = ny[e.u], x2 = nx[e.v], y2 = ny[e.v];
            Line line = new Line(x1, y1, x2, y2);
            line.setStroke(C_EDGE_DEFAULT);
            line.setStrokeWidth(1.5);
            edgeLines.add(line);
            canvas.getChildren().add(line);

            if (graph.getType() == Graph.Type.DIRECTED) {
                Polygon arrow = makeArrow(x1, y1, x2, y2, C_EDGE_DEFAULT);
                edgeArrows.add(arrow);
                canvas.getChildren().add(arrow);
            }
        }

        for (int i = 0; i < n; i++) {
            Circle c = new Circle(nx[i], ny[i], NODE_R);
            c.setFill(C_NODE_DEFAULT);
            c.setStroke(C_NODE_STROKE);
            c.setStrokeWidth(2);

            Text lbl = new Text(String.valueOf(i));
            lbl.setFont(Font.font("Consolas", fontSize));
            lbl.setFill(Color.web("#1e293b"));
            lbl.setX(nx[i] - lbl.getLayoutBounds().getWidth() / 2 - 2);
            lbl.setY(ny[i] + lbl.getLayoutBounds().getHeight() / 4);

            nodeCircles[i] = c;
            nodeLabels[i]  = lbl;
            canvas.getChildren().addAll(c, lbl);
        }
    }

    private Polygon makeArrow(double x1, double y1, double x2, double y2, Color fill) {
        double mx = (x1 + x2) / 2, my = (y1 + y2) / 2;
        double dx = x2 - x1, dy = y2 - y1;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len < 1e-6) return new Polygon();
        dx /= len; dy /= len;
        double nx_ = -dy, ny_ = dx, sz = 7;
        Polygon p = new Polygon(
                mx + dx * sz,                    my + dy * sz,
                mx - dx * sz + nx_ * sz * 0.5,   my - dy * sz + ny_ * sz * 0.5,
                mx - dx * sz - nx_ * sz * 0.5,   my - dy * sz - ny_ * sz * 0.5
        );
        p.setFill(fill);
        return p;
    }

    // =========================================================================
    // Animacao BFS / DFS
    // =========================================================================
    private void runAnimation(int msPerFrame) {
        int s = spinnerS.getValue(), t = spinnerT.getValue();
        boolean useBfs = ((RadioButton) algoToggle.getSelectedToggle()).getText().equals("BFS");

        GraphSearch.Result result = useBfs
                ? GraphSearch.bfs(graph, s, t)
                : GraphSearch.dfs(graph, s, t);

        visitOrder  = result.visitOrder;
        currentPath = result.path;
        animStep    = 0;

        gerarRelatorio();
        statusLabel.setText("Animando " + (useBfs ? "BFS" : "DFS") + "...");

        resetNodeColors();
        resetEdgeColors();
        colorNode(s, C_NODE_START);
        colorNode(t, C_NODE_GOAL);

        timeline = new Timeline(new KeyFrame(Duration.millis(msPerFrame), e -> animTick()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.playFromStart();
    }

    private void animTick() {
        int s = spinnerS.getValue(), t = spinnerT.getValue();
        if (animStep >= visitOrder.size()) {
            stopAnimation();
            paintFinalPath(s, t);
            statusLabel.setText("Animacao concluida.");
            return;
        }
        int v = visitOrder.get(animStep++);
        if (v != s && v != t) colorNode(v, C_NODE_VISITED);
        statusLabel.setText("Passo " + animStep + "/" + visitOrder.size()
                + " -- vertice " + v);
    }

    private void paintFinalPath(int s, int t) {
        if (currentPath == null || currentPath.isEmpty()) return;
        Set<Integer> pathSet = new HashSet<>(currentPath);
        for (int v : pathSet) if (v != s && v != t) colorNode(v, C_NODE_PATH);
        colorNode(s, C_NODE_START);
        colorNode(t, C_NODE_GOAL);

        List<Graph.Edge> all = graph.allEdges();
        for (int i = 0; i < currentPath.size() - 1; i++) {
            int u = currentPath.get(i), v = currentPath.get(i + 1);
            int idx = 0;
            for (Graph.Edge e : all) {
                boolean match = (e.u == u && e.v == v)
                        || (graph.getType() == Graph.Type.UNDIRECTED && e.u == v && e.v == u);
                if (match && idx < edgeLines.size()) {
                    edgeLines.get(idx).setStroke(C_EDGE_PATH);
                    edgeLines.get(idx).setStrokeWidth(3);
                    if (idx < edgeArrows.size()) edgeArrows.get(idx).setFill(C_EDGE_PATH);
                    break;
                }
                idx++;
            }
        }
    }

    private void stopAnimation() {
        if (timeline != null) { timeline.stop(); timeline = null; }
    }

    // =========================================================================
    // Cores
    // =========================================================================
    private void colorNode(int v, Color c) {
        if (nodeCircles != null && v >= 0 && v < nodeCircles.length)
            nodeCircles[v].setFill(c);
    }

    private void resetNodeColors() {
        if (nodeCircles != null) for (Circle c : nodeCircles) c.setFill(C_NODE_DEFAULT);
    }

    private void resetEdgeColors() {
        for (Line l : edgeLines) { l.setStroke(C_EDGE_DEFAULT); l.setStrokeWidth(1.5); }
        for (Polygon p : edgeArrows) p.setFill(C_EDGE_DEFAULT);
    }
}