package hai913i.tp1.extract;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Calcule une signature de methode coherente, utilisee a la fois par
 * StructureExtractor (pour identifier les noeuds du graphe d'appel) et par
 * CallExtractor (pour identifier les cibles d'appel). Les deux doivent
 * produire EXACTEMENT le meme format pour qu'un appel se relie correctement
 * a son noeud methode en B3.
 *
 * Format : nom(Type1,Type2,...) avec les types apres effacement (erasure),
 * ex. "checkOut(library.model.Member)".
 */
public final class SignatureUtil {

    private SignatureUtil() {
    }

    public static String signatureOf(IMethodBinding mb) {
        StringBuilder sb = new StringBuilder(mb.getName()).append("(");
        ITypeBinding[] params = mb.getMethodDeclaration().getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(params[i].getErasure().getQualifiedName());
        }
        return sb.append(")").toString();
    }
}