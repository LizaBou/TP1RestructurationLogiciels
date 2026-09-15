package hai913i.tp1.model;

public class FieldInfo {
    private final String name;
    private final String type;
    private final String visibility;

    public FieldInfo(String name, String type, String visibility) {
        this.name = name;
        this.type = type;
        this.visibility = visibility;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public String getVisibility() { return visibility; }

    @Override
    public String toString() {
        return visibility + " " + type + " " + name;
    }
}