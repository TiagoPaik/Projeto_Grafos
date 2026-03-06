package algo;

import model.Cell;
import model.GridMap;

import java.util.List;

public interface SearchAlgorithm {
    void init(GridMap map, Cell start, Cell goal);
    SearchStep step();
    boolean isFinished();
    List<Cell> getPath();
    int getDistToGoal();
}