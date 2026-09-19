package hai913i.tp1.model;

public class MethodInfo {
    private final String name;
    private final int parameterCount;
    private final boolean constructor;
    private final int lineCount; // nouveau : lignes du corps, 0 si pas de corps

    public MethodInfo(String name, int parameterCount, boolean constructor, int lineCount) {
        this.name = name;
        this.parameterCount = parameterCount;
        this.constructor = constructor;
        this.lineCount = lineCount;
    }

    public String getName() { return name; }
    public int getParameterCount() { return parameterCount; }
    public boolean isConstructor() { return constructor; }
    public int getLineCount() { return lineCount; }

    @Override
    public String toString() {
        return (constructor ? "<constructeur> " : "") + name + "(" + parameterCount + " param., " + lineCount + " lignes)";
    }
}