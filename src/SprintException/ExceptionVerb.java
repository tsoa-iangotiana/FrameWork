package SprintException;

public class ExceptionVerb extends Exception {
    public ExceptionVerb() {
        super("Verb incompatible avec la methode utilisé");
    }
    public ExceptionVerb(String message) {
        super(message);
    }
}
