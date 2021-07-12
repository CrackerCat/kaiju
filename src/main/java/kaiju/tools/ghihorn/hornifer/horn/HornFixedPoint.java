// package kaiju.tools.ghihorn.hornifer.horn;

// import java.util.Collection;
// import java.util.Set;
// import java.util.concurrent.ConcurrentHashMap;

// import com.google.common.base.Verify;
// import com.google.common.base.VerifyException;
// import com.microsoft.z3.BoolSort;
// import com.microsoft.z3.Fixedpoint;
// import com.microsoft.z3.FuncDecl;
// import com.microsoft.z3.Params;

// import ghidra.util.task.TaskMonitor;
// import kaiju.tools.ghihorn.hornifer.horn.element.HornFact;
// import kaiju.tools.ghihorn.hornifer.horn.element.HornPredicate;
// import kaiju.tools.ghihorn.hornifer.horn.variable.HornConstant;
// import kaiju.tools.ghihorn.z3.GhiHornContext;

// public class HornFixedPoint {
//     private HornPredicate goal;
//     private final Set<HornClause> rules;
//     private final Set<HornFact> facts;

//     public HornFixedPoint() {
//         goal = null;
//         rules = ConcurrentHashMap.newKeySet();
//         facts = ConcurrentHashMap.newKeySet();
//     }

//     public boolean addFact(final HornFact p) {
//         return this.facts.add(p);
//     }

//     public boolean addRule(final HornClause c) {
//         return this.rules.add(c);
//     }

//     public boolean addFacts(final Collection<HornFact> p) {
//         return this.facts.addAll(p);
//     }

//     public boolean addRules(final Collection<HornClause> c) {
//         return this.rules.addAll(c);
//     }

//     public void setGoal(final HornPredicate g) {
//         this.goal = g;
//     }

//     /**
//      * @return the goal
//      */
//     public HornPredicate getGoal() {
//         return goal;
//     }

//     /**
//      * @return the rules
//      */
//     public Set<HornClause> getRules() {
//         return rules;
//     }

//     /**
//      * @return the facts
//      */
//     public Set<HornFact> getFacts() {
//         return facts;
//     }

//     public boolean verify(TaskMonitor mon) {
//         try {
//             Verify.verify(goal != null, "You must specify a goal!");
//             Verify.verify(!this.rules.isEmpty(), "You must specify at least one rule!");
//             Verify.verify(!this.rules.isEmpty(), "You must specify at least one fact (such as the start fact)!");

//             return true;
//         } catch (VerifyException ve) {
//             mon.setMessage(ve.getMessage());
//         }

//         return false;
//     }

//     @Override
//     public String toString() {
//         final StringBuilder sb = new StringBuilder();
//         this.rules.forEach(s -> sb.append(s).append("\n"));
//         return sb.toString();
//     }

//     /**
//      * Create a fixed point out of created relations & rules
//      * 
//      * @return the createed Fixedpoint
//      */
//     public synchronized Fixedpoint instantiate(final GhiHornContext context) {

//         Params parameters = context.mkParams();

//         parameters.add("fp.engine", "spacer");
//         parameters.add("fp.xform.inline_eager", false);
//         parameters.add("fp.xform.slice", false);
//         parameters.add("fp.xform.inline_linear", false);
//         parameters.add("fp.xform.subsumption_checker", false);
//         parameters.add("fp.datalog.generate_explanations", true);

//         // Combine all the facts, rules and relations from the program
//         final Fixedpoint fx = context.mkFixedpoint();
//         synchronized (fx) {

//             fx.setParameters(parameters);

//             for (HornFact fact : facts) {
//                 final FuncDecl<BoolSort> factDecl = fact.declare(context);
//                 fx.registerRelation(factDecl);
//                 int varVals[] = fact.getVariableInstances().stream().sequential().filter(vi -> vi != null)
//                         // Get each expression
//                         .map(v -> v.getExpression())
//                         // This may not be necessary
//                         .filter(x -> x instanceof HornConstant)
//                         // Get the list of integers
//                         .mapToInt(n -> ((HornConstant) n).getValue().intValue()).toArray();

//                 fx.addFact(factDecl, varVals);
//             }

//             // Actually make the rule by instantiating the variables
//             for (final HornClause clause : rules) {

//                 HornRuleExpr rule = clause.instantiate(context);

//                 fx.registerRelation(rule.getBodyDecl());
//                 fx.registerRelation(rule.getHeadDecl());

//                 fx.addRule(rule.getRuleExpr(), rule.getNameSymbol());
//             }
//         }
//         return fx;
//     }
// }
