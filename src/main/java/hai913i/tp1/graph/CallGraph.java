package hai913i.tp1.graph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import hai913i.tp1.model.CallRecord;
import hai913i.tp1.model.ClassInfo;
import hai913i.tp1.model.MethodInfo;
import hai913i.tp1.model.ProjectModel;

/**
 * Etape B3 : graphe d'appel du projet, construit uniquement a partir du
 * ProjectModel (donc aucune classe org.eclipse.jdt ici non plus).
 *
 * Noeuds : toutes les methodes comptees du projet, identifiees par
 * "classe#nom(TypesParams)" (les surcharges sont donc des noeuds distincts).
 *
 * Arcs : un arc f -> g existe si le corps de f contient au moins un appel
 * interne et resolu dont la cible est g. Plusieurs sites d'appel de f vers g
 * donnent un seul arc, dont le poids est le nombre de sites (§4.4).
 */
public class CallGraph {

    private final List<String> nodes = new ArrayList<>();
    // adjacence : source -> (cible -> poids), TreeMap pour un ordre deterministe
    private final Map<String, Map<String, Integer>> edges = new TreeMap<>();
    // graphe inverse, construit en meme temps, pour l'analyse d'impact
    private final Map<String, Map<String, Integer>> reverseEdges = new TreeMap<>();

    private int internalCallSites = 0;
    private int externalCalls = 0;
    private int unresolvedCalls = 0;

    public CallGraph(ProjectModel model) {
        buildNodes(model);
        buildEdges(model);
    }

    private void buildNodes(ProjectModel model) {
        for (ClassInfo c : model.getClasses()) {
            for (MethodInfo m : c.getMethods()) {
                nodes.add(c.getQualifiedName() + "#" + m.getSignature());
            }
        }
    }

    private void buildEdges(ProjectModel model) {
        for (CallRecord call : model.getCalls()) {
            if (!call.isResolved()) {
                unresolvedCalls++;
                continue;
            }
            if (call.isExternal()) {
                externalCalls++;
                continue;
            }
            internalCallSites++;
            String source = call.getCallerNodeId();
            String target = call.getTargetSignature();

            edges.computeIfAbsent(source, k -> new TreeMap<>())
                    .merge(target, 1, Integer::sum);
            reverseEdges.computeIfAbsent(target, k -> new TreeMap<>())
                    .merge(source, 1, Integer::sum);
        }
    }

    public int nodeCount() {
        return nodes.size();
    }

    public int edgeCount() {
        int total = 0;
        for (Map<String, Integer> targets : edges.values()) {
            total += targets.size();
        }
        return total;
    }

    public int internalCallSiteCount() {
        return internalCallSites;
    }

    public int externalCallCount() {
        return externalCalls;
    }

    public int unresolvedCallCount() {
        return unresolvedCalls;
    }

    /** Les methodes appelees par le noeud donne, avec le poids de chaque arc. */
    public Map<String, Integer> callees(String nodeId) {
        return edges.getOrDefault(nodeId, Map.of());
    }

    /** Les methodes qui appellent le noeud donne (graphe inverse, analyse d'impact). */
    public Map<String, Integer> callers(String nodeId) {
        return reverseEdges.getOrDefault(nodeId, Map.of());
    }

    /** Sortie texte complete : tous les arcs, tries, avec leur poids. */
    public String toReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("Noeuds: ").append(nodeCount())
                .append("  Arcs: ").append(edgeCount())
                .append("  Sites internes: ").append(internalCallSiteCount())
                .append("  Externes: ").append(externalCallCount())
                .append("  Non resolus: ").append(unresolvedCallCount())
                .append("\n");
        for (var sourceEntry : edges.entrySet()) {
            for (var targetEntry : sourceEntry.getValue().entrySet()) {
                sb.append("  ").append(sourceEntry.getKey())
                        .append(" -> ").append(targetEntry.getKey())
                        .append(" (poids ").append(targetEntry.getValue()).append(")")
                        .append("\n");
            }
        }
        return sb.toString();
    }

    /** Tous les identifiants de noeuds connus (utile pour verifier qu'un nom existe). */
    public List<String> allNodeIds() {
        return new ArrayList<>(new TreeSet<>(nodes));
    }
}