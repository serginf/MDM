package eu.supersede.mdm.storage.cep.Interpreter;

import eu.supersede.mdm.storage.cep.RDF_Model.Operators.*;
import eu.supersede.mdm.storage.cep.RDF_Model.Rule;
import eu.supersede.mdm.storage.cep.RDF_Model.action.Action;
import eu.supersede.mdm.storage.cep.RDF_Model.condition.*;
import eu.supersede.mdm.storage.cep.RDF_Model.event.*;
import eu.supersede.mdm.storage.cep.RDF_Model.window.Window;
import eu.supersede.mdm.storage.cep.RDF_Model.window.WindowType;

/**
 * Created by osboxes on 26/05/17.
 */
public class InterpreterTest {
    public static void main(String[] args) throws Exception {

        test2();

    }


    public static void test2() throws InterpreterException {
//        select a.custId, sum(b.price)
//        from pattern [every a=ServiceOrder ->
//                b=ProductOrder where timer:within(1 min)].win:time(2 hour)
//        where a.name = 'Repair' and b.custId = a.custId
//        group by a.custId
//        having sum(b.price) > 100

        //CEPElement and Attributes
        Attribute custIda = new Attribute();
        custIda.setName("custId");
        Attribute name = new Attribute();
        name.setName("name");

        EventSchema serviceOrdera = new EventSchema();
        serviceOrdera.setEventName("ServiceOrder");
        serviceOrdera.addAttribute(custIda);
        serviceOrdera.addAttribute(name);

        Event serviceOrder = new Event();
        serviceOrder.setEventSchema(serviceOrdera);

        custIda.setEvent(serviceOrdera);
        name.setEvent(serviceOrdera);

        Attribute price = new Attribute();
        price.setName("price");
        Attribute custIdb = new Attribute();
        custIdb.setName("custId");

        EventSchema productOrdera = new EventSchema();
        productOrdera.setEventName("ProductOrder");
        productOrdera.addAttribute(price);
        productOrdera.addAttribute(custIdb);

        Event productOrder = new Event();
        productOrder.setEventSchema(productOrdera);

        price.setEvent(productOrdera);
        custIdb.setEvent(productOrdera);

        Sequence sequence = new Sequence();
        TemporalPattern sequenceEvent = new TemporalPattern();
        sequenceEvent.setTemporalOperator(sequence);
        sequenceEvent.addEvents(serviceOrder);
        sequenceEvent.addEvents(productOrder);

        Within within = new Within();
        within.setOffset(1);
        within.setTimeUnit(TimeUnit.minute);

        TemporalPattern withinEvent = new TemporalPattern();
        withinEvent.setTemporalOperator(within);
        withinEvent.addEvents(sequenceEvent);

        //Window
        Window window = new Window();
        window.setTimeUnit(TimeUnit.hour);
        window.setWindowType(WindowType.TUMBLING_WINDOW);
        window.setWithin(2);

        //Action
        Action action = new Action();
        FunctionParameter parameter = new FunctionParameter();
        parameter.setOperand(price);
        //action.set
        Sum sum = new Sum(parameter);
        action.addActionAttribute(sum);
        action.addActionAttribute(custIda);

        //Condition
        LiteralOperand literal1 = new LiteralOperand();
        literal1.setType(AttributeType.TYPE_STRING);
        literal1.setValue("Repair");

        LiteralOperand literal2 = new LiteralOperand();
        literal2.setType(AttributeType.TYPE_INTEGER);
        literal2.setValue("100");

        FunctionParameter groupParameter1 = new FunctionParameter();
        groupParameter1.setOperand(custIda);
        GroupBy groupBy = new GroupBy(groupParameter1);

        ComplexPredicate allCondition = new ComplexPredicate();
        allCondition.setOperator(new LogicOperator(LogicOperatorEnum.Conjunction));

        SimpleClause c1 = new SimpleClause();
        c1.setOperand1(sum);
        c1.setOperator(new ComparasionOperator(ComparasionOperatorEnum.GT));
        c1.setOperand2(literal2);

        SimpleClause c2 = new SimpleClause();
        c2.setOperand1(groupBy);


        ComplexPredicate whereCondition = new ComplexPredicate();
        whereCondition.setOperator(new LogicOperator(LogicOperatorEnum.Conjunction));

        SimpleClause c3 = new SimpleClause();
        c3.setOperand1(name);
        c3.setOperator(new ComparasionOperator(ComparasionOperatorEnum.EQ));
        c3.setOperand2(literal1);

        SimpleClause c4 = new SimpleClause();
        c4.setOperand1(custIda);
        c4.setOperator(new ComparasionOperator(ComparasionOperatorEnum.EQ));
        c4.setOperand2(custIdb);

        whereCondition.getConditions().add(c3);
        whereCondition.getConditions().add(c4);


        allCondition.getConditions().add(c1);
        allCondition.getConditions().add(c2);
        allCondition.getConditions().add(whereCondition);


        //Rule
        Rule rule = new Rule();
        rule.setCEPElement(withinEvent);
        rule.setWindow(window);
        rule.setAction(action);
        rule.setCondition(allCondition);

        System.out.println(rule.interpret(InterpreterContext.ESPER));
    }

//    public static void test1() throws InterpreterException {
//        //select count(EventA.A) from pattern [every EventA where timer:within(2 sec)].win:time(2 hour)
//
//        Attribute a = new Attribute();
//        a.setName("A");
//
//        EventSchema event1 = new EventSchema();
//        event1.setEventName("EventA");
//        event1.addAttribute(a);
//
//        a.setEvent(event1);
//
//        Within within = new Within();
//        within.setOffset(2);
//        within.setTimeUnit(TimeUnit.second);
//
//        TemporalPattern temporalEvent = new TemporalPattern();
//        temporalEvent.setTemporalOperator(within);
//        temporalEvent.addEvents(event1);
//
//        Window window = new Window();
//        window.setTimeUnit(TimeUnit.hour);
//        window.setWindowType(WindowType.TUMBLING_WINDOW);
//        window.setWithin(2);
//
//        Action action = new Action();
//        FunctionParameter parameter = new FunctionParameter();
//        parameter.setOperand(a);
//        //action.set
//        Count count = new Count(parameter);
//        action.addActionAttribute(count);
//
//        Rule rule = new Rule();
//        rule.setCEPElement(temporalEvent);
//        rule.setWindow(window);
//        rule.setAction(action);
//
//        System.out.println(rule.interpret(InterpreterContext.ESPER));
//    }
}
