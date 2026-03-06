package model;

public class Poi {
    private final Cell entrance;
    private final PoiType type;
    private final String name;
    private final int buildingId;

    public Poi(Cell entrance, PoiType type, String name, int buildingId) {
        this.entrance = entrance;
        this.type = type;
        this.name = name;
        this.buildingId = buildingId;
    }

    public Cell getEntrance() { return entrance; }
    public PoiType getType() { return type; }
    public String getName() { return name; }
    public int getBuildingId() { return buildingId; }
}