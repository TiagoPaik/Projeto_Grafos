package algo;

import model.Cell;

import java.util.ArrayList;
import java.util.List;

public class SearchStep {
    public final List<Cell> visitedNow = new ArrayList<>();
    public final List<Cell> frontierAddedNow = new ArrayList<>();

    public boolean finished = false;
    public boolean foundGoal = false;

    public int distToGoal = -1;
    public List<Cell> path = null;

    public String message = "";
}