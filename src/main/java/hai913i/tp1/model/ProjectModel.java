package hai913i.tp1.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Etape B1 : modele de faits complet et independant de JDT.
 *
 * A partir d'ici, aucune classe org.eclipse.jdt ne doit plus etre necessaire :
 * tout le calcul de metriques et la construction du graphe d'appel se font
 * uniquement a partir des objets contenus ici.
 *
 * Les listes sont triees de maniere deterministe (ordre alphabetique des noms
 * qualifies) afin que deux executions successives sur le meme projet
 * produisent exactement la meme sortie, quel que soit l'ordre dans lequel
 * JDT a livre les fichiers.
 */
public class ProjectModel {

    private final List<ClassInfo> classes;
    private final List<CallRecord> calls;

    public ProjectModel(List<ClassInfo> classes, List<CallRecord> calls) {
        this.classes = new ArrayList<>(classes);
        this.calls = new ArrayList<>(calls);
        sortForDeterminism();
    }

    private void sortForDeterminism() {
        classes.sort(Comparator.comparing(ClassInfo::getQualifiedName));
        for (ClassInfo c : classes) {
            c.getFields().sort(Comparator.comparing(FieldInfo::getName));
            c.getMethods().sort(Comparator.comparing(MethodInfo::getName)
                    .thenComparingInt(MethodInfo::getParameterCount));
        }
        calls.sort(Comparator
                .comparing(CallRecord::getCallerClass)
                .thenComparing(CallRecord::getCallerMethod)
                .thenComparingInt(CallRecord::getLine));
    }

    public List<ClassInfo> getClasses() {
        return classes;
    }

    public List<CallRecord> getCalls() {
        return calls;
    }

    /** Retrouve une classe par son nom qualifie, ou null si absente. */
    public ClassInfo findClass(String qualifiedName) {
        for (ClassInfo c : classes) {
            if (c.getQualifiedName().equals(qualifiedName)) {
                return c;
            }
        }
        return null;
    }
}