package app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.function.Consumer;

/**
 * Menu principal do projeto.
 *
 * Permite escolher entre:
 *   (1) Cidade 100×100 gerada internamente (BFS / DFS visual no grid)
 *   (2) Carregar grafo de arquivo .txt (Formato A ou B da prova)
 */
public class MenuView {

    public enum Algo { BFS, DFS }
    public enum Rep  { MATRIZ_ADJ, LISTA_INCIDENCIA }

    public record Config(Algo algo, Rep rep) {}

    /**
     * @param onStartCity  callback quando usuário escolhe a cidade 100×100
     * @param onLoadFile   callback quando usuário quer carregar arquivo
     */
    public static Parent build(Consumer<Config> onStartCity, Runnable onLoadFile) {

        // ---- Título ----
        Label title = new Label("Projeto Grafos");
        title.setFont(Font.font("System Bold", 22));
        title.setTextFill(Color.web("#1e293b"));

        Label sub = new Label("Escolha o modo de entrada:");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569;");

        // ---- Seção 1: Cidade 100×100 ----
        Label secCity = new Label("Modo 1 — Cidade 100 × 100 (entrada interna)");
        secCity.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #334155;");

        ChoiceBox<Algo> algoBox = new ChoiceBox<>();
        algoBox.getItems().addAll(Algo.BFS, Algo.DFS);
        algoBox.setValue(Algo.BFS);

        ChoiceBox<Rep> repBox = new ChoiceBox<>();
        repBox.getItems().addAll(Rep.LISTA_INCIDENCIA, Rep.MATRIZ_ADJ);
        repBox.setValue(Rep.LISTA_INCIDENCIA);

        Button startCityBtn = new Button("▶  Abrir Cidade");
        startCityBtn.setStyle("""
                -fx-font-weight: bold;
                -fx-background-color: #4f46e5;
                -fx-text-fill: white;
                -fx-padding: 8 20;
                -fx-background-radius: 6;
                """);
        startCityBtn.setOnAction(e ->
                onStartCity.accept(new Config(algoBox.getValue(), repBox.getValue())));

        GridPane cityGrid = new GridPane();
        cityGrid.setHgap(10);
        cityGrid.setVgap(8);
        cityGrid.add(new Label("Algoritmo:"),     0, 0);
        cityGrid.add(algoBox,                     1, 0);
        cityGrid.add(new Label("Representação:"), 0, 1);
        cityGrid.add(repBox,                      1, 1);
        cityGrid.add(startCityBtn,                1, 2);

        // ---- Separador ----
        Separator sep = new Separator();
        sep.setPadding(new Insets(4, 0, 4, 0));

        // ---- Seção 2: Arquivo ----
        Label secFile = new Label("Modo 2 — Carregar arquivo .txt (Formato A ou B)");
        secFile.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #334155;");

        Label fmtHint = new Label(
                "Formato A (lista de adjacência):\n" +
                        "  Linha 1:  n  m  tipo        (tipo = D ou U)\n" +
                        "  Linhas 2..m+1:  u  v  [w]\n\n" +
                        "Formato B (matriz de adjacência):\n" +
                        "  Linha 1:  n  tipo\n" +
                        "  Linhas 2..n+1:  n valores (0/1 ou pesos)\n"
        );
        fmtHint.setStyle("-fx-font-family: Consolas; -fx-font-size: 11px; -fx-text-fill: #64748b;");

        Button loadFileBtn = new Button("📂  Carregar arquivo .txt");
        loadFileBtn.setStyle("""
                -fx-font-weight: bold;
                -fx-background-color: #0ea5e9;
                -fx-text-fill: white;
                -fx-padding: 8 20;
                -fx-background-radius: 6;
                """);
        loadFileBtn.setOnAction(e -> onLoadFile.run());

        // ---- Layout ----
        VBox box = new VBox(14,
                title, sub,
                new Separator(),
                secCity, cityGrid,
                sep,
                secFile, fmtHint, loadFileBtn
        );
        box.setPadding(new Insets(28, 32, 28, 32));
        box.setAlignment(Pos.TOP_LEFT);
        box.setStyle("-fx-background-color: #ffffff;");
        box.setPrefWidth(520);
        return box;
    }

    /** Overload de compatibilidade para código existente que usa só 1 argumento */
    @Deprecated
    public static Parent build(Consumer<Config> onStartCity) {
        return build(onStartCity, () -> {});
    }
}