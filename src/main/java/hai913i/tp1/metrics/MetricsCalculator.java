package hai913i.tp1.metrics;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hai913i.tp1.model.ClassInfo;
import hai913i.tp1.model.MethodInfo;
import hai913i.tp1.model.ProjectModel;

/**
 * Etape B2 : toutes les metriques du sujet (questions 1 a 13), calculees
 * exclusivement a partir du ProjectModel (aucune classe org.eclipse.jdt).
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

    /** Q6 : nombre moyen de lignes de code par methode (methodes ayant un corps). */
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

    /** Q8 : les 10% de classes qui possedent le plus de methodes. */
    public List<ClassInfo> topClassesByMethodCount() {
        return TopTenPercent.select(model.getClasses(), c -> c.getMethods().size());
    }

    /** Q9 : les 10% de classes qui possedent le plus d'attributs. */
    public List<ClassInfo> topClassesByFieldCount() {
        return TopTenPercent.select(model.getClasses(), c -> c.getFields().size());
    }

    /** Q10 : classes appartenant a la fois a Q8 et Q9. */
    public List<ClassInfo> classesInBothTopCategories() {
        Set<String> namesInQ9 = new HashSet<>();
        for (ClassInfo c : topClassesByFieldCount()) {
            namesInQ9.add(c.getQualifiedName());
        }
        List<ClassInfo> result = new ArrayList<>();
        for (ClassInfo c : topClassesByMethodCount()) {
            if (namesInQ9.contains(c.getQualifiedName())) {
                result.add(c);
            }
        }
        return result;
    }

    /** Q11 : classes possedant strictement plus de X methodes. */
    public List<ClassInfo> classesAboveMethodThreshold(int x) {
        List<ClassInfo> result = new ArrayList<>();
        for (ClassInfo c : model.getClasses()) {
            if (c.getMethods().size() > x) {
                result.add(c);
            }
        }
        return result;
    }

    /** Q12 : pour chaque classe, les 10% de ses methodes avec le plus de lignes. */
    public Map<String, List<MethodInfo>> topMethodsPerClass() {
        Map<String, List<MethodInfo>> result = new LinkedHashMap<>();
        for (ClassInfo c : model.getClasses()) {
            List<MethodInfo> top = TopTenPercent.select(c.getMethods(), MethodInfo::getLineCount);
            result.put(c.getQualifiedName(), top);
        }
        return result;
    }

    /** Petit couple (classe, methode) pour representer le resultat de Q13. */
    public record ClassMethod(String className, MethodInfo method) {
    }

    /** Q13 : nombre maximal de parametres, et les methodes concernees. */
    public List<ClassMethod> methodsWithMaxParameters() {
        int max = -1;
        for (ClassInfo c : model.getClasses()) {
            for (MethodInfo m : c.getMethods()) {
                max = Math.max(max, m.getParameterCount());
            }
        }
        List<ClassMethod> result = new ArrayList<>();
        for (ClassInfo c : model.getClasses()) {
            for (MethodInfo m : c.getMethods()) {
                if (m.getParameterCount() == max) {
                    result.add(new ClassMethod(c.getQualifiedName(), m));
                }
            }
        }
        return result;
    }
}