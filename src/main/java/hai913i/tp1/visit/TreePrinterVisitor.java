package hai913i.tp1.visit;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

public class TreePrinterVisitor extends ASTVisitor {
    private int depth = 0;

    @Override
    public boolean preVisit2(ASTNode node) {
        String indent = "  ".repeat(depth);
        System.out.println(indent + node.getClass().getSimpleName());
        depth++;
        return true; // true = continue à visiter les enfants de ce nœud
    }

    @Override
    public void postVisit(ASTNode node) {
        depth--;
    }
}