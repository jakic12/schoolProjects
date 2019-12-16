import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

class Parser{
    static String[] requiredArguments = new String[]{
        "model filename (String)",
        "dataset filename (String)",
        "output filename (String)"
    };

    public static void main(String[] args){
        try{
            args = new String[]{"tree1", "rConceptsCond.csv", "outputs/rConceptsCond.csv"};
            checkArgs(args);
    
            // read the model file
            File inputFile = new File("models/" + args[0]);
            FileInputStream fs = new FileInputStream(inputFile);
        
            String rawFile = "";
            
            while(fs.available() > 0){
                rawFile += (char)fs.read();
            }
            
            fs.close();
            
            //Build the linear function tree
            ArrayList<LinearFunction> linearFunctions = parseLinearFunctions(rawFile);
            HashMap<String, LinearFunction> linearFunctionsByLm = new HashMap<>();

            for(LinearFunction lf : linearFunctions){
                linearFunctionsByLm.put("LM" + lf.lmNumber, lf);
                System.out.println(lf);
            }

            RuleJunction treeTopJunction = parseRuleTree(rawFile.substring(
                "M5 pruned model tree:\r\n(using smoothed linear models)\r\n\r\n".length(),
                rawFile.indexOf("\r\n\r\nLM num")
            ), linearFunctionsByLm);

            LinkedHashMap<String, ArrayList<Double>> input = CsvManager.openCsv(new File("datasets/" + args[1]));

            for(int i = 0; i < input.get(input.keySet().iterator().next()).size(); i++){
                for(String key : input.keySet()){
                    System.out.print(key + ":" + input.get(key).get(i));
                } 
                System.out.println("");
            }

            //Regress
            ArrayList<Double> features = new ArrayList<>();

            ArrayList<String> keys = new ArrayList<>();
            ArrayList<ArrayList<Double>> values = new ArrayList<>();
            
            String className = "";
            for(int i = 0; i < input.get(input.keySet().iterator().next()).size(); i++){
                values.add(new ArrayList<>());
            
                int j = 0;
                for(String key : input.keySet()){
                    if(input.keySet().size()-1 > j){
                        if(i == 0)
                            keys.add(key);
                        values.get(i).add(input.get(key).get(i));
                    }else{
                        className = key;
                    }
                    j++;
                }
            }

            for(ArrayList<Double> row : values){
                features.add(treeTopJunction.evaluateJunction(keys, row));
            }
             
            LinkedHashMap<String, ArrayList<Double>> outWithFeatures = new LinkedHashMap<>();
            for(String key : keys){
                ArrayList<Double> outColumn = new ArrayList<>();
                if(keys.indexOf(key) != -1){
                    int indexOfKey = keys.indexOf(key);
                    for(int i = 0; i < values.size(); i++){
                        outColumn.add(values.get(i).get(indexOfKey));
                    }
                }
                
                outWithFeatures.put(key, outColumn);
            }
            
            outWithFeatures.put(className, input.get(className));
            outWithFeatures.put("features", features);

            System.out.println(outWithFeatures);

            CsvManager.writeToFile(new File(args[2]), outWithFeatures);

        }catch(FileNotFoundException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }catch(IOException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }catch(ProgramException e){
            System.out.println(e.getMessage());
        }
    }

    static ArrayList<LinearFunction> parseLinearFunctions(String input){
        String filteredInput = "LM num: 1\r\n" + input.split("LM num: 1\r\n")[1].split("\r\n\r\nNumber of Rules : ")[0];
        ArrayList<LinearFunction> out = new ArrayList<>();

        for(String fnct : filteredInput.split("\r\n\r\n")){
            String lmNum = fnct.split("LM num: ")[1].split("\r\n")[0];
            ArrayList<Double> coefficients = new ArrayList<>();
            ArrayList<String> keys = new ArrayList<>();

            for(String ckPair : fnct.split("class = \r\n")[1].split("\r\n")){
                ckPair = ckPair.replace("\t", "");

                if(ckPair.length() != 0){
                    if(ckPair.contains("*")){
                        Double cf = Double.parseDouble(ckPair.split("\\*")[0].replace(" ", ""));
                        if(cf != 0){
                            coefficients.add(cf);
                            keys.add(ckPair.split("\\*")[1].replace(" ", ""));
                        }
                    }else{
                        Double cf = Double.parseDouble(ckPair.replace(" ", ""));
                        if(cf != 0d){
                            coefficients.add(cf);
                        }
                    }
                }

            }

            out.add(new LinearFunction(lmNum, coefficients, keys));
        }

        return out;
    }

    static RuleJunction parseRuleTree(String croppedInput, HashMap<String, LinearFunction> linearFunctionsByLm){
        String[] inputLines = croppedInput.split("\r\n");
        System.out.println(inputLines[0]);
        String[] splitHeader = inputLines[0].split(inputLines[0].indexOf(" <= ") != -1? " <= " : " >  ");
        boolean biggerThan = inputLines[0].indexOf(" <= ") == -1;
        String variableName = splitHeader[0];
        Double ruleCoeff = Double.parseDouble(splitHeader[1].split(" : ")[0]);


        Rule rule = new Rule(){
            @Override
            public boolean evaluate(Double value){
                return biggerThan? (value > ruleCoeff) : (value <= ruleCoeff);
            }

            @Override
            public String getVariableName(){
                return variableName;
            }
        };

        if(inputLines[0].indexOf("LM") != -1){
            if(inputLines[1].indexOf("LM") != -1){
                
                RuleJunction outRuleJunction =  new RuleJunction(rule, linearFunctionsByLm.get("LM" + inputLines[0].split("LM")[1].split(" ")[0]), linearFunctionsByLm.get("LM" + inputLines[1].split("LM")[1].split(" ")[0]));
                return outRuleJunction;
            }else{
                String ifFalse = "";
                int i = 2;
                for(; i < inputLines.length; i++){
                    ifFalse += inputLines[i].replaceAll("^\\|   ", "") + (inputLines.length-1 == i? "" : "\r\n");
                }
                
                RuleJunction outRuleJunction =  new RuleJunction(rule, linearFunctionsByLm.get("LM" + inputLines[0].split("LM")[1].split(" ")[0]), parseRuleTree(ifFalse, linearFunctionsByLm));
                return outRuleJunction;
            }
        }else{
            String ifTrue = "";
            int i = 1;
            for(; i < inputLines.length; i++){
                if(inputLines[i].indexOf(variableName + " >  " + inputLines[0].split(" <= ")[1]) == -1){
                    ifTrue += inputLines[i].replaceAll("^\\|   ", "") + (inputLines.length-1 == i? "" : "\r\n");
                }else{
                    break;
                }
            }
    
            String ifFalse = "";
            if(inputLines[i].indexOf("LM") != -1){
                
                RuleJunction outRuleJunction =  new RuleJunction(rule, parseRuleTree(ifTrue, linearFunctionsByLm), linearFunctionsByLm.get("LM" + inputLines[i].split("LM")[1].split(" ")[0]));
                System.out.println("zte");
                return outRuleJunction;
            }else{
                for(i++; i < inputLines.length; i++){
                    ifFalse += inputLines[i].replaceAll("^\\|   ", "") + (inputLines.length-1 == i? "" : "\r\n");
                }
            }
            
            RuleJunction outRuleJunction =  new RuleJunction(rule, parseRuleTree(ifTrue, linearFunctionsByLm), parseRuleTree(ifFalse, linearFunctionsByLm));
            return outRuleJunction;
        }

    }

    
    public static void checkArgs(String[] args) throws ProgramException{
        if(args.length == requiredArguments.length){
            return;
        }else if(args.length < requiredArguments.length){
            throw new ProgramException("The argument " + requiredArguments[args.length] + " is required");
        }
    }
}

class ProgramException extends Exception{
    public ProgramException(String errorMesage){
        super(errorMesage);
    }
}