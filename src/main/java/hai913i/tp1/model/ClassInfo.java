package hai913i.tp1.model;

import java.util.ArrayList;
import java.util.List;

public class ClassInfo {
    private final String qualifiedName;
    private final String kind; // "class", "interface", "enum"
    private final String packageName;
    private final List<String> superclasses = new ArrayList<>();
    private final List<String> interfaces = new ArrayList<>();
    private final List<FieldInfo> fields = new ArrayList<>();
    private final List<MethodInfo> methods = new ArrayList<>();

    public ClassInfo(String qualifiedName, String kind, String packageName) {
        this.qualifiedName = qualifiedName;
        this.kind = kind;
        this.packageName = packageName;
    }

    public String getQualifiedName() { return qualifiedName; }
    public String getKind() { return kind; }
    public String getPackageName() { return packageName; }
    public List<String> getSuperclasses() { return superclasses; }
    public List<String> getInterfaces() { return interfaces; }
    public List<FieldInfo> getFields() { return fields; }
    public List<MethodInfo> getMethods() { return methods; }

    @Override
    public String toString() {
        return kind + " " + qualifiedName + " [pkg=" + packageName
                + ", superclasses=" + superclasses
                + ", interfaces=" + interfaces
                + ", attributs=" + fields.size()
                + ", methodes=" + methods.size() + "]";
    }
}