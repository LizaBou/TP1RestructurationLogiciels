package hai913i.tp1;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;

import hai913i.tp1.extract.CallExtractor;
import hai913i.tp1.extract.StructureExtractor;
import hai913i.tp1.graph.CallGraph;
import hai913i.tp1.metrics.MetricsCalculator;
import hai913i.tp1.metrics.MetricsCalculator.ClassMethod;
import hai913i.tp1.model.CallRecord;
import hai913i.tp1.model.ClassInfo;
import hai913i.tp1.model.FieldInfo;
import hai913i.tp1.model.MethodInfo;
import hai913i.tp1.model.ProjectModel;
import hai913i.tp1.parse.JdtParser;
import hai913i.tp1.parse.JdtParser.ParsedFile;
import hai913i.tp1.parse.ProjectSources;

/**
 * Point d'entrée en ligne de commande de l'analyseur.
 *
 * Usage : java -jar target/hai913i-tp1-analyzer.jar DOSSIER_DU_PROJET [SEUIL_X]
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage : java -jar target/hai913i-tp1-analyzer.jar DOSSIER_DU_PROJET");
            System.exit(2);
        }
        Path project = Path.of(args[0]);
        int threshold = 5; // valeur par defaut si non fournie (la vraie gestion d'erreur viendra en B4)
        if (args.length >= 2) {
            try {
                threshold = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Erreur : le seuil X doit etre un entier, recu '" + args[1] + "'");
                System.exit(4);
                return;
            }
        }
        ProjectSources sources;
        try {
            sources = ProjectSources.of(project);
        } catch (IllegalArgumentException e) {
            System.err.println("Erreur : " + e.getMessage());
            System.exit(3);
            return;
        }

        List<ParsedFile> files = JdtParser.parse(sources, List.of());
        int errors = 0;
        for (ParsedFile file : files) {
            for (IProblem problem : file.unit().getProblems()) {
                if (problem.isError()) {
                    errors++;
                    System.err.println(file.path().getFileName() + ":" + problem.getSourceLineNumber() + " "
                            + problem.getMessage());
                }
            }
        }
        System.out.println("Racine des sources     : " + sources.sourceRoot());
        System.out.println("Unites de compilation  : " + files.size());
        System.out.println("Erreurs de compilation : " + errors);

        StructureExtractor extractor = new StructureExtractor();
        List<ClassInfo> allClasses = new ArrayList<>();
        for (ParsedFile file : files) {
            allClasses.addAll(extractor.extract(file.unit()));
        }

        // Ensemble des noms qualifiés de classes du projet (pour distinguer interne/externe)
        Set<String> projectClassNames = new HashSet<>();
        for (ClassInfo ci : allClasses) {
            projectClassNames.add(ci.getQualifiedName());
        }

        CallExtractor callExtractor = new CallExtractor(projectClassNames);
        List<CallRecord> allCalls = new ArrayList<>();

        for (ParsedFile file : files) {
            for (Object obj : file.unit().types()) {
                AbstractTypeDeclaration type = (AbstractTypeDeclaration) obj;
                addCallsRecursively(file, type, callExtractor, allCalls);
            }
        }

        ProjectModel model = new ProjectModel(allClasses, allCalls);

        System.out.println();
        System.out.println("=== Modele du projet : " + model.getClasses().size() + " classes ===");
        for (ClassInfo ci : model.getClasses()) {
            System.out.println(ci);
            for (FieldInfo f : ci.getFields()) {
                System.out.println("    attribut: " + f);
            }
            for (MethodInfo m : ci.getMethods()) {
                System.out.println("    methode : " + m);
            }
        }

        MetricsCalculator metrics = new MetricsCalculator(model);

        List<Path> javaFiles = files.stream().map(ParsedFile::path).toList();
        long totalLines = metrics.applicationLineCount(javaFiles);

        List<Integer> allMethodLines = new ArrayList<>();
        for (ClassInfo ci : model.getClasses()) {
            for (MethodInfo m : ci.getMethods()) {
                allMethodLines.add(m.getLineCount());
            }
        }

        System.out.println();
        System.out.println("=== Metriques B2 (Q1-Q7) ===");
        System.out.printf("Q1 nombre de classes          : %d%n", metrics.classCount());
        System.out.printf("Q2 lignes de l'application     : %d%n", totalLines);
        System.out.printf("Q3 nombre total de methodes    : %d%n", metrics.totalMethodCount());
        System.out.printf("Q4 nombre de paquetages        : %d%n", metrics.packageCount());
        System.out.printf("Q5 moyenne methodes/classe     : %.2f%n", metrics.averageMethodsPerClass());
        System.out.printf("Q6 moyenne lignes/methode      : %.2f%n", metrics.averageLinesPerMethod(allMethodLines));
        System.out.printf("Q7 moyenne attributs/classe    : %.2f%n", metrics.averageFieldsPerClass());

        System.out.println();
        System.out.println("=== Metriques B2 (Q8-Q13) ===");

        System.out.println("Q8 top 10% classes (methodes) :");
        for (ClassInfo c : metrics.topClassesByMethodCount()) {
            System.out.println("  " + c.getQualifiedName() + " (" + c.getMethods().size() + " methodes)");
        }

        System.out.println("Q9 top 10% classes (attributs) :");
        for (ClassInfo c : metrics.topClassesByFieldCount()) {
            System.out.println("  " + c.getQualifiedName() + " (" + c.getFields().size() + " attributs)");
        }

        System.out.println("Q10 intersection Q8/Q9 :");
        for (ClassInfo c : metrics.classesInBothTopCategories()) {
            System.out.println("  " + c.getQualifiedName());
        }

        System.out.println("Q11 classes avec plus de " + threshold + " methodes :");
        for (ClassInfo c : metrics.classesAboveMethodThreshold(threshold)) {
            System.out.println("  " + c.getQualifiedName() + " (" + c.getMethods().size() + " methodes)");
        }

        System.out.println("Q12 top 10% methodes par classe (lignes) :");
        for (var entry : metrics.topMethodsPerClass().entrySet()) {
            if (!entry.getValue().isEmpty()) {
                System.out.println("  " + entry.getKey() + " :");
                for (MethodInfo m : entry.getValue()) {
                    System.out.println("    " + m.getName() + " (" + m.getLineCount() + " lignes)");
                }
            }
        }

        System.out.println("Q13 methodes avec le plus de parametres :");
        for (ClassMethod cm : metrics.methodsWithMaxParameters()) {
            System.out.println("  " + cm.className() + "." + cm.method().getName()
                    + " (" + cm.method().getParameterCount() + " parametres)");
        }

        int internal2 = 0, external2 = 0, unresolved2 = 0;
        for (CallRecord c : model.getCalls()) {
            if (!c.isResolved()) unresolved2++;
            else if (c.isExternal()) external2++;
            else internal2++;
        }
        System.out.println();
        System.out.println("=== Appels du modele : " + model.getCalls().size() + " ===");
        System.out.println("Internes: " + internal2 + "  Externes: " + external2 + "  Non resolus: " + unresolved2);

        // ===== B3 : graphe d'appel =====
        CallGraph graph = new CallGraph(model);

        System.out.println();
        System.out.println("=== Graphe d'appel (B3) ===");
        System.out.println(graph.toReport());

        // Verifications ciblees du point de controle B3
        String catalogAdd2 = "library.service.Catalog#add(library.model.Item,library.model.Item)";
        String catalogAdd1 = "library.service.Catalog#add(library.model.Item)";
        System.out.println("Verif Catalog#add(Item,Item) -> Catalog#add(Item) :");
        System.out.println("  poids = " + graph.callees(catalogAdd2).getOrDefault(catalogAdd1, 0));

        String textUtilsRepeat = "library.util.TextUtils#repeat(java.lang.String,int)";
        System.out.println("Verif recursion TextUtils#repeat :");
        System.out.println("  poids = " + graph.callees(textUtilsRepeat).getOrDefault(textUtilsRepeat, 0));

        String loanableCheckOut = "library.model.Loanable#checkOut(library.model.Member)";
        System.out.println("Verif appelantes de Loanable#checkOut(Member) :");
        System.out.println("  " + graph.callers(loanableCheckOut));

        String itemCheckOut = "library.model.Item#checkOut(library.model.Member)";
        System.out.println("Verif appelantes de Item#checkOut(Member) (doit etre vide) :");
        System.out.println("  " + graph.callers(itemCheckOut));
    }

    private static void addCallsRecursively(ParsedFile file, AbstractTypeDeclaration type,
                                            CallExtractor extractor, List<CallRecord> out) {
        ITypeBinding binding = type.resolveBinding();
        String qualifiedName = (binding != null) ? binding.getQualifiedName() : type.getName().getIdentifier();
        out.addAll(extractor.extractForType(file.unit(), type, qualifiedName));

        for (Object member : type.bodyDeclarations()) {
            if (member instanceof AbstractTypeDeclaration nested) {
                addCallsRecursively(file, nested, extractor, out);
            }
        }
    }
}