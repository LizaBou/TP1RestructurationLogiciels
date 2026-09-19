package hai913i.tp1.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Implemente la regle "les 10%" du sujet, §4.3 :
 * - parmi n candidats, on retient les k = ceil(n/10) premiers par valeur
 *   decroissante (au moins 1 si n >= 1, aucun si n = 0)
 * - tout candidat egal a la valeur du k-ieme est retenu aussi (le resultat
 *   peut donc depasser k elements)
 * - un candidat dont la valeur vaut 0 n'est jamais retenu
 */
public final class TopTenPercent {

    private TopTenPercent() {
    }

    public static <T> List<T> select(List<T> candidates, ToIntFunction<T> valueOf) {
        int n = candidates.size();
        if (n == 0) {
            return List.of();
        }

        List<T> sorted = new ArrayList<>(candidates);
        sorted.sort((a, b) -> Integer.compare(valueOf.applyAsInt(b), valueOf.applyAsInt(a)));

        int k = (n + 9) / 10; // ceil(n/10), evite l'arrondi vers le bas d'un (int)(n*0.1)
        int thresholdValue = valueOf.applyAsInt(sorted.get(k - 1));

        List<T> result = new ArrayList<>();
        for (T t : sorted) {
            int v = valueOf.applyAsInt(t);
            if (v >= thresholdValue && v > 0) {
                result.add(t);
            }
        }
        return result;
    }
}