package app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class MenuView {

    public enum Algo { BFS, DFS }
    public enum Rep  { MATRIZ_ADJ, LISTA_INCIDENCIA }

    public record Config(Algo algo, Rep rep) {}

    public static Parent build(java.util.function.Consumer<Config> onStart) {
        Label title = new Label("Menu - Cidade (Grafos)");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ChoiceBox<Algo> algo = new ChoiceBox<>();
        algo.getItems().addAll(Algo.BFS, Algo.DFS);
        algo.setValue(Algo.BFS);

        ChoiceBox<Rep> rep = new ChoiceBox<>();
        rep.getItems().addAll(Rep.MATRIZ_ADJ, Rep.LISTA_INCIDENCIA);
        rep.setValue(Rep.LISTA_INCIDENCIA);

        Button start = new Button("Iniciar");
        start.setOnAction(e -> onStart.accept(new Config(algo.getValue(), rep.getValue())));

        VBox box = new VBox(12,
                title,
                new Label("Algoritmo:"), algo,
                new Label("Representação:"), rep,
                start
        );
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }
}