package eu.supersede.mdm.storage.cep.RDF_Model.window;

import eu.supersede.mdm.storage.cep.Interpreter.Interpreter;
import eu.supersede.mdm.storage.cep.Interpreter.InterpreterContext;
import eu.supersede.mdm.storage.cep.Interpreter.InterpreterException;
import eu.supersede.mdm.storage.cep.RDF_Model.Operators.TimeUnit;

import java.util.Map;

/**
 * Created by osboxes on 17/04/17.
 */
public class Window implements Interpreter {

    private String IRI;
    private WindowType windowType;
    private int within;
    private WindowUnit windowUnit;
    private TimeUnit timeUnit;

    public Window() {
    }

    public Window(WindowType windowType, int within, TimeUnit timeUnit, String IRI) {
        this.IRI = IRI;
        this.windowType = windowType;
        this.within = within;
        this.timeUnit = timeUnit;
        this.windowUnit = WindowUnit.TIME;
    }


    public Window(WindowType windowType, int within, String IRI) {
        this.IRI = IRI;
        this.windowType = windowType;
        this.within = within;
        this.windowUnit = WindowUnit.EVENT;
    }


    public WindowType getWindowType() {
        return windowType;
    }

    public void setWindowType(WindowType windowType) {
        this.windowType = windowType;
    }

    public int getWithin() {
        return within;
    }

    public void setWithin(int within) {
        this.within = within;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public String getIRI() {
        return IRI;
    }

    public void setIRI(String IRI) {
        this.IRI = IRI;
    }

    public WindowUnit getWindowUnit() {
        return windowUnit;
    }

    public void setWindowUnit(WindowUnit windowUnit) {
        this.windowUnit = windowUnit;
    }

    @Override
    public String interpret(InterpreterContext context) throws InterpreterException {
        if (windowType.equals(WindowType.SLIDING_WINDOW))
            return ".win:time_batch(" + within + timeUnit + ")";
        else if (windowType.equals(WindowType.TUMBLING_WINDOW))
            return ".win:time(" + within + " " + timeUnit + ")";
        throw new InterpreterException("window type not supported");
    }

    @Override
    public String interpret(InterpreterContext context, Map<String, Object> props) throws InterpreterException {
        return null;
    }

    @Override
    public Map<String, String> interpretToMap(InterpreterContext context) throws InterpreterException {
        return null;
    }
}
