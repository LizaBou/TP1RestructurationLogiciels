package hai913i.tp1.model;

public class CallRecord {
    private final String callerClass;
    private final String callerMethod;
    private final int line;
    private final String calleeName;
    private final String receiverType;
    private final String targetSignature; // null si la cible n'est pas resolue
    private final boolean external;
    private final boolean resolved;

    public CallRecord(String callerClass, String callerMethod, int line,
                      String calleeName, String receiverType,
                      String targetSignature, boolean external, boolean resolved) {
        this.callerClass = callerClass;
        this.callerMethod = callerMethod;
        this.line = line;
        this.calleeName = calleeName;
        this.receiverType = receiverType;
        this.targetSignature = targetSignature;
        this.external = external;
        this.resolved = resolved;
    }

    public String getCallerClass() { return callerClass; }
    public String getCallerMethod() { return callerMethod; }
    public int getLine() { return line; }
    public String getCalleeName() { return calleeName; }
    public String getReceiverType() { return receiverType; }
    public String getTargetSignature() { return targetSignature; }
    public boolean isExternal() { return external; }
    public boolean isResolved() { return resolved; }

    @Override
    public String toString() {
        String cible = !resolved ? "NON RESOLUE"
                : external ? "[externe] " + targetSignature
                : targetSignature;
        return callerClass + "." + callerMethod + ", ligne " + line + ", "
                + calleeName + "() : receveur=" + receiverType + ", cible=" + cible;
    }
}