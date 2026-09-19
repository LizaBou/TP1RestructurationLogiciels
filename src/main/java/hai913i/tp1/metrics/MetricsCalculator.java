package hai913i.tp1.metrics;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hai913i.tp1.model.ClassInfo;
import hai913i.tp1.model.MethodInfo;
import hai913i.tp1.model.ProjectModel;

/**
 * Etape B2 : metriques de base (questions 1 a 7 du sujet).
 *
 * Aucune classe org.eclipse.jdt n'apparait ici : tout est calcule a partir
 * du ProjectModel (donc indirectement des fichiers .java pour la question 2
 * uniquement, qui doit compter des lignes physiques de fichier).
 */
public class MetricsCalculator {

    private final ProjectModel model;

    public MetricsCalculator(ProjectModel model) {
        this.model = model;
    }

    /** Q1 : nombre de classes (au sens large : classes+interfaces+enum). */
    public int classCount() {
        return model.getClasses().size();
    }

    /** Q2 : nombre de lignes de code de l'application (lignes physiques des .java analyses). */
    public long applicationLineCount(List<Path> javaFiles) throws Exception {
        long total = 0;
        for (Path p : javaFiles) {
            total += Files.readAllLines(p).size();
        }
        return total;
    }

    /** Q3 : nombre total de methodes. */
    public int totalMethodCount() {
        int total = 0;
        for (ClassInfo c : model.getClasses()) {
            total += c.getMethods().size();
        }
        return total;
    }

    /** Q4 : nombre total de paquetages distincts. */
    public int packageCount() {
        Set<String> packages = new HashSet<>();
        for (ClassInfo c : model.getClasses()) {
            packages.add(c.getPackageName());
        }
        return packages.size();
    }

    /** Q5 : nombre moyen de methodes par classe. */
    public double averageMethodsPerClass() {
        if (classCount() == 0) return 0.0;
        return (double) totalMethodCount() / classCount();
    }

    /** Q6 : nombre moyen de lignes de code par methode (methodes ayant un corps).
     *  NB : necessite les lignes de chaque methode, calculees en A3/A2 -> a
     *  fournir ici via une liste externe (voir MethodLineInfo plus bas). */
    public double averageLinesPerMethod(List<Integer> methodBodyLineCounts) {
        int withBody = 0;
        long totalLines = 0;
        for (int lines : methodBodyLineCounts) {
            if (lines > 0) {
                withBody++;
                totalLines += lines;
            }
        }
        if (withBody == 0) return 0.0;
        return (double) totalLines / withBody;
    }

    /** Q7 : nombre moyen d'attributs par classe. */
    public double averageFieldsPerClass() {
        if (classCount() == 0) return 0.0;
        int totalFields = 0;
        for (ClassInfo c : model.getClasses()) {
            totalFields += c.getFields().size();
        }
        return (double) totalFields / classCount();
    }
}