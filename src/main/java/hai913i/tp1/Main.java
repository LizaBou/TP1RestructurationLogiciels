package hai913i.tp1;
import hai913i.tp1.extract.StructureExtractor;
import hai913i.tp1.model.ClassInfo;
import hai913i.tp1.model.FieldInfo;
import hai913i.tp1.model.MethodInfo;
import hai913i.tp1.visit.TreePrinterVisitor;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import hai913i.tp1.extract.CallExtractor;
import hai913i.tp1.model.CallRecord;

import org.eclipse.jdt.core.compiler.IProblem;

import hai913i.tp1.parse.JdtParser;
import hai913i.tp1.parse.JdtParser.ParsedFile;
import hai913i.tp1.parse.ProjectSources;

/**
 * Point d'entrée en ligne de commande de l'analyseur (version de départ).
 *
 * Le squelette ne vérifie que l'environnement (point de contrôle A0) : il analyse le projet et affiche
 * le nombre d'unités de compilation et d'erreurs. Tout le reste est à concevoir : extraction de la
 * structure, appels, métriques, graphe d'appel, options comme le seuil X.
 *
 * Usage : java -jar target/hai913i-tp1-analyzer.jar DOSSIER_DU_PROJET
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
        List<ClassInfo> allClasses = new java.util.ArrayList<>();
        for (ParsedFile file : files) {
            allClasses.addAll(extractor.extract(file.unit()));
        }

        System.out.println();
        System.out.println("=== Structure extraite : " + allClasses.size() + " classes ===");
        for (ClassInfo ci : allClasses) {
            System.out.println(ci);
            for (FieldInfo f : ci.getFields()) {
                System.out.println("    attribut: " + f);
            }
            for (MethodInfo m : ci.getMethods()) {
                System.out.println("    methode : " + m);
            }
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

        int internal = 0, external = 0, unresolved = 0;
        for (CallRecord c : allCalls) {
            if (!c.isResolved()) unresolved++;
            else if (c.isExternal()) external++;
            else internal++;
        }

        System.out.println();
        System.out.println("=== Appels extraits : " + allCalls.size() + " ===");
        System.out.println("Internes: " + internal + "  Externes: " + external + "  Non resolus: " + unresolved);
        for (CallRecord c : allCalls) {
            System.out.println("  " + c);
        }

      //  for (ParsedFile file : files) {
       //     if (file.path().toString().endsWith("Dvd.java")
       //             || file.path().toString().endsWith("Category.java")) {
        //        System.out.println("=== " + file.path() + " ===");
      //          file.unit().accept(new TreePrinterVisitor());
       //     }
       // }

        // À FAIRE (A1 et suite) : parcourir les AST avec vos visiteurs, construire votre modèle de faits,
        // puis calculer les métriques et le graphe d'appel. Gardez cette classe courte : elle lit les
        // arguments et délègue.
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
