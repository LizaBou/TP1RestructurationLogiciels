package hai913i.tp1.extract;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

import hai913i.tp1.model.CallRecord;

/**
 * Etape A3 : pour chaque methode comptee, on retrouve les appels qu'elle
 * contient (MethodInvocation, SuperMethodInvocation), avec le type statique
 * du receveur et la cible resolue.
 */
public class CallExtractor {

    private final Set<String> projectClassNames;

    public CallExtractor(Set<String> projectClassNames) {
        this.projectClassNames = projectClassNames;
    }

    /** A appeler pour chaque type de premier niveau ET chaque type membre. */
    public List<CallRecord> extractForType(CompilationUnit cu, AbstractTypeDeclaration type, String qualifiedTypeName) {
        List<CallRecord> result = new ArrayList<>();
        for (Object obj : type.bodyDeclarations()) {
            if (obj instanceof MethodDeclaration md && md.getBody() != null) {
                extractForMethod(cu, md, qualifiedTypeName, result);
            }
        }
        return result;
    }

    private void extractForMethod(CompilationUnit cu, MethodDeclaration method,
                                  String qualifiedTypeName, List<CallRecord> out) {
        String methodName = method.isConstructor() ? "<init>" : method.getName().getIdentifier();
        IMethodBinding methodOwnBinding = method.resolveBinding();
        ITypeBinding enclosingType = methodOwnBinding != null
                ? methodOwnBinding.getDeclaringClass() : null;
        String callerSignature = (methodOwnBinding != null)
                ? SignatureUtil.signatureOf(methodOwnBinding)
                : methodName + "(" + method.parameters().size() + " param. non resolus)";

        Block body = method.getBody();
        body.accept(new ASTVisitor() {

            @Override
            public boolean visit(MethodInvocation node) {
                handleInvocation(node, node.getName().getIdentifier(),
                        node.resolveMethodBinding(), node.getExpression(), enclosingType,
                        false, cu, qualifiedTypeName, methodName, callerSignature, out);
                return true; // continue : on veut aussi les appels dans les lambdas/args
            }

            @Override
            public boolean visit(SuperMethodInvocation node) {
                handleInvocation(node, node.getName().getIdentifier(),
                        node.resolveMethodBinding(), null, enclosingType,
                        true, cu, qualifiedTypeName, methodName, callerSignature, out);
                return true;
            }
        });
    }

    private void handleInvocation(ASTNode node, String calleeName, IMethodBinding methodBinding,
                                  Expression receiverExpr, ITypeBinding enclosingType, boolean isSuperCall,
                                  CompilationUnit cu, String callerClass, String callerMethod,
                                  String callerSignature, List<CallRecord> out) {

        int line = cu.getLineNumber(node.getStartPosition());
        String receiverType = resolveReceiverType(receiverExpr, methodBinding, enclosingType, isSuperCall);

        boolean resolved = methodBinding != null;
        String targetSignature = null;
        boolean external = true;

        if (resolved) {
            ITypeBinding declaring = methodBinding.getDeclaringClass();
            String declQualified = declaring != null ? declaring.getErasure().getQualifiedName() : "?";
            targetSignature = declQualified + "#" + SignatureUtil.signatureOf(methodBinding);
            external = declaring == null || !projectClassNames.contains(declQualified);
        }

        out.add(new CallRecord(callerClass, callerMethod, callerSignature, line, calleeName,
                receiverType, targetSignature, external, resolved));
    }

    // Regles du sujet, section 4.4 :
    // - receveur explicite -> type statique de l'expression
    // - pas de receveur, methode statique -> type qui la declare
    // - pas de receveur, methode d'instance -> type englobant (this implicite)
    // - super.m() -> superclasse directe du type englobant
    private String resolveReceiverType(Expression receiverExpr, IMethodBinding methodBinding,
                                       ITypeBinding enclosingType, boolean isSuperCall) {
        if (isSuperCall) {
            if (enclosingType != null && enclosingType.getSuperclass() != null) {
                return enclosingType.getSuperclass().getErasure().getQualifiedName();
            }
            return "?";
        }
        if (receiverExpr != null) {
            ITypeBinding t = receiverExpr.resolveTypeBinding();
            return t != null ? t.getErasure().getQualifiedName() : "?";
        }
        // pas de receveur ecrit
        if (methodBinding != null && Modifier.isStatic(methodBinding.getModifiers())) {
            ITypeBinding declaring = methodBinding.getDeclaringClass();
            return declaring != null ? declaring.getErasure().getQualifiedName() : "?";
        }
        return enclosingType != null ? enclosingType.getErasure().getQualifiedName() : "?";
    }

    private String signatureOf(IMethodBinding mb) {
        return SignatureUtil.signatureOf(mb);
    }
}