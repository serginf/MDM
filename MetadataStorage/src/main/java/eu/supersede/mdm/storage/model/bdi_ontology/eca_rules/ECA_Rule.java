package eu.supersede.mdm.storage.model.bdi_ontology.eca_rules;

/**
 * Created by snadal on 26/01/17.
 */
public class ECA_Rule {

    private String eca_ruleID;
    private String graph;
    private String globalLevel;
    private String name;
    private String feature;
    private OperatorTypes operator;
    private PredicatesTypes predicate;
    private Object value;
    private int windowTime;
    private int windowSize;
    private ActionTypes action;

    public String getEca_ruleID() {
        return eca_ruleID;
    }

    public void setEca_ruleID(String eca_ruleID) {
        this.eca_ruleID = eca_ruleID;
    }

    public String getGraph() {
        return graph;
    }

    public void setGraph(String graph) {
        this.graph = graph;
    }

    public String getGlobalLevel() {
        return globalLevel;
    }

    public void setGlobalLevel(String globalLevel) {
        this.globalLevel = globalLevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public OperatorTypes getOperator() {
        return operator;
    }

    public void setOperator(OperatorTypes operator) {
        this.operator = operator;
    }

    public PredicatesTypes getPredicate() {
        return predicate;
    }

    public void setPredicate(PredicatesTypes predicate) {
        this.predicate = predicate;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int getWindowTime() {
        return windowTime;
    }

    public void setWindowTime(int windowTime) {
        this.windowTime = windowTime;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public ActionTypes getAction() {
        return action;
    }

    public void setAction(ActionTypes action) {
        this.action = action;
    }
}
