package model;

import java.util.Objects;

public class Cell {
    public final int r;
    public final int c;

    public Cell(int r, int c) {
        this.r = r;
        this.c = c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cell)) return false;
        Cell cell = (Cell) o;
        return r == cell.r && c == cell.c;
    }

    @Override
    public int hashCode() {
        return Objects.hash(r, c);
    }

    @Override
    public String toString() {
        return "(" + r + "," + c + ")";
    }
}