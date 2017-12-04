package eu.supersede.mdm.storage.cep.sm4cep;

import com.google.common.collect.Lists;
import eu.supersede.mdm.storage.cep.RDF_Model.Operators.*;
import eu.supersede.mdm.storage.cep.RDF_Model.Rule;
import eu.supersede.mdm.storage.cep.RDF_Model.action.Action;
import eu.supersede.mdm.storage.cep.RDF_Model.condition.*;
import eu.supersede.mdm.storage.cep.RDF_Model.event.*;
import eu.supersede.mdm.storage.cep.RDF_Model.window.Window;
import eu.supersede.mdm.storage.cep.RDF_Model.window.WindowType;
import eu.supersede.mdm.storage.cep.RDF_Model.window.WindowUnit;
import eu.supersede.mdm.storage.util.Utils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import scala.Tuple2;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Sm4cepParser {

    private String sm4cepNamespace = "http://www.essi.upc.edu/~jvarga/sm4cep/"; // the namespace address

    private String endpoint = "http://localhost:8890/sparql";
    private HashMap<String, EventSchema> eventSchemata = new HashMap<String, EventSchema> (); // all event schemata, something like event schemata cash
    private HashMap<String, Attribute> eventAttributes = new HashMap<String, Attribute> (); // all event attributes, something like event attributes cash

    public Sm4cepParser() {
    }

    public Sm4cepParser(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getSm4cepNamespace() {
        return sm4cepNamespace;
    }

    public void setSm4cepNamespace(String sm4cepNamespace) {
        this.sm4cepNamespace = sm4cepNamespace;
    }

    public HashMap<String, EventSchema> getEventSchemata() {
        return eventSchemata;
    }

    public void setEventSchemata(HashMap<String, EventSchema> eventSchemata) {
        this.eventSchemata = eventSchemata;
    }

    public HashMap<String, Attribute> getEventAttributes() {
        return eventAttributes;
    }

    public void setEventAttributes(HashMap<String, Attribute> eventAttributes) {
        this.eventAttributes = eventAttributes;
    }

    // for a given rule IRI get the rule with all its elements
    public Rule getRule(String ruleIRI) {
        Rule rule = new Rule();
        rule.setIRI(ruleIRI);

        try {
            Window window = this.getWindow(ruleIRI);
            rule.setWindow(window);

            CEPElement cepElement = this.getCEPElement(ruleIRI);
            rule.setCEPElement(cepElement);

            Condition condition = this.getConditionForRule(ruleIRI);
            rule.setCondition(condition);

            Action action = this.getAction(ruleIRI);
            rule.setAction(action);

        } catch (WindowException we) {
            System.out.println("The rule has the following window exception: \n" + we);
        } catch (CEPElementException cepe) {
            System.out.println("The rule has the following CEP Element exception: \n" + cepe);
        } catch (ConditionException ce) {
            System.out.println("The rule has the following Condition exception: \n" + ce);
        } catch (ActionException ae) {
            System.out.println("The rule has the following Action exception: \n" + ae);
        }


        return rule;
    }

    public Window getWindow(String ruleIRI) throws WindowException {
        Window window = new Window();

        String qGetWindow =

                "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +

                        " SELECT DISTINCT ?w \n" +
                        " WHERE { \n" +
                        ruleIRI + " a sm4cep:Rule . \n" +
                        ruleIRI + " sm4cep:hasWindow ?w . \n" +
                        "?w a sm4cep:Window . \n" +
                        "} ";

        ResultSet results = this.runAQuery(qGetWindow, endpoint);

        if (results.hasNext()) {
            QuerySolution soln = results.nextSolution();

            if (results.hasNext())
                throw new WindowException("Rule has more than one window!");

            RDFNode windowNode = soln.get("w");
            String windowIRI = formatIRI(windowNode.toString());

            window.setIRI(windowIRI);

            // set window kind/type
            WindowType windowType = this.getWindowKind(windowIRI);
            window.setWindowType(windowType);

            // set window unit
            WindowUnit windowUnit = this.getWindowUnit(windowIRI);
            window.setWindowUnit(windowUnit);

        } else {
            throw new WindowException("Rule has no window!");
        }

        return window;
    }

    // get window kind/type
    public WindowType getWindowKind(String windowIRI) throws WindowException {

        WindowType windowType = null;

        String qGetWindowKind =

                "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +

                        " SELECT DISTINCT ?windowKind \n" +
                        " WHERE { \n" +
                        windowIRI + " sm4cep:hasWindowAttribute ?windowKind . \n" +
                        "?windowKind a sm4cep:WindowKind . \n" +
                        "} ";

        ResultSet results = this.runAQuery(qGetWindowKind, endpoint);


        if (results.hasNext()) {
            QuerySolution soln = results.nextSolution();

            if (results.hasNext())
                throw new WindowException("Rule has more than one window kind!");

            RDFNode windowKindNode = soln.get("windowKind");
            String windowKind = formatIRI(windowKindNode.toString());

            if (this.equalsToSm4cepElement(windowKind, "SlidingWindow")) {
                windowType = WindowType.SLIDING_WINDOW;
            } else if (this.equalsToSm4cepElement(windowKind, "TumblingWindow")) {
                windowType = WindowType.TUMBLING_WINDOW;
            }

        } else {
            throw new WindowException("Rule has no window kind!");
        }

        return windowType;
    }

    // get window unit
    public WindowUnit getWindowUnit(String windowIRI) throws WindowException {

        WindowUnit windowUnit = null;

        String qGetWindowUnit =

                "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +

                        " SELECT DISTINCT ?windowUnit \n" +
                        " WHERE { \n" +
                        windowIRI + " sm4cep:hasWindowAttribute ?windowUnit . \n" +
                        "?windowUnit a sm4cep:WindowUnit . \n" +
                        "} ";

        ResultSet results = this.runAQuery(qGetWindowUnit, endpoint);


        if (results.hasNext()) {
            QuerySolution soln = results.nextSolution();

            if (results.hasNext())
                throw new WindowException("Rule has more than one window unit!");

            RDFNode windowUnitNode = soln.get("windowUnit");
            String windowUnitString = formatIRI(windowUnitNode.toString());

            if (this.equalsToSm4cepElement(windowUnitString, "TimeUnit")) {
                windowUnit = WindowUnit.TIME;
            }  else if (this.equalsToSm4cepElement(windowUnitString, "EventUnit")) {
                windowUnit = WindowUnit.EVENT;
            }

        } else {
            throw new WindowException("Rule has no window unit!");
        }

        return windowUnit;
    }

    /*****   GET CEP ELEMENT   *****/

    // get CEP element
    public CEPElement getCEPElement (String ruleIRI) throws CEPElementException {
        CEPElement cepElement;
        String qGetCEPElement =

                "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +

                        " SELECT DISTINCT ?e \n" +
                        " WHERE { \n" +
                        ruleIRI + " a sm4cep:Rule . \n" +
                        ruleIRI + " sm4cep:hasCEPElement ?e . \n" +
                        //"?e a sm4cep:CEPElement . \n" +
                        "} ";

        ResultSet results = this.runAQuery(qGetCEPElement, endpoint);

        if (results.hasNext()) {
            QuerySolution soln = results.nextSolution();

            if (results.hasNext())
                throw new CEPElementException("Rule has more than one CEP element!");

            RDFNode cepNode = soln.get("e");
            String cepIRI = formatIRI(cepNode.toString());

            cepElement = this.getCEPElement(cepIRI, ruleIRI);
        } else {
            throw new CEPElementException("Rule has no CEP element!");
        }

        return cepElement;
    }


    // get CEP elements for a given rule
    public CEPElement getCEPElement(String cepElementIRI, String ruleIRI) throws CEPElementException {
        CEPElement cepElement = null;

        String qGetElementType =

                "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +

                        " SELECT DISTINCT ?elementType \n" +
                        " WHERE { \n" +
                        cepElementIRI + " a ?elementType . \n" +
                        "} ";

        ResultSet results = this.runAQuery(qGetElementType, endpoint);

        if (results.hasNext()) { // ASSUMPTION IS THAT EACH RULE HAS A SINGLE CEP ELEMENT TYPE
            QuerySolution soln = results.nextSolution();

            RDFNode elementTypeNode = soln.get("elementType");
            String elementType = formatIRI(elementTypeNode.toString());

            if (this.equalsToSm4cepElement(elementType, "Event"))
                cepElement = this.getEventInRule(cepElementIRI, ruleIRI);
            else if (this.equalsToSm4cepElement(elementType, "TimeEvent"))
                cepElement = this.getTimeEvent(cepElementIRI);
                //else if (this.equalsToSm4cepElement(cepElementIRI, "Pattern"))
                //	cepElement = this.getPattern(cepElementIRI, ruleIRI);
            else if (this.equalsToSm4cepElement(elementType, "TemporalPattern"))
                cepElement = this.getPattern(cepElementIRI, ruleIRI);
            else if (this.equalsToSm4cepElement(elementType, "LogicPattern"))
                cepElement = this.getPattern(cepElementIRI, ruleIRI);
        }
        else {
            throw new CEPElementException("The rule has no CEP elements!");
        }
        if (results.hasNext()) {
            throw new CEPElementException("The rule has more than one CEP element!");
        }

        return cepElement;
    }

    /*****   GET EVENT   *****/

    // get event for an event IRI
    public Event getEventInRule (String eventIRI, String ruleIRI) throws CEPElementException {
        Event event = new Event (eventIRI);

        EventSchema eventSchema = this.getEventSchema(eventIRI);
        event.setEventSchema(eventSchema);

        LinkedList<SimpleClause> filters = this.getFiltersOverEvent(ruleIRI, eventIRI);
        event.setFilters(filters);

        return event;
    }

    // get event schema for an event IRI
    public EventSchema getEventSchema (String eventIRI) throws CEPElementException{

        EventSchema eventSchema = null;

        String qGetEventSchemaIRI =

                "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +

                        " SELECT DISTINCT ?eventSchemaIRI \n" +
                        " WHERE { \n" +
                        eventIRI + " sm4cep:hasEventSchema ?eventSchemaIRI . \n" +
                        "?eventSchemaIRI a sm4cep:EventSchema . \n" +
                        "} ";

        ResultSet results = this.runAQuery(qGetEventSchemaIRI, endpoint);
        String eventSchemaIRI = "";

        if (results.hasNext()) {
            QuerySolution soln = results.nextSolution();

            RDFNode eventSchemaNode = soln.get("eventSchemaIRI");
            eventSchemaIRI = formatIRI(eventSchemaNode.toString());
        }
        else {
            throw new CEPElementException("Event does not have schema defined!");
        }
        if (results.hasNext()){
            throw new CEPElementException("Event has more than 1 schema defined!");
        }

        if (this.eventSchemata.containsKey(eventSchemaIRI)){ // event schema already exists
            eventSchema = this.eventSchemata.get(eventSchemaIRI);
        }
        else { // we need to retrieve the element schema
            eventSchema = new EventSchema(eventSchemaIRI);

            String qGetEventSchemaAttributes =

                    "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +
                            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +

                            " SELECT DISTINCT ?eventAttribute \n" +
                            " WHERE { \n" +
                            eventSchemaIRI + " sm4cep:hasEventAttribute ?eventAttribute . \n" +
                            "?eventAttribute a sm4cep:EventAttribute . \n" +
                            "} ";

            ResultSet results2 = this.runAQuery(qGetEventSchemaAttributes, endpoint);

            while (results2.hasNext()) {
                QuerySolution soln2 = results2.nextSolution();

                RDFNode eventAttributeNode = soln2.get("eventAttribute");
                String eventAttributeString = formatIRI(eventAttributeNode.toString());

                Attribute eventAttribute = new Attribute(eventAttributeString); // TODO: we don't know element type here... in principle, we can always set it to string as default value
                eventAttribute.setEvent(eventSchema);
                eventAttribute.setAttributeType(AttributeType.TYPE_STRING);
                eventAttribute.setName(this.getLastIRIWord(eventAttributeString)); // adding this for translation to ESPER
                this.eventAttributes.put(eventAttributeString, eventAttribute); // add attribute to the list of all attributes (needed for the filters definition)
                eventSchema.addAttribute(eventAttribute); // TODO: here we can add duplicate check, i.e., if the list already contains this elements
            }

            eventSchema.setEventName(this.getLastIRIWord(eventSchemaIRI)); // adding this for translation to ESPER
            this.eventSchemata.put(eventSchemaIRI, eventSchema);
        }

        if (eventSchema != null)
            return eventSchema;
        else
            throw new CEPElementException ("No event schema...");
    }

    // get filters for an event
    public LinkedList<SimpleClause> getFiltersOverEvent (String ruleIRI, String eventIRI) throws CEPElementException{

        LinkedList<SimpleClause> filters = new LinkedList<SimpleClause> (); // list of all filters, i.e., simple clauses in a rule for an event

        String qGetFilters =

                "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +

                        " SELECT DISTINCT ?filter \n" +
                        " WHERE { \n" +
                        ruleIRI + " sm4cep:hasFilter ?filter . \n" +
                        " ?filter a sm4cep:SimpleClause . \n" +
                        " ?filter sm4cep:hasLeftOperand ?attribute . \n" +
                        " ?attribute sm4cep:forEvent " + eventIRI + " . \n" +
                        "} ";

        ResultSet results = this.runAQuery(qGetFilters, endpoint);

        while (results.hasNext()) {
            QuerySolution soln = results.nextSolution();

            RDFNode filterNode = soln.get("filter");
            String filterString = formatIRI(filterNode.toString());
            filters.add(this.getFilter(filterString));
        }

        return filters;
    }


    public SimpleClause getFilter (String filterIRI) throws CEPElementException{ // here the assumption of well formatting is that the attribute operand is always on the left side,
        // while the right operand is literal and the operator needs to be an SM4CEP comparison operator
        SimpleClause filter = new SimpleClause(filterIRI);

        String qGetFilter =

                "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +

                        " SELECT DISTINCT ?left ?leftType ?right ?rightType ?operator \n" +
                        " WHERE { \n" +
                        filterIRI + " sm4cep:hasLeftOperand ?left . \n" +
                        "?left a ?leftType . \n" +
                        filterIRI + " sm4cep:hasRightOperand ?right . \n" +
                        filterIRI + " sm4cep:hasComparisonOperator ?operator . \n" +
                        "OPTIONAL { ?right a ?rightType . } \n" +
                        "} ";

        ResultSet results = this.runAQuery(qGetFilter, endpoint);

        if (results.hasNext()) {
            QuerySolution soln = results.nextSolution();

            // getting left (i.e., number 1) operand
            RDFNode leftTypeNode = soln.get("leftType");
            String leftTypeIRI = formatIRI(leftTypeNode.toString());
            if (this.equalsToSm4cepElement(leftTypeIRI, "UsedAttribute")) {

                RDFNode leftNode = soln.get("left");
                String leftOperandIRI = formatIRI(leftNode.toString());

                String qGetAttributeType =

                        "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +

                                " SELECT DISTINCT ?attributeType \n" +
                                " WHERE { \n" +
                                leftOperandIRI + " sm4cep:forAttribute ?attributeType . \n" +
                                "} ";

                ResultSet results2 = this.runAQuery(qGetAttributeType, endpoint);
                String attributeTypeIRI;
                if (results2.hasNext()) {
                    QuerySolution soln2 = results2.nextSolution();

                    // getting left (i.e., number 1) operand
                    RDFNode attributeTypeNode = soln2.get("attributeType");
                    attributeTypeIRI = formatIRI(attributeTypeNode.toString());
                }
                else {
                    throw new CEPElementException("Used attribute does not have event attribute type defined!");
                }
                if (results2.hasNext()) {
                    throw new CEPElementException("Used attribute has more than one event attribute type defined!");
                }

                Attribute eAttribute = this.eventAttributes.get(attributeTypeIRI); // the event attribute should have been already retrieved with the event schema
                if (eAttribute != null){
                    eAttribute.setOperandType(Operand.OperandType.other); // since it is not 'group by' neither 'having', it is 'other'
                    filter.setOperand1(eAttribute);
                }
                else {
                    throw new CEPElementException ("The definition of the event attribute is not retrieved with the event schema definition!");
                }
            }
            else {
                throw new CEPElementException("Left operand is not (defined as) an event attribute!");
            }

            // getting right (i.e., number 2) operand
            RDFNode rightTypeNode = soln.get("rightType");
            if (rightTypeNode == null){ // if it is literal it won't have the type
                //String rightTypeIRI = formatIRI(rightTypeNode.toString());
                //if (this.equalsToSm4cepElement(rightTypeIRI, "Literal")) {

                RDFNode rightNode = soln.get("right");
                String rightOperand = formatIRI(rightNode.toString());
                if (rightOperand != null){
                    LiteralOperand rightOp = new LiteralOperand();
                    rightOp.setValue(rightOperand);
                    rightOp.setOperandType(Operand.OperandType.other); // since it is a literal it belongs to 'other'
                    filter.setOperand2(rightOp);
                }
                else {
                    throw new CEPElementException ("The literal in the filter is not defined!");
                }
                //}
            }
            else {
                throw new CEPElementException("Right operand is not (defined as) an event attribute!");
            }

            // getting operator
            RDFNode operatorNode = soln.get("operator");
            String operatorIRI = formatIRI(operatorNode.toString());

            ComparasionOperator operator = this.getComparisonOperator(operatorIRI);

            if (operator != null) {
                filter.setOperator(operator);
            }
            else {
                throw new CEPElementException("Operator does not belong to the SM4CEP comparison operators!");
            }
        }
        else {
            throw new CEPElementException ("Filter misses  internals!");
        }
        if (results.hasNext()) {
            throw new CEPElementException("More than one result for elements belonging to a single filter!");
        }

        return filter;
    }


    /*****   GET TIME EVENT   *****/

    // get time event event IRI
    public TimeEvent getTimeEvent (String timeEventIRI) throws CEPElementException {
        TimeEvent timeEvent = new TimeEvent (timeEventIRI);

        String qGetTimeStamp =

                "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +

                        " SELECT DISTINCT ?timeStamp \n" +
                        " WHERE { \n" +
                        timeEventIRI + " sm4cep:hasTimeStamp ?timeStamp . \n" +
                        "} ";

        ResultSet results2 = this.runAQuery(qGetTimeStamp, endpoint);

        if (results2.hasNext()) {
            QuerySolution soln2 = results2.nextSolution();

            RDFNode timeStampNode = soln2.get("timeStamp");
            String timeStamp = formatIRI(timeStampNode.toString());

            timeEvent.setTimestamp(timeStamp);
        }
        else {
            throw new CEPElementException("Time event does not have time stamp!");
        }
        if (results2.hasNext()) {
            throw new CEPElementException("Time event has more than one time stamp!");
        }

        return timeEvent;
    }


    /*****   GET PATTERN   *****/

    // get CEP pattern
    public Pattern getPattern(String patternIRI, String ruleIRI) throws CEPElementException {
        Pattern pattern = null;

        String qGetPatternType =

                "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +

                        " SELECT DISTINCT ?patternType \n" +
                        " WHERE { \n" +
                        patternIRI + " a ?patternType . \n" +
                        "} ";

        ResultSet results = this.runAQuery(qGetPatternType, endpoint);

        if (results.hasNext()) { // ASSUMPTION IS THAT A PATTERN IS OF A SINGLE TYPE
            QuerySolution soln = results.nextSolution();

            RDFNode patternTypeNode = soln.get("patternType");
            String patternType = formatIRI(patternTypeNode.toString());

            if (this.equalsToSm4cepElement(patternType, "TemporalPattern"))
                pattern = this.getTemporalPattern(patternIRI, ruleIRI);
            else if (this.equalsToSm4cepElement(patternType, "LogicPattern"))
                pattern = this.getLogicPattern(patternIRI, ruleIRI);
        }
        else {
            throw new CEPElementException("The pattern has no type!");
        }
        if (results.hasNext()) {
            throw new CEPElementException("The pattern has more than one type!");
        }

        return pattern;
    }

    // get complex event
    public TemporalPattern getTemporalPattern(String patternIRI, String ruleIRI) throws CEPElementException {
        TemporalPattern temporalPattern = new TemporalPattern(patternIRI) ;

        String qGetTemporalOperator =

                "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +

                        " SELECT DISTINCT ?temporalOperator \n" +
                        " WHERE { \n" +
                        patternIRI + " sm4cep:usesTemporalOperator ?temporalOperator . \n" +
                        "} ";

        ResultSet results = this.runAQuery(qGetTemporalOperator, endpoint);

        if (results.hasNext()) { // ASSUMPTION IS THAT A PATTERN IS OF A SINGLE TYPE
            QuerySolution soln = results.nextSolution();

            RDFNode operatorNode = soln.get("temporalOperator");
            String operator = formatIRI(operatorNode.toString());

            temporalPattern.setTemporalOperator(this.getTemporalOperator(operator));
        }
        else {
            throw new CEPElementException("The temporal pattern has no operator!");
        }
        if (results.hasNext()) {
            throw new CEPElementException("The temporal pattern has more than one operator!");
        }

        temporalPattern.setCEPElements(this.getCEPElementsList(patternIRI, ruleIRI));


        return temporalPattern;
    }

    // get complex event
    public LogicPattern getLogicPattern(String patternIRI, String ruleIRI) throws CEPElementException {
        LogicPattern logicPattern = new LogicPattern(patternIRI) ;

        String qGetLogicOperator =

                "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +

                        " SELECT DISTINCT ?logicOperator \n" +
                        " WHERE { \n" +
                        patternIRI + " sm4cep:usesLogicOperator ?logicOperator . \n" +
                        "} ";

        ResultSet results = this.runAQuery(qGetLogicOperator, endpoint);

        if (results.hasNext()) { // ASSUMPTION IS THAT A PATTERN IS OF A SINGLE TYPE
            QuerySolution soln = results.nextSolution();

            RDFNode operatorNode = soln.get("logicOperator");
            String operator = formatIRI(operatorNode.toString());

            logicPattern.setLogicOperator(this.getLogicOperator(operator));
        }
        else {
            throw new CEPElementException("The logic pattern has no operator!");
        }
        if (results.hasNext()) {
            throw new CEPElementException("The logic pattern has more than one operator!");
        }

        logicPattern.setCEPElements(this.getCEPElementsList(patternIRI, ruleIRI));


        return logicPattern;
    }

    // get the list of events for an event pattern
    public LinkedList<CEPElement> getCEPElementsList(String patternIRI, String ruleIRI) throws CEPElementException {

        LinkedList<CEPElement> elementsList = new LinkedList<CEPElement>();

        String qGetPatternList =

                "PREFIX sm4cep: <" + this.sm4cepNamespace + "> \n" +

                        " SELECT DISTINCT ?element ?order \n" +
                        " WHERE { \n" +
                        patternIRI + " sm4cep:containsElement ?includedElement . \n" +
                        "?includedElement sm4cep:representsElement ?element . \n" +
                        "?includedElement sm4cep:hasElementOrder ?order . \n" +
                        "} ";

        ResultSet results = this.runAQuery(qGetPatternList, endpoint);

        List<Tuple2<Integer,CEPElement>> unorderedList = Lists.newArrayList();
        while (results.hasNext()) {
            QuerySolution soln = results.nextSolution();

            RDFNode elementNode = soln.get("element");
            String elementString = formatIRI(elementNode.toString());

            RDFNode orderNode = soln.get("order");
            CEPElement elementInList = this.getCEPElement(elementString, ruleIRI); // this will be recursive call until it is resolved with simple elements or a complex element that has no elements
            unorderedList.add(new Tuple2<Integer,CEPElement>(orderNode.asLiteral().getInt(),elementInList));
        }

        unorderedList.sort((a, b) -> {
            return (a._1 < b._1) ? -1 : a._1 == b._1 ? 0 : 1;
        });
        unorderedList.forEach(t -> elementsList.add(t._2));

        return elementsList;
    }

    /*****   GET CONDITION   *****/

    public Condition getConditionForRule (String ruleIRI) throws ConditionException {
        Condition condition;
        String qGetCondition =

                "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +

                        " SELECT DISTINCT ?c \n" +
                        " WHERE { \n" +
                        ruleIRI + " a sm4cep:Rule . \n" +
                        ruleIRI + " sm4cep:hasCondition ?c . \n" +
                        //"?c a sm4cep:Condition . \n" +
                        "} ";

        ResultSet results = this.runAQuery(qGetCondition, endpoint);

        if (results.hasNext()) {
            QuerySolution soln = results.nextSolution();

            if (results.hasNext())
                throw new ConditionException("Rule has more than one Condition element!");

            RDFNode cNode = soln.get("c");
            String cIRI = formatIRI(cNode.toString());

            condition = this.getCondition(cIRI);
        } else {
            throw new ConditionException("Rule has no conditions!");
        }
        return condition;
    }

    // get condition
    public Condition getCondition (String conditionIRI) throws ConditionException {
        Condition condition = null;

        String qGetConditionType =

                "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +

                        " SELECT DISTINCT ?conditionType \n" +
                        " WHERE { \n" +
                        conditionIRI + " a ?conditionType . \n" +
                        "} ";

        ResultSet results = this.runAQuery(qGetConditionType, endpoint);

        if (results.hasNext()) { // ASSUMPTION IS THAT A PATTERN IS OF A SINGLE TYPE
            QuerySolution soln = results.nextSolution();

            RDFNode conditionTypeNode = soln.get("conditionType");
            String conditionType = formatIRI(conditionTypeNode.toString());

            if (this.equalsToSm4cepElement(conditionType, "SimpleClause"))
                condition = this.getSimpleClause(conditionIRI);
            else if (this.equalsToSm4cepElement(conditionType, "ComplexPredicate"))
                condition = this.getComplexPredicate(conditionIRI);
        }
        else {
            throw new ConditionException("The condition has no type!");
        }
        if (results.hasNext()) {
            throw new ConditionException("The condition has more than one type!");
        }

        return condition;
    }

    // get simple clause
    public SimpleClause getSimpleClause(String conditionIRI) throws ConditionException {
        SimpleClause clause = new SimpleClause(conditionIRI);

        Operand left = this.getSimpleClauseLeftOperand(conditionIRI);
        if (left != null) {
            clause.setOperand1(left);

            try {
                Operand right = this.getSimpleClauseRightOperand(conditionIRI);
                ComparasionOperator operator = this.getSimpleClauseOperator(conditionIRI);

                if (right != null && operator != null) {
                    clause.setOperand2(right);
                    clause.setOperator(operator);
                }
            } catch (ConditionException ce) {
                if (!ce.toString().equalsIgnoreCase("upc.edu.cep.sm4cep.ConditionException: Right operand is missing!"))
                    throw new ConditionException (ce.toString());
            }
        }
        else {
            throw new ConditionException ("No content for the simple clause!");
        }

        return clause;


    }

    // get the left operand of a simple clause,
    //NOTE: assumption of well formatting is that the left operand needs to have a type, i.e., needs to be an attribute or a function. Otherwise the exception is thrown
    public Operand getSimpleClauseLeftOperand (String conditionIRI) throws ConditionException {
        String qGetLeftOperand =

                "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +

                        " SELECT ?left ?leftType \n" +
                        " WHERE { \n" +
                        conditionIRI + " sm4cep:hasLeftOperand ?left . \n" +
                        "?left a ?leftType . \n" +
                        "} ";

        ResultSet results = this.runAQuery(qGetLeftOperand, endpoint);

        String leftOperandIRI = "";
        String leftOperandTypeIRI = "";

        if (results.hasNext()) {
            QuerySolution soln = results.nextSolution();

            RDFNode leftNode = soln.get("left");
            leftOperandIRI = formatIRI(leftNode.toString());
            //System.out.println("Left: " + leftIRI);

            RDFNode leftTypeNode = soln.get("leftType");
            leftOperandTypeIRI = formatIRI(leftTypeNode.toString());
            //System.out.println("Operator: " + leftTypeIRI);
        }
        else {
            throw new ConditionException("Left operand is missing!");
        }
        if (results.hasNext()){
            throw new ConditionException("There is more than one left operand in a condition!");
        }
        return getOperand (leftOperandIRI, leftOperandTypeIRI);
    }

    // get the right operand of a simple clause
    //NOTE: assumption of well formatting is that the right operand may not have a type, and in that case it is considered as literal (i.e., if the operand has a value but not a type). Otherwise, an exception is thrown
    public Operand getSimpleClauseRightOperand (String conditionIRI) throws ConditionException {
        String qGetRightOperand =

                "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +

                        " SELECT ?right ?rightType \n" +
                        " WHERE { \n" +
                        conditionIRI + " sm4cep:hasRightOperand ?right . \n" +
                        "?right a ?rightType . \n" +
                        "} ";

        ResultSet results = this.runAQuery(qGetRightOperand, endpoint);

        String rightOperandIRI = "";
        String rightOperandTypeIRI = "";

        if (results.hasNext()) {
            QuerySolution soln = results.nextSolution();

            RDFNode rightNode = soln.get("right");
            rightOperandIRI = formatIRI(rightNode.toString());
            //System.out.println("Left: " + leftIRI);

            RDFNode rightTypeNode = soln.get("rightType");
            rightOperandTypeIRI = formatIRI(rightTypeNode.toString());
            //System.out.println("Operator: " + leftTypeIRI);
        }
        else { // here it may happen that it is a literal so it does not have a type, and that is what we check in this branch
            qGetRightOperand = // version without type

                    "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +

                            " SELECT ?right \n" +
                            " WHERE { \n" +
                            conditionIRI + " sm4cep:hasRightOperand ?right . \n" +
                            "} ";

            results = this.runAQuery(qGetRightOperand, endpoint);

            if (results.hasNext()) {
                QuerySolution soln = results.nextSolution();

                RDFNode rightNode = soln.get("right");
                rightOperandIRI = formatIRI(rightNode.toString());
                //System.out.println("Left: " + leftIRI);
                rightOperandTypeIRI = "sm4cep:Literal";
            }
            else
                throw new ConditionException("Right operand is missing!");
        }
        if (results.hasNext()){
            throw new ConditionException("There is more than one right operand in a condition!");
        }
        return getOperand (rightOperandIRI, rightOperandTypeIRI);
    }

    // get the operator of a simple clause
    // NOTE: the operator is always a comparison type operator
    public ComparasionOperator getSimpleClauseOperator (String conditionIRI) throws ConditionException {
        String qGetOperator =

                "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +

                        " SELECT ?operator \n" +
                        " WHERE { \n" +
                        conditionIRI + " sm4cep:hasComparisonOperator ?operator . \n" +
                        "} ";

        ResultSet results = this.runAQuery(qGetOperator, endpoint);

        String operatorIRI = "";

        if (results.hasNext()) {
            QuerySolution soln = results.nextSolution();

            RDFNode operatorNode = soln.get("operator");
            operatorIRI = formatIRI(operatorNode.toString());
            //System.out.println("Left: " + leftIRI);

        }
        else {
            throw new ConditionException("Simple Clause Operator is missing!");
        }
        if (results.hasNext()){
            throw new ConditionException("There is more than one operator in a simple clause!");
        }
        return getComparisonOperator(operatorIRI);
    }


    // get operand of the simple clause
    public Operand getOperand (String operandIRI, String operandTypeIRI) throws ConditionException{
        Operand operand = null;

        if (this.equalsToSm4cepElement(operandTypeIRI, "UsedAttribute")) {
            operand = this.getAttributeAsOperand(operandIRI);
        }
        else if (this.equalsToSm4cepElement(operandTypeIRI, "Literal")){
            operand = this.getLiteralAsOperand(operandIRI);
        }
        // in the last case we consider that it is a function, and if not it should throw and exception
        else if (operandIRI != null && !operandIRI.equalsIgnoreCase("") && operandTypeIRI != null && !operandTypeIRI.equalsIgnoreCase("")){
            operand = this.getFunctionAsOperand(operandIRI, operandTypeIRI);
        }

        return operand;
    }

    public Attribute getAttributeAsOperand(String attributeIRI) throws ConditionException{
        Attribute eAttribute = null;

        String qGetAttributeType =

                "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +

                        " SELECT DISTINCT ?attributeType \n" +
                        " WHERE { \n" +
                        attributeIRI + " sm4cep:forAttribute ?attributeType . \n" +
                        "} ";

        ResultSet results2 = this.runAQuery(qGetAttributeType, endpoint);
        String attributeTypeIRI;
        if (results2.hasNext()) {
            QuerySolution soln2 = results2.nextSolution();

            // getting left (i.e., number 1) operand
            RDFNode attributeTypeNode = soln2.get("attributeType");
            attributeTypeIRI = formatIRI(attributeTypeNode.toString());
        }
        else {
            throw new ConditionException("Attribute operand does not have event attribute type defined!");
        }
        if (results2.hasNext()) {
            throw new ConditionException("Attribute operand has more than one event attribute type defined!");
        }

        eAttribute = this.eventAttributes.get(attributeTypeIRI); // the event attribute should have been already retrieved with the event schema
        if (eAttribute != null){
            eAttribute.setOperandType(Operand.OperandType.other); // since it is not 'group by' neither 'having', it is 'other'
        }
        else {
            throw new ConditionException ("The event attribute operand is not retrieved with the event schema definition!");
        }

        return eAttribute;
    }

    // get literal operand
    // NOTE: literals cannot have the IRI, thus LiteralOperand will never have IRI defined for its superclass
    // NOTE: also from the RDF we don't know the RDF type so it can either stay empty or be defined as string
    public LiteralOperand getLiteralAsOperand(String literalValue) throws ConditionException{
        LiteralOperand literal = null;

        if (literalValue != null && !literalValue.equalsIgnoreCase("") ){
            literal = new LiteralOperand();
            literal.setValue(literalValue);
            literal.setType(AttributeType.TYPE_STRING);

            return literal;
        }
        throw new ConditionException ("Literal value is empty or undefined");

    }

    // get function operand
    // NOTE: here we consider the following cases, the classical functions AVG, COUNT, MIN, MAX, SUM, and two special functions GROUP BY and HAVING
    // NOTE: in case of HAVING Having needs to be modeled as a complex predicate that on one side has:
    //- a simple clause with only left operand that is the function having, and
    //- one or more simple clauses that are predicates of having
    public FunctionOperand getFunctionAsOperand(String functionIRI, String functionTypeIRI) throws ConditionException {
        FunctionOperand function = null;

        // we use comparison not to tie it to a specific IRI

        if (functionTypeIRI.contains("sum") || functionTypeIRI.contains("Sum") || functionTypeIRI.contains("SUM")) {// we use comparison not to tie it to a specific IRI
            function = new FunctionOperand();
            function.setFunctionName("sum");
            function.setIRI(functionIRI);
            function.setParameters(this.getFunctionParametersList(functionIRI, functionTypeIRI));
        } else if (functionTypeIRI.contains("avg") || functionTypeIRI.contains("Avg") || functionTypeIRI.contains("AVG")) {
            function = new FunctionOperand();
            function.setFunctionName("avg");
            function.setIRI(functionIRI);
            function.setParameters(this.getFunctionParametersList(functionIRI, functionTypeIRI));
        } else if (functionTypeIRI.contains("min") || functionTypeIRI.contains("Min") || functionTypeIRI.contains("MIN")) {
            function = new FunctionOperand();
            function.setFunctionName("min");
            function.setIRI(functionIRI);
            function.setParameters(this.getFunctionParametersList(functionIRI, functionTypeIRI));
        } else if (functionTypeIRI.contains("max") || functionTypeIRI.contains("Max") || functionTypeIRI.contains("MAX")) {
            function = new FunctionOperand();
            function.setFunctionName("max");
            function.setIRI(functionIRI);
            function.setParameters(this.getFunctionParametersList(functionIRI, functionTypeIRI));
        } else if (functionTypeIRI.contains("cnt") || functionTypeIRI.contains("Cnt") || functionTypeIRI.contains("CNT") ||
                functionTypeIRI.contains("count") || functionTypeIRI.contains("Count") || functionTypeIRI.contains("COUNT")) {
            function = new FunctionOperand();
            function.setFunctionName("count");
            function.setIRI(functionIRI);
            function.setParameters(this.getFunctionParametersList(functionIRI, functionTypeIRI));
        } else if (functionTypeIRI.contains("having") || functionTypeIRI.contains("Having") || functionTypeIRI.contains("HAVING")) { // HAVING DOESN'T HAVE PARAMS but is defined as complex predicate
            function = new FunctionOperand();
            function.setFunctionName("having");
            function.setIRI(functionIRI);
        } else if (functionTypeIRI.contains("groupby") || functionTypeIRI.contains("Groupby") || functionTypeIRI.contains("GROUPBY") ||
                functionTypeIRI.contains("groupBy") || functionTypeIRI.contains("GroupBy")) {
            function = new FunctionOperand();
            function.setFunctionName("group by");
            function.setIRI(functionIRI);
            function.setParameters(this.getFunctionParametersList(functionIRI, functionTypeIRI));
        }
        else {
            throw new ConditionException ("Unknown operator!");
        }

        return function;
    }

    // get the list of function parameters for a function
    public LinkedList<FunctionParameter> getFunctionParametersList(String functionIRI, String functionTypeIRI) throws ConditionException {

        LinkedList<FunctionParameter> parametersList = new LinkedList<FunctionParameter>();

        String qGetFunctionList =

                "PREFIX sm4cep: <" + this.sm4cepNamespace + "> \n" +

                        " SELECT DISTINCT ?operand ?order \n" +
                        " WHERE { \n" +
                        functionIRI + " sm4cep:hasFunctionParameter ?includedParameter . \n" +
                        "?includedParameter sm4cep:representsOperand ?operand . \n" +
                        "?includedParameter sm4cep:hasFunctionParameterOrder ?order . \n" +
                        "} ";

        ResultSet results = this.runAQuery(qGetFunctionList, endpoint);

        List<Tuple2<Integer,FunctionParameter>> unorderedList = Lists.newArrayList();
        while (results.hasNext()) {
            QuerySolution soln = results.nextSolution();

            RDFNode operandNode = soln.get("operand");
            String operandString = formatIRI(operandNode.toString());

            RDFNode orderNode = soln.get("order");
            // this will be recursive call until it is resolved with a simple operand
            Operand operandInList = this.getOperandForFunctionParameter(operandString);
            FunctionParameter parameterInList = new FunctionParameter(operandInList, orderNode.asLiteral().getInt(), operandString);

            unorderedList.add(new Tuple2<Integer,FunctionParameter>(orderNode.asLiteral().getInt(),parameterInList));
        }
        unorderedList.sort((a, b) -> (a._1 < b._1) ? -1 : a._1 == b._1 ? 0 : 1);
        unorderedList.forEach(t -> parametersList.add(t._2));

        return parametersList;
    }

    // get the operand for a function parameter
    //NOTE: assumption of well formatting is that the operand may not have a type, and in that case it is considered as literal (i.e., if the operand has a value but not a type). Otherwise, an exception is thrown
    public Operand getOperandForFunctionParameter (String operandIRI) throws ConditionException {
        String	qGetOperandType =

                "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +

                        " SELECT ?type \n" +
                        " WHERE { \n" +
                        operandIRI + " a ?type . \n" +
                        "} ";

        ResultSet results = this.runAQuery(qGetOperandType, endpoint);

        String operandTypeIRI = "";

        if (results.hasNext()) {
            QuerySolution soln = results.nextSolution();

            RDFNode typeNode = soln.get("type");
            operandTypeIRI = formatIRI(typeNode.toString());
        }
        else {
            operandTypeIRI = "sm4cep:Literal";
        }
        if (results.hasNext()){
            throw new ConditionException("There is more than one type for a function parameter operand!");
        }

        return getOperand(operandIRI, operandTypeIRI);
    }


    // get complex predicate
    public ComplexPredicate getComplexPredicate(String conditionIRI) throws ConditionException {
        ComplexPredicate complexPredicate = new ComplexPredicate(conditionIRI) ;

        String qGetLogicOperator =

                "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +

                        " SELECT DISTINCT ?logicOperator \n" +
                        " WHERE { \n" +
                        conditionIRI + " sm4cep:hasLogicOperator ?logicOperator . \n" +
                        "} ";

        ResultSet results = this.runAQuery(qGetLogicOperator, endpoint);

        if (results.hasNext()) { // ASSUMPTION IS THAT A COMPLEX PREDICATE IS OF A SINGLE TYPE
            QuerySolution soln = results.nextSolution();

            RDFNode operatorNode = soln.get("logicOperator");
            String operator = formatIRI(operatorNode.toString());
            try{
                complexPredicate.setOperator(this.getLogicOperator(operator));
            }
            catch (CEPElementException e) {
                throw new ConditionException ("Problem with Logic Operation in a Complex Predicate!");
            }
        }
        else {
            throw new ConditionException("The complex predicate has no operator!");
        }
        if (results.hasNext()) {
            throw new ConditionException("The complex predicate has more than one operator!");
        }

        complexPredicate.setConditions(this.getComplexPredicateConditionList(conditionIRI)); //(this.getCEPElementsList(patternIRI, ruleIRI));

        return complexPredicate;
    }

    // get the list of events for an event pattern
    public LinkedList<Condition> getComplexPredicateConditionList(String conditionIRI) throws ConditionException {

        LinkedList<Condition> conditionsList = new LinkedList<Condition>();

        String qGetConditionsList =

                "PREFIX sm4cep: <" + this.sm4cepNamespace + "> \n" +

                        " SELECT DISTINCT ?condition ?order \n" +
                        " WHERE { \n" +
                        conditionIRI + " sm4cep:containsCondition ?includedCondition . \n" +
                        "?includedCondition sm4cep:representsCondition ?condition . \n" +
                        "?includedCondition sm4cep:hasConditionOrder ?order . \n" +
                        "} ";

        ResultSet results = this.runAQuery(qGetConditionsList, endpoint);

        List<Tuple2<Integer,Condition>> unorderedList = Lists.newArrayList();
        while (results.hasNext()) {
            QuerySolution soln = results.nextSolution();

            RDFNode conditionNode = soln.get("condition");
            String conditionString = formatIRI(conditionNode.toString());

            RDFNode orderNode = soln.get("order");
            // this will be recursive call until it is resolved with simple condition
            Condition conditionInList = this.getCondition(conditionString);
            unorderedList.add(new Tuple2<Integer,Condition>(orderNode.asLiteral().getInt(),conditionInList));
        }
        unorderedList.sort((a, b) -> (a._1 < b._1) ? -1 : a._1 == b._1 ? 0 : 1);
        unorderedList.forEach(t -> conditionsList.add(t._2));

        return conditionsList;
    }

    /*****   GET ACTION   *****/

    public Action getAction(String ruleIRI) throws ActionException{
        Action action = new Action();

        //retrieve action IRI
        String actionIRI = "";

        String qGetActionIRI =

                "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +

                        " SELECT DISTINCT ?actionIRI \n" +
                        " WHERE { \n" +
                        ruleIRI + " sm4cep:hasAction ?actionIRI . \n" +
                        "} ";

        ResultSet results = this.runAQuery(qGetActionIRI, endpoint);

        if (results.hasNext()) {
            QuerySolution soln = results.nextSolution();

            RDFNode actionNode = soln.get("actionIRI");
            actionIRI = formatIRI(actionNode.toString());
        }
        else {
            throw new ActionException("The rule has no action!");
        }
        if (results.hasNext()) {
            throw new ActionException("The rule has more than one action!");
        }
        action.setIRI(actionIRI);

        //retrieve event
        String eventIRI = "";

        String qGetEvent =

                "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +

                        " SELECT DISTINCT ?eventIRI \n" +
                        " WHERE { \n" +
                        actionIRI + " sm4cep:asEvent ?eventIRI . \n" +
                        "} ";

        results = this.runAQuery(qGetEvent, endpoint);

        if (results.hasNext()) {
            QuerySolution soln = results.nextSolution();

            RDFNode eventNode = soln.get("eventIRI");
            eventIRI = formatIRI(eventNode.toString());
        }
        else {
            throw new ActionException("The action is not related to an event!");
        }
        if (results.hasNext()) {
            throw new ActionException("The action is related to more than one event!");
        }

        try {
            action.setAssociatedEvent(this.getEventInRule(eventIRI, ruleIRI));
        }
        catch (CEPElementException ex) {
            throw new ActionException ("An event associated to an action cannot be retrieved!");
        }

        //retrieve action parameters
        action.setActionAttributes(this.getActionOperands(actionIRI));

        return action;
    }

    // get the list of events for an event pattern
    //NOTE returns any operand as action attributes in the Action class
    public LinkedList<Operand> getActionOperands(String actionIRI) throws ActionException {

        LinkedList<Operand> operandsList = new LinkedList<Operand>();

        String qGetOperandsList =

                "PREFIX sm4cep: <" + this.sm4cepNamespace + "> \n" +

                        " SELECT DISTINCT ?operand ?order \n" +
                        " WHERE { \n" +
                        actionIRI + " sm4cep:hasActionParameter ?includedParameter . \n" +
                        "?includedParameter sm4cep:representedWith ?operand . \n" +
                        "?includedParameter sm4cep:hasActionParameterOrder ?order . \n" +
                        "} ";

        ResultSet results = this.runAQuery(qGetOperandsList, endpoint);

        List<Tuple2<Integer,Operand>> unorderedList = Lists.newArrayList();
        while (results.hasNext()) {
            QuerySolution soln = results.nextSolution();

            RDFNode operandNode = soln.get("operand");
            String operandString = formatIRI(operandNode.toString());

            RDFNode orderNode = soln.get("order");
            // this will be recursive call until it is resolved with a simple operand
            Operand operandInList = null;
            try {
                operandInList = this.getOperandForFunctionParameter(operandString);
            } catch (ConditionException e) {
                e.printStackTrace();
            }
            unorderedList.add(new Tuple2<Integer,Operand>(orderNode.asLiteral().getInt(),operandInList));
        }
        unorderedList.sort((a, b) -> (a._1 < b._1) ? -1 : a._1 == b._1 ? 0 : 1);
        unorderedList.forEach(t -> operandsList.add(t._2));

        return operandsList;
    }

    /*****   GET OPERATORS   *****/

    // get temporal operators - here we focus on within and sequence operators that are defined in our ontology
    public TemporalOperator getTemporalOperator(String operatorIRI) throws CEPElementException  {
        TemporalOperator tempOp = null;

        if (this.equalsToSm4cepElement(operatorIRI, "Sequence")) {
            tempOp = new Sequence(operatorIRI);
        } else {
            String qGetOperatorType =

                    "PREFIX sm4cep: <" + this.sm4cepNamespace + "> \n" +
                            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +

                            " SELECT DISTINCT ?type \n" +
                            " WHERE { \n" +
                            operatorIRI + " a ?type . \n" +
                            "} ";

            ResultSet results = this.runAQuery(qGetOperatorType, endpoint);

            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();

                RDFNode typeNode = soln.get("type");
                String type = formatIRI(typeNode.toString());
                //String offset = formatIRI(offsetNode.toString());

                if (this.equalsToSm4cepElement(type, "WithIn")) {
                    String qGetWithInOffset =

                            "PREFIX sm4cep: <" + this.sm4cepNamespace + "> \n" +
                                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +

                                    " SELECT DISTINCT ?offset \n" +
                                    " WHERE { \n" +
                                    operatorIRI + " sm4cep:hasOffset ?offset . \n" +
                                    "} ";

                    ResultSet results2 = this.runAQuery(qGetWithInOffset, endpoint);

                    if (results2.hasNext()) {
                        QuerySolution soln2 = results2.nextSolution();

                        // getting left (i.e., number 1) operand
                        RDFNode offsetNode = soln2.get("offset");
                        int offset = offsetNode.asLiteral().getInt();
                        //String offset = formatIRI(offsetNode.toString());

                        tempOp = new Within(offset, TimeUnit.millisecond, operatorIRI); // NOTE: Here we assume that the offset is in milliseconds
                    }
                    else {
                        throw new CEPElementException("There is no offset for the within operator!");
                    }
                    if (results.hasNext()) {
                        throw new CEPElementException("There is more than one offset for the within operator!");
                    }
                }
            }
        }

        return tempOp;
    }

    // get logic operator - covering the ones available in the ontology
    public LogicOperator getLogicOperator(String operatorIRI) throws CEPElementException {
        LogicOperator operator = null;

        if (this.equalsToSm4cepElement(operatorIRI, "Conjunction"))
            operator = new LogicOperator(LogicOperatorEnum.Conjunction);
        else if (this.equalsToSm4cepElement(operatorIRI, "Disjunction"))
            operator = new LogicOperator(LogicOperatorEnum.Disjunction);
        else if (this.equalsToSm4cepElement(operatorIRI, "Negation"))
            operator = new LogicOperator(LogicOperatorEnum.Negation);

        return operator;
    }

    // get comparison operator - focusing on the operators specified in our ontology
    public ComparasionOperator getComparisonOperator(String operatorIRI) {
        ComparasionOperator operator = null;

        if (this.equalsToSm4cepElement(operatorIRI, "Equal"))
            operator = new ComparasionOperator(ComparasionOperatorEnum.EQ);
        else if (this.equalsToSm4cepElement(operatorIRI, "NotEqual"))
            operator = new ComparasionOperator(ComparasionOperatorEnum.NE);
        else if (this.equalsToSm4cepElement(operatorIRI, "GreaterThan"))
            operator = new ComparasionOperator(ComparasionOperatorEnum.GT);
        else if (this.equalsToSm4cepElement(operatorIRI, "GreaterOrEqual"))
            operator = new ComparasionOperator(ComparasionOperatorEnum.GE);
        else if (this.equalsToSm4cepElement(operatorIRI, "LessThan"))
            operator = new ComparasionOperator(ComparasionOperatorEnum.LT);
        else if (this.equalsToSm4cepElement(operatorIRI, "LessOrEqual"))
            operator = new ComparasionOperator(ComparasionOperatorEnum.LE);

        return operator;
    }

    /*****   RDF MANAGEMENT AND QUERYING SPARQL ENDPOINT   *****/

    private ResultSet runAQuery(String sparqlQuery, String endpoint) {
        /*
        Query query = QueryFactory.create(sparqlQuery);
        QueryExecution qExe = QueryExecutionFactory.sparqlService(endpoint, query);
        ResultSet results = qExe.execSelect();
        return ResultSetFactory.copyResults(results);
        */
        Dataset dataset = Utils.getTDBDataset();
        dataset.begin(ReadWrite.READ);

        ResultSet res = null;

        try (QueryExecution qExec = QueryExecutionFactory.create(QueryFactory.create(sparqlQuery), dataset)) {
            res = ResultSetFactory.copyResults(qExec.execSelect());
        } catch (Exception e) {
            e.printStackTrace();
        }

        dataset.end();
        dataset.close();
        return res;
    }

    // format IRI if it is not surrounded with brackets and it should be
    public String formatIRI(String iri) {
        if (iri.startsWith("http://"))
            return "<" + iri + ">";
        else
            return iri;
    }

    // get the last word from an IRI
    private String getLastIRIWord (String iri) {
        String[] slashTokens = iri.split("/");
        String[] hashTagTokens = slashTokens[slashTokens.length-1].split("#");
        String[] colonTagTokens = hashTagTokens[hashTagTokens.length-1].split(":");
        String iriEnding = colonTagTokens[colonTagTokens.length-1]; // in case there is a prefix before

        if (iriEnding != null && iriEnding.length() > 0 && iriEnding.charAt(iriEnding.length() - 1) == '>') {
            iriEnding = iriEnding.substring(0, iriEnding.length() - 1);
        }

        return iriEnding;
    }

    // compare if a string belongs to an sm4cep element
    public boolean equalsToSm4cepElement(String retrievedIRI, String sm4cepElement ) {
        if (retrievedIRI.equalsIgnoreCase("sm4cep:" + sm4cepElement) ||
                retrievedIRI.equalsIgnoreCase(sm4cepNamespace + sm4cepElement) ||
                retrievedIRI.equalsIgnoreCase("<"+ sm4cepNamespace + sm4cepElement +">"))
            return true;
        return false;
    }

    // getters and setters
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    // stringing methods
    public String stringWindow(Window w) {
        String text = "";
        text += "Window with values: \n";
        text += "  window kind: " + w.getWindowType().toString() + "\n";
        if (w.getTimeUnit() != null)
            text += "  window unit: " + w.getTimeUnit().toString() + "\n";
        else
            text += " window unit: event unit";

        return text;
    }

    public void getAllEventSchemata () throws CEPElementException{

        String qGetEventSchemaIRI =

                "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +

                        " SELECT DISTINCT ?eventSchemaIRI \n" +
                        " WHERE { \n" +
                        "?eventSchemaIRI a sm4cep:EventSchema . \n" +
                        "} ";

        ResultSet results = this.runAQuery(qGetEventSchemaIRI, endpoint);
        String eventSchemaIRI = "";

        if (!results.hasNext())
            throw new CEPElementException("Not a single event schema!");
        while (results.hasNext()) {
            QuerySolution soln = results.nextSolution();

            RDFNode eventSchemaNode = soln.get("eventSchemaIRI");
            eventSchemaIRI = formatIRI(eventSchemaNode.toString());

            if (!this.eventSchemata.containsKey(eventSchemaIRI)){ // we need to retrieve the element schema
                EventSchema eventSchema = new EventSchema(eventSchemaIRI);

                String qGetEventSchemaAttributes =

                        "PREFIX sm4cep: <" + sm4cepNamespace + "> \n" +
                                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +

                                " SELECT DISTINCT ?eventAttribute \n" +
                                " WHERE { \n" +
                                eventSchemaIRI + " sm4cep:hasEventAttribute ?eventAttribute . \n" +
                                "?eventAttribute a sm4cep:EventAttribute . \n" +
                                "} ";

                ResultSet results2 = this.runAQuery(qGetEventSchemaAttributes, endpoint);

                while (results2.hasNext()) {
                    QuerySolution soln2 = results2.nextSolution();

                    RDFNode eventAttributeNode = soln2.get("eventAttribute");
                    String eventAttributeString = formatIRI(eventAttributeNode.toString());

                    Attribute eventAttribute = new Attribute(eventAttributeString); // TODO: we don't know element type here... in principle, we can always set it to string as default value
                    eventAttribute.setEvent(eventSchema);
                    eventAttribute.setAttributeType(AttributeType.TYPE_STRING);
                    eventAttribute.setName(this.getLastIRIWord(eventAttributeString)); // adding this for translation to ESPER
                    this.eventAttributes.put(eventAttributeString, eventAttribute); // add attribute to the list of all attributes (needed for the filters definition)
                    eventSchema.addAttribute(eventAttribute); // TODO: here we can add duplicate check, i.e., if the list already contains this elements
                }

                eventSchema.setEventName(this.getLastIRIWord(eventSchemaIRI)); // adding this for translation to ESPER
                this.eventSchemata.put(eventSchemaIRI, eventSchema);
            }

        }
    }
}