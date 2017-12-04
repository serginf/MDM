package eu.supersede.mdm.storage.cep.RDF_Model.condition;

import eu.supersede.mdm.storage.cep.Interpreter.InterpreterContext;
import eu.supersede.mdm.storage.cep.Interpreter.InterpreterException;
import eu.supersede.mdm.storage.cep.RDF_Model.Operators.LogicOperator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by osboxes on 17/04/17.
 */
public class ComplexPredicate extends Condition {

    protected List<Condition> conditions;
    protected LogicOperator operator;

    public ComplexPredicate() {
        super();
        conditions = new ArrayList<>();
    }

    public ComplexPredicate(List<Condition> conditions, LogicOperator operator) {
        super();
        this.conditions = conditions;
        this.operator = operator;
    }

    public ComplexPredicate(String IRI) {
        super(IRI);
        conditions = new ArrayList<>();
    }

    public ComplexPredicate(List<Condition> conditions, LogicOperator operator, String IRI) {
        super(IRI);
        this.conditions = conditions;
        this.operator = operator;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public LogicOperator getOperator() {
        return operator;
    }

    public void setOperator(LogicOperator operator) {
        this.operator = operator;
    }

    @Override
    public String interpret(InterpreterContext context) throws InterpreterException {
        switch (context) {
            case ESPER: {
                String complexPredicate = conditions.get(0).interpret(context) + " ";
                for (int i = 1; i < conditions.size(); i++) {
                    complexPredicate += operator.interpret(context) + " ";
                    complexPredicate += conditions.get(i).interpret(context) + " ";
                }
                return complexPredicate;
            }
            default: {
                String complexPredicate = conditions.get(0).interpret(context) + " ";
                for (int i = 1; i < conditions.size(); i++) {
                    complexPredicate += operator.interpret(context) + " ";
                    complexPredicate += conditions.get(i).interpret(context) + " ";
                }
                return complexPredicate;
            }
        }
    }

    @Override
    public String interpret(InterpreterContext context, Map<String, Object> props) throws InterpreterException {
        return null;
    }

    @Override
    public Map<String, String> interpretToMap(InterpreterContext context) throws InterpreterException {
        Map<String, String> map = new HashMap<>();
        switch (context) {
            case ESPER: {
                boolean isgroupby = this.hasGroupBy();
                boolean ishaving = this.hasHaving();
                boolean iswhere = this.hasWhere();
                if ((isgroupby && ishaving) || (ishaving && iswhere) || (isgroupby && iswhere)) {
                    for (Condition condition : conditions) {
                        if (isgroupby) {
                            if (condition.hasGroupBy()) {
                                map.put("group by", condition.interpret(context));
                                isgroupby = false;
                            }
                        }
                        if (ishaving) {
                            if (condition.hasHaving()) {
                                map.put("having", condition.interpret(context));
                                ishaving = false;
                            }
                        }
                        if (iswhere) {
                            if (condition.hasWhere()) {
                                map.put("where", condition.interpret(context));
                                iswhere = false;
                            }
                        }
                    }
                } else {
                    if (isgroupby) {
                        map.put("group by", this.interpret(context));
                    } else if (ishaving) {
                        map.put("having", this.interpret(context));
                    } else if (iswhere) {
                        map.put("where", this.interpret(context));
                    }
                }
                return map;
            }
            default: {

                map.put("complex predicate", this.interpret(context));
                return map;
            }

        }
    }
}
