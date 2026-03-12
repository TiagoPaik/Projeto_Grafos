package app;

import algo.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import model.Cell;
import model.GridMap;
import model.Poi;
import model.PoiType;
import util.CityGridGenerator;
import util.PoiGenerator;
import app.GraphView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainApp extends Application {

    // ===== Visual =====
    private static final int CELL_SIZE = 7;

    private static final Color FREE = Color.web("#f6f7fb");
    private static final Color WALL = Color.web("#1f2937");
    private static final Color VISITED = Color.web("#60a5fa");
    private static final Color FRONTIER = Color.web("#fbbf24");
    private static final Color START = Color.web("#22c55e");
    private static final Color GOAL = Color.web("#ef4444");
    private static final Color PATH = Color.web("#a78bfa");

    private static final Color POI_CASA = Color.web("#93c5fd");
    private static final Color POI_ESCOLA = Color.web("#fde68a");
    private static final Color POI_HOSPITAL = Color.web("#fecaca");
    private static final Color POI_FACULDADE = Color.web("#ddd6fe");
    private static final Color POI_MERCADO = Color.web("#bbf7d0");

    // ===== Seeds / City =====
    private final long seedCity = 12345L;
    private final long seedPois = 777L;
    private final int[][] grid = CityGridGenerator.generate(100, 100, seedCity);

    // ===== Start / Goal =====
    private final Cell start = new Cell(0, 0);
    private Cell goal = new Cell(99, 99);

    // ===== Runtime =====
    private GridMap map;
    private Rectangle[][] cells;
    private Timeline timeline;
    private SearchAlgorithm algo;

    // ===== UI =====
    private Label status;
    private TextArea results;
    private ChoiceBox<Poi> destinoBox;

    // ===== POIs =====
    private List<Poi> pois;
    private final Map<Cell, Poi> poiByCell = new HashMap<>();
    private int buildingCount = 0;

    // ===== Filter =====
    private boolean showCasa = false;
    private boolean showEscola = true;
    private boolean showHospital = true;
    private boolean showFaculdade = true;
    private boolean showMercado = true;

    private boolean isPoiVisible(Poi p) {
        return switch (p.getType()) {
            case CASA -> showCasa;
            case ESCOLA -> showEscola;
            case HOSPITAL -> showHospital;
            case FACULDADE -> showFaculdade;
            case MERCADO -> showMercado;
        };
    }

    @Override
    public void start(Stage stage) {
        showMenu(stage);
    }

    private void showMenu(Stage stage) {
        Scene menuScene = new Scene(
                MenuView.build(
                        // Modo 1: cidade 100x100
                        cfg -> {
                            Scene vizScene = new Scene(buildVisualizer(stage, cfg), 1280, 820);
                            stage.setTitle("Cidade 100x100 — Grafos");
                            stage.setScene(vizScene);
                        },
                        // Modo 2: carregar arquivo Formato A ou B
                         () -> GraphView.show(stage, _v -> showMenu(stage))
                ),
                560, 440
        );
        stage.setTitle("Projeto Grafos");
        stage.setScene(menuScene);
        stage.show();
    }

    private Parent buildVisualizer(Stage stage, MenuView.Config cfg) {
        // garante start/goal livres
        grid[start.r][start.c] = 1;
        grid[goal.r][goal.c] = 1;

        map = new GridMap(grid);

        // POIs (1 por prédio)
        PoiGenerator.Result res = PoiGenerator.generateOnePerBuilding(grid, seedPois);
        pois = res.pois;
        buildingCount = res.buildingCount;

        poiByCell.clear();
        for (Poi p : pois) poiByCell.put(p.getEntrance(), p);

        // ===== Grid UI =====
        int R = map.rows(), C = map.cols();
        cells = new Rectangle[R][C];

        GridPane gridPane = new GridPane();
        gridPane.setHgap(1);
        gridPane.setVgap(1);
        gridPane.setPadding(new Insets(12));
        gridPane.setAlignment(Pos.CENTER);

        for (int r = 0; r < R; r++) {
            for (int c = 0; c < C; c++) {
                Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE);
                rect.setArcWidth(4);
                rect.setArcHeight(4);
                rect.setStroke(Color.web("#e5e7eb"));
                rect.setStrokeWidth(0.5);

                final int rr = r, cc = c;
                rect.setOnMouseClicked(e -> onCellClick(rr, cc));

                cells[r][c] = rect;
                gridPane.add(rect, c, r);
            }
        }

        ScrollPane centerScroll = new ScrollPane(gridPane);
        centerScroll.setFitToWidth(true);
        centerScroll.setFitToHeight(true);

        // ===== Right panel: Results =====
        results = new TextArea();
        results.setEditable(false);
        results.setWrapText(true);
        results.setPrefWidth(380);
        results.setStyle("-fx-font-family: Consolas; -fx-font-size: 12px;");
        results.setText("=== RESULTADOS ===\nSelecione um destino e rode o algoritmo.");

        Label resultsTitle = new Label("Relatorio");
        resultsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        VBox rightPanel = new VBox(10, resultsTitle, results);
        rightPanel.setPadding(new Insets(12));
        rightPanel.setPrefWidth(400);
        rightPanel.setStyle("""
                -fx-background-color: #ffffff;
                -fx-border-color: #e5e7eb;
                -fx-border-width: 0 0 0 1;
                """);

        // ===== Bottom bar: nicer + taller =====
        status = new Label();
        status.setStyle("-fx-font-size: 13px; -fx-text-fill: #111827;");

        Button runBtn = new Button("Rodar");
        Button resetBtn = new Button("Reset");
        Button backBtn = new Button("Menu");

        runBtn.setStyle("-fx-font-weight: bold;");
        resetBtn.setStyle("-fx-font-weight: bold;");
        backBtn.setStyle("-fx-font-weight: bold;");

        ToggleGroup tg = new ToggleGroup();
        RadioButton bfsBtn = new RadioButton("BFS");
        RadioButton dfsBtn = new RadioButton("DFS");
        bfsBtn.setToggleGroup(tg);
        dfsBtn.setToggleGroup(tg);

        bfsBtn.setSelected(cfg.algo() == MenuView.Algo.BFS);
        dfsBtn.setSelected(cfg.algo() == MenuView.Algo.DFS);

        Slider speed = new Slider(1, 120, 12);
        speed.setPrefWidth(180);
        speed.setShowTickMarks(true);
        speed.setShowTickLabels(true);
        speed.setMajorTickUnit(30);
        speed.setMinorTickCount(2);

        // destino
        destinoBox = new ChoiceBox<>();
        destinoBox.setPrefWidth(320);
        destinoBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Poi poi) {
                if (poi == null) return "";
                Cell e = poi.getEntrance();
                return poi.getName() + " (" + poi.getType() + ") [" + e.r + "," + e.c + "]";
            }
            @Override
            public Poi fromString(String s) { return null; }
        });
        destinoBox.setOnAction(e -> {
            Poi selected = destinoBox.getValue();
            if (selected == null) return;
            setGoalToPoi(selected);
        });

        // filtro
        CheckBox cbCasa = new CheckBox("Casa");
        CheckBox cbEscola = new CheckBox("Escola");
        CheckBox cbHospital = new CheckBox("Hospital");
        CheckBox cbFaculdade = new CheckBox("Faculdade");
        CheckBox cbMercado = new CheckBox("Mercado");

        cbCasa.setSelected(showCasa);
        cbEscola.setSelected(showEscola);
        cbHospital.setSelected(showHospital);
        cbFaculdade.setSelected(showFaculdade);
        cbMercado.setSelected(showMercado);

        cbCasa.selectedProperty().addListener((o, ov, nv) -> { showCasa = nv; paintBase(); refreshDestinoBox(); });
        cbEscola.selectedProperty().addListener((o, ov, nv) -> { showEscola = nv; paintBase(); refreshDestinoBox(); });
        cbHospital.selectedProperty().addListener((o, ov, nv) -> { showHospital = nv; paintBase(); refreshDestinoBox(); });
        cbFaculdade.selectedProperty().addListener((o, ov, nv) -> { showFaculdade = nv; paintBase(); refreshDestinoBox(); });
        cbMercado.selectedProperty().addListener((o, ov, nv) -> { showMercado = nv; paintBase(); refreshDestinoBox(); });

        // Linha principal de controles (mais espaçada)
        HBox topControls = new HBox(
                10,
                backBtn, runBtn, resetBtn,
                new Separator(),
                bfsBtn, dfsBtn,
                new Separator(),
                new Label("Destino:"), destinoBox,
                new Separator(),
                new Label("Velocidade:"), speed
        );
        topControls.setAlignment(Pos.CENTER_LEFT);

        // Linha do filtro
        HBox filterRow = new HBox(10, new Label("Filtro:"), cbCasa, cbEscola, cbHospital, cbFaculdade, cbMercado);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        // Linha de status (em baixo)
        HBox statusRow = new HBox(status);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        VBox bottomBar = new VBox(8, topControls, filterRow, statusRow);
        bottomBar.setPadding(new Insets(10, 12, 12, 12));
        bottomBar.setStyle("""
                -fx-background-color: #f9fafb;
                -fx-border-color: #e5e7eb;
                -fx-border-width: 1 0 0 0;
                """);

        // ===== Root =====
        BorderPane root = new BorderPane();
        root.setCenter(centerScroll);
        root.setRight(rightPanel);
        root.setBottom(bottomBar);
        root.setStyle("-fx-background-color: #ffffff;");

        // ===== Init =====
        paintBase();
        refreshDestinoBox();
        initAlgorithm(bfsBtn.isSelected());
        status.setText(baseStatus("Pronto. Selecione destino e rode."));

        // ===== Handlers =====
        runBtn.setOnAction(e -> runAnimation((int) speed.getValue()));

        resetBtn.setOnAction(e -> {
            stopAnimation();
            paintBase();
            refreshDestinoBox();
            initAlgorithm(bfsBtn.isSelected());
            results.setText("=== RESULTADOS ===\nSelecione um destino e rode o algoritmo.");
            status.setText(baseStatus("Resetado."));
        });

        tg.selectedToggleProperty().addListener((obs, oldV, newV) -> {
            stopAnimation();
            paintBase();
            refreshDestinoBox();
            initAlgorithm(bfsBtn.isSelected());
            results.setText("=== RESULTADOS ===\nAlgoritmo alterado. Rode novamente.");
            status.setText(baseStatus("Trocou algoritmo."));
        });

        backBtn.setOnAction(e -> { stopAnimation(); showMenu(stage); });

        return root;
    }

    private void refreshDestinoBox() {
        if (destinoBox == null) return;

        Poi selected = destinoBox.getValue();
        destinoBox.getItems().clear();

        for (Poi p : pois) {
            if (isPoiVisible(p)) destinoBox.getItems().add(p);
        }

        if (selected != null && destinoBox.getItems().contains(selected)) {
            destinoBox.setValue(selected);
        } else if (!destinoBox.getItems().isEmpty()) {
            destinoBox.setValue(destinoBox.getItems().get(0));
        }
    }

    private String baseStatus(String prefix) {
        int poiCount = (pois == null) ? 0 : pois.size();
        return prefix + "  |  Prédios=" + buildingCount + "  |  POIs=" + poiCount + "  |  Destino=" + goal;
    }

    private void setGoalToPoi(Poi p) {
        stopAnimation();
        goal = p.getEntrance();
        paintBase();

        boolean useBfs = (algo instanceof BfsAlgorithm);
        initAlgorithm(useBfs);

        results.setText("=== RESULTADOS ===\nDestino escolhido: " + p.getName() + "\nAgora clique em  Rodar.");
        status.setText(baseStatus("Destino: " + p.getName() + " (" + p.getType() + ")."));
    }

    private void onCellClick(int r, int c) {
        Cell clicked = new Cell(r, c);
        Poi poi = poiByCell.get(clicked);

        if (poi == null || !isPoiVisible(poi)) return;

        if (destinoBox != null) destinoBox.setValue(poi);
        setGoalToPoi(poi);
    }

    private void initAlgorithm(boolean useBfs) {
        algo = useBfs ? new BfsAlgorithm() : new DfsAlgorithm();
        algo.init(map, start, goal);
    }

    private void paintBase() {
        for (int r = 0; r < map.rows(); r++) {
            for (int c = 0; c < map.cols(); c++) {
                cells[r][c].setFill(map.isWall(r, c) ? WALL : FREE);
            }
        }

        for (Poi p : pois) {
            if (!isPoiVisible(p)) continue;
            Cell e = p.getEntrance();
            if (e.equals(start) || e.equals(goal)) continue;
            cells[e.r][e.c].setFill(colorForPoi(p.getType()));
        }

        cells[start.r][start.c].setFill(START);
        cells[goal.r][goal.c].setFill(GOAL);
    }

    private Color colorForPoi(PoiType t) {
        return switch (t) {
            case CASA -> POI_CASA;
            case ESCOLA -> POI_ESCOLA;
            case HOSPITAL -> POI_HOSPITAL;
            case FACULDADE -> POI_FACULDADE;
            case MERCADO -> POI_MERCADO;
        };
    }

    private void runAnimation(int msPerFrame) {
        stopAnimation();
        timeline = new Timeline(new KeyFrame(Duration.millis(msPerFrame), e -> doStep()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.playFromStart();
        status.setText(baseStatus("Rodando..."));
    }

    private void stopAnimation() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }

    private void doStep() {
        SearchStep s = algo.step();

        for (Cell v : s.visitedNow) {
            if (!v.equals(start) && !v.equals(goal)) cells[v.r][v.c].setFill(VISITED);
        }
        for (Cell f : s.frontierAddedNow) {
            if (!f.equals(start) && !f.equals(goal)) cells[f.r][f.c].setFill(FRONTIER);
        }

        // repinta POIs por cima
        for (Poi p : pois) {
            if (!isPoiVisible(p)) continue;
            Cell e = p.getEntrance();
            if (e.equals(start) || e.equals(goal)) continue;
            cells[e.r][e.c].setFill(colorForPoi(p.getType()));
        }

        cells[start.r][start.c].setFill(START);
        cells[goal.r][goal.c].setFill(GOAL);

        status.setText(baseStatus(s.message));

        if (s.finished) {
            stopAnimation();

            if (s.foundGoal) {
                List<Cell> path = (s.path != null) ? s.path : algo.getPath();

                // pinta caminho
                if (path != null) {
                    for (Cell p : path) {
                        if (!p.equals(start) && !p.equals(goal)) cells[p.r][p.c].setFill(PATH);
                    }
                }

                // calcula resultados com pesos (100m por aresta + tempo por aresta no GridMap)
                int hops = (path == null) ? 0 : (path.size() - 1);
                int totalMeters = hops * GridMap.EDGE_DISTANCE_METERS;

                double totalSeconds = 0.0;
                if (path != null) {
                    for (int i = 0; i < path.size() - 1; i++) {
                        Cell a = path.get(i);
                        Cell b = path.get(i + 1);
                        totalSeconds += map.edgeTimeSeconds(a.r, a.c, b.r, b.c);
                    }
                }

                results.setText(buildResultReport(hops, totalMeters, totalSeconds, path));
                status.setText(baseStatus("Finalizado: encontrou caminho."));
            } else {
                results.setText("=== RESULTADOS (s -> t) ===\nSem caminho até o destino.\n");
                status.setText(baseStatus("Finalizado: sem caminho."));
            }
        }
    }

    private String buildResultReport(int hops, int totalMeters, double totalSeconds, List<Cell> path) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== RESULTADOS (s -> t) ===\n");
        sb.append("Algoritmo: ").append((algo instanceof BfsAlgorithm) ? "BFS" : "DFS").append("\n");
        sb.append("Hops: ").append(hops).append("\n");
        sb.append("Distância: ").append(totalMeters).append(" m (")
                .append(String.format("%.2f", totalMeters / 1000.0)).append(" km)\n");
        sb.append("Tempo: ").append(String.format("%.1f", totalSeconds)).append(" s (")
                .append(String.format("%.1f", totalSeconds / 60.0)).append(" min)\n\n");

        sb.append("Origem s: ").append(start).append("\n");
        sb.append("Destino t: ").append(goal).append("\n\n");

        sb.append("Caminho (").append((path == null) ? 0 : path.size()).append(" vértices):\n");
        if (path == null || path.isEmpty()) {
            sb.append("  (sem caminho)\n");
        } else {
            for (int i = 0; i < path.size(); i++) {
                sb.append(path.get(i));
                if (i < path.size() - 1) sb.append(" -> ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        launch(args);
    }
}