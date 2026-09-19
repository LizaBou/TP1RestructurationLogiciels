package hai913i.tp1.extract;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import hai913i.tp1.model.ClassInfo;
import hai913i.tp1.model.FieldInfo;
import hai913i.tp1.model.MethodInfo;

/**
 * Etape A2 (+ complement pour B2) : extrait la structure du projet
 * (classes, attributs, methodes, heritage) a partir des AST JDT.
 */
public class StructureExtractor {

    public List<ClassInfo> extract(CompilationUnit cu) {
        List<ClassInfo> result = new ArrayList<>();
        collectTypes(cu, cu.types(), result);
        return result;
    }

    // Parcourt une liste de declarations de type de premier niveau,
    // et redescend dans les classes membres (imbriquees) recursivement.
    private void collectTypes(CompilationUnit cu, List<?> types, List<ClassInfo> result) {
        for (Object obj : types) {
            AbstractTypeDeclaration type = (AbstractTypeDeclaration) obj;
            ClassInfo info = extractOne(type);
            result.add(info);

            // classes imbriquees : on redescend dans bodyDeclarations()
            for (Object member : type.bodyDeclarations()) {
                if (member instanceof AbstractTypeDeclaration nested) {
                    ClassInfo nestedInfo = extractOne(nested);
                    result.add(nestedInfo);
                    fillFieldsAndMethods(cu, nested, nestedInfo);
                }
            }
            fillFieldsAndMethods(cu, type, info);
        }
    }

    private ClassInfo extractOne(AbstractTypeDeclaration type) {
        ITypeBinding binding = type.resolveBinding();
        String qualifiedName = (binding != null) ? binding.getQualifiedName() : type.getName().getIdentifier();
        String kind = (type instanceof EnumDeclaration) ? "enum"
                : (type instanceof TypeDeclaration td && td.isInterface()) ? "interface"
                : "class";
        String packageName = (binding != null && binding.getPackage() != null)
                ? binding.getPackage().getName() : "(defaut)";

        ClassInfo info = new ClassInfo(qualifiedName, kind, packageName);

        if (binding != null) {
            ITypeBinding superclass = binding.getSuperclass();
            while (superclass != null && !"java.lang.Object".equals(superclass.getQualifiedName())) {
                info.getSuperclasses().add(superclass.getQualifiedName());
                superclass = superclass.getSuperclass();
            }
            for (ITypeBinding itf : binding.getInterfaces()) {
                info.getInterfaces().add(itf.getQualifiedName());
            }
        }
        return info;
    }

    private void fillFieldsAndMethods(CompilationUnit cu, AbstractTypeDeclaration type, ClassInfo info) {
        for (Object obj : type.bodyDeclarations()) {
            BodyDeclaration decl = (BodyDeclaration) obj;
            if (decl instanceof FieldDeclaration fd) {
                String visibility = visibilityOf(fd.getModifiers());
                String fieldType = fd.getType().toString();
                for (Object fragObj : fd.fragments()) {
                    VariableDeclarationFragment frag = (VariableDeclarationFragment) fragObj;
                    info.getFields().add(new FieldInfo(frag.getName().getIdentifier(), fieldType, visibility));
                }
            } else if (decl instanceof MethodDeclaration md) {
                int lineCount = computeLineCount(cu, md);
                info.getMethods().add(new MethodInfo(
                        md.getName().getIdentifier(),
                        md.parameters().size(),
                        md.isConstructor(),
                        lineCount));
            }
        }
    }

    // §4.2 : lignes physiques du corps, de l'accolade ouvrante a la fermante incluses.
    // 0 pour une methode sans corps (abstraite, interface).
    private int computeLineCount(CompilationUnit cu, MethodDeclaration md) {
        Block body = md.getBody();
        if (body == null) {
            return 0;
        }
        int startLine = cu.getLineNumber(body.getStartPosition());
        int endLine = cu.getLineNumber(body.getStartPosition() + body.getLength() - 1);
        return endLine - startLine + 1;
    }

    private String visibilityOf(int modifiers) {
        if (Modifier.isPublic(modifiers)) return "public";
        if (Modifier.isProtected(modifiers)) return "protected";
        if (Modifier.isPrivate(modifiers)) return "private";
        return "package";
    }
}