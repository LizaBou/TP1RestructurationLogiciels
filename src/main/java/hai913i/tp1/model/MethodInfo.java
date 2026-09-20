package hai913i.tp1.model;

public class MethodInfo {
    private final String name;
    private final int parameterCount;
    private final boolean constructor;
    private final int lineCount;
    private final String signature; // nom(TypesDesParametres), pour identifier un noeud du graphe

    public MethodInfo(String name, int parameterCount, boolean constructor, int lineCount, String signature) {
        this.name = name;
        this.parameterCount = parameterCount;
        this.constructor = constructor;
        this.lineCount = lineCount;
        this.signature = signature;
    }

    public String getName() { return name; }
    public int getParameterCount() { return parameterCount; }
    public boolean isConstructor() { return constructor; }
    public int getLineCount() { return lineCount; }
    public String getSignature() { return signature; }

    @Override
    public String toString() {
        return (constructor ? "<constructeur> " : "") + name + "(" + parameterCount + " param., " + lineCount + " lignes)";
    }
}