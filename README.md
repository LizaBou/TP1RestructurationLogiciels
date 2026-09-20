# HAI913I — TP1 Analyse statique

Analyseur statique de programmes Java en ligne de commande, construit avec Eclipse JDT Core.
Il lit le code source d'un projet, en extrait la structure (classes, attributs, méthodes,
héritage), les appels de méthodes, calcule des métriques et construit le graphe d'appel.

## Projet

### Architecture

Le code suit la chaîne présentée en CM02 : *extracteur de modèle → modèle → extracteurs de
propriétés → propriétés → traitements*.

```
src/main/java/hai913i/tp1/
  parse/
    JdtParser.java          extracteur de modele : fichiers .java -> AST JDT avec bindings
    ProjectSources.java     localise et valide les fichiers .java d'un projet
  extract/
    StructureExtractor.java extrait classes, attributs, methodes, heritage
    CallExtractor.java      extrait les appels de methodes et le type du receveur
    SignatureUtil.java      calcule une signature de methode coherente (nom+params)
  model/
    ClassInfo.java, FieldInfo.java, MethodInfo.java, CallRecord.java
                             modele de faits, independant de JDT
    ProjectModel.java       regroupe les faits, garantit un ordre deterministe
  metrics/
    MetricsCalculator.java  toutes les metriques (questions 1 a 13)
    TopTenPercent.java      regle generique des "10%" (paragraphe 4.3 du sujet)
  graph/
    CallGraph.java          construction et interrogation du graphe d'appel
  visit/
    TreePrinterVisitor.java visiteur de demonstration (etape A1)
  Main.java                  point d'entree en ligne de commande
```

Une fois les faits extraits (`ClassInfo`, `MethodInfo`, `CallRecord`), aucune classe
`org.eclipse.jdt.*` n'est plus utilisée dans le reste du programme : les métriques et le graphe
d'appel travaillent uniquement sur le modèle de faits (`ProjectModel`).

### Entrée attendue

Un chemin vers la racine d'un projet Maven Java (le dossier contenant `pom.xml` et `src/`), et
optionnellement un seuil entier X pour la question 11.

### Sorties produites

Sortie texte sur la sortie standard, dans l'ordre :
1. informations de parsing (racine des sources, unités de compilation, erreurs)
2. structure complète du projet (classes, attributs, méthodes)
3. métriques B2, questions 1 à 13
4. graphe d'appel B3 (résumé + liste des arcs)

Les erreurs de compilation individuelles sont affichées sur la sortie d'erreur, sans interrompre
l'analyse des autres fichiers.

### Dépendances

- Eclipse JDT Core `org.eclipse.jdt:org.eclipse.jdt.core:3.46.0` (Maven Central, résolu
  automatiquement par Maven)
- Aucune dépendance à Spoon (partie C non traitée dans cette version)

### Limites connues

- Pas d'expansion des appels virtuels (pas de CHA) : un appel écrit via un type interface ou une
  superclasse abstraite reste rattaché à ce type déclaré, jamais à une implémentation concrète.
  Exemple concret sur le projet de validation : l'appel `loanable.checkOut(member)` dans
  `LoanService.borrow` est rattaché à `Loanable#checkOut`, jamais à `Item#checkOut`, même si
  c'est cette dernière qui s'exécute réellement.
- Pas de suivi de la réflexion, des proxys, ni de l'injection de dépendances.
- Les arcs vers les constructeurs ne sont pas construits (option non activée).

## Environnement

Testé avec :
- JDK 17 (`java -version`)
- Maven 3.9.x (`mvn -v`)
- Eclipse JDT Core 3.46.0
- IntelliJ IDEA (import direct du `pom.xml`) sous Linux (poste `dptinfo-pret` de la fac)

Aucune interface graphique n'est requise : le projet se construit et s'exécute entièrement en
ligne de commande.

## Construction

Depuis une copie propre du dépôt (aucun `target/` présent) :

```bash
cd starter
mvn clean package
```

Un test automatique (`ParserSmokeTest`) tourne pendant le build et vérifie que la résolution des
bindings entre fichiers fonctionne. Le build produit `target/hai913i-tp1-analyzer.jar`.

## Exécution

```bash
java -jar target/hai913i-tp1-analyzer.jar CHEMIN_DU_PROJET [SEUIL_X]
```

- `CHEMIN_DU_PROJET` : chemin vers un projet Maven Java à analyser (obligatoire). Entourer de
  guillemets si le chemin contient des espaces.
- `SEUIL_X` : entier utilisé pour la question 11 (facultatif, 5 par défaut).

Exemple, sur le projet de validation fourni :

```bash
java -jar target/hai913i-tp1-analyzer.jar ../resources/validation 5
```

## Validation

Testé sur `resources/validation` (14 fichiers, 4 paquetages) :

| Métrique | Valeur obtenue |
|---|---|
| Unités de compilation / erreurs | 14 / 0 |
| Classes (classes+interfaces+enum) | 15 |
| Lignes de code | 493 |
| Méthodes | 59 (dont 9 constructeurs) |
| Attributs | 21 |
| Appels | 94 (46 internes, 48 externes, 0 non résolu) |
| Nœuds / arcs du graphe | 59 / 44 |

Testé aussi sur `resources/robustness/` :
- `missing-dep` : analyse complète malgré l'import manquant, appel concerné signalé non résolu
- `syntax-error` : erreurs de `Broken.java` signalées, `Helper.java` analysé normalement
- `no-java-files` : message d'erreur explicite, code de sortie non nul
- dossier inexistant : message d'erreur explicite, code de sortie non nul
- seuil X mal formé (ex. `abc`) : refus explicite, code de sortie non nul
- chemin contenant des espaces : analyse fonctionnelle, sans erreur

## Dépannage

**`IllegalStateException: Missing system library`** : le parseur JDT n'a pas reçu le classpath du
JDK d'exécution. Vérifier que `ASTParser.setEnvironment(...)` est appelé avec `true` en dernier
argument (inclusion du JDK courant).

**`mvn -q compile exec:java` échoue avec `mainClass missing`** : cette commande est destinée au
projet `resources/validation` (le projet analysé), pas au projet `starter` (l'analyseur). Pour
lancer l'analyseur, utiliser `mvn clean package` puis `java -jar target/...jar`.

**Chemin avec espaces sous Linux** : entourer le chemin de guillemets, par exemple
`java -jar target/hai913i-tp1-analyzer.jar "/chemin/avec espace/projet"`.

**Erreur d'authentification Git (`403`) lors d'un `git push`** : GitHub n'accepte plus les mots
de passe classiques ; utiliser un Personal Access Token (Settings > Developer settings > Personal
access tokens > Tokens classic) avec la permission `repo`.

## Dépôt

Code source également disponible sur :
<https://github.com/LizaBou/TP1RestructurationLogiciels>
