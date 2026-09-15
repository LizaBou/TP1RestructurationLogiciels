package hai913i.tp1.model;

public class MethodInfo {
    private final String name;
    private final int parameterCount;
    private final boolean constructor;

    public MethodInfo(String name, int parameterCount, boolean constructor) {
        this.name = name;
        this.parameterCount = parameterCount;
        this.constructor = constructor;
    }

    public String getName() { return name; }
    public int getParameterCount() { return parameterCount; }
    public boolean isConstructor() { return constructor; }

    @Override
    public String toString() {
        return (constructor ? "<constructeur> " : "") + name + "(" + parameterCount + " param.)";
    }
}