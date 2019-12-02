import java.util.ArrayList;

class RuleJunction{
    RuleJunction ruleIfTrue;
    RuleJunction ruleIfFalse;
    Rule junctionRule;
    LinearFunction linearFunctionIfTrue;
    LinearFunction linearFunctionIfFalse;

    RuleJunction(Rule junctionRule, RuleJunction ruleIfTrue, RuleJunction ruleIfFalse){
        this.ruleIfTrue = ruleIfTrue;
        this.ruleIfFalse = ruleIfFalse;
        this.junctionRule = junctionRule;
    }

    RuleJunction(Rule junctionRule, RuleJunction ruleIfTrue, LinearFunction linearFunctionIfFalse){
    }

    RuleJunction(Rule junctionRule, LinearFunction linearFunctionIfTrue, RuleJunction ruleIfFalse){
        
    }

    RuleJunction(Rule junctionRule, LinearFunction linearFunctionIfTrue, LinearFunction linearFunctionIfFalse){
        this.linearFunctionIfTrue = linearFunctionIfTrue;
        this.linearFunctionIfFalse = linearFunctionIfFalse;
    }

    Double evaluateJunction(ArrayList<String> keys, ArrayList<Double> values){
        if(this.junctionRule.evaluate(values.get(keys.indexOf(this.junctionRule.getVariableName())))){
            if(this.linearFunctionIfTrue != null){
                return this.linearFunctionIfTrue.evaluateFunction(keys, values);
            }else{
                return this.ruleIfTrue.evaluateJunction(keys, values);
            }
        }else{
            if(this.linearFunctionIfFalse != null){
                return this.linearFunctionIfFalse.evaluateFunction(keys, values);
            }else{
                return this.ruleIfFalse.evaluateJunction(keys, values);
            }
        }
    }
}