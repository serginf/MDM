package eu.supersede.mdm.storage.cep.RDF_Model.Operators;


import eu.supersede.mdm.storage.cep.Interpreter.InterpreterContext;
import eu.supersede.mdm.storage.cep.Interpreter.InterpreterException;

import java.util.Map;

/**
 * Created by osboxes on 18/05/17.
 */
public class Sequence extends TemporalOperator {
    public Sequence(String IRI) {
        super(TemporalOperatorEnum.Sequence, IRI);
    }

    public Sequence() {
        super(TemporalOperatorEnum.Sequence);
    }

    @Override
    public String interpret(InterpreterContext context) throws InterpreterException {
        switch (context) {
            case ESPER: {
                return "->";
            }
            default: {
                return "->";
            }
        }
    }

    @Override
    public String interpret(InterpreterContext context, Map<String, Object> props) throws InterpreterException {
        return null;
    }

    @Override
    public Map<String, String> interpretToMap(InterpreterContext context) throws InterpreterException {
        throw new InterpreterException("not supported");
    }
}

