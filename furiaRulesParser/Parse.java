import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * cli params  
 * <ruleFile> {String} file in the datasets folder
 */
class Parse {
    static String[] requiredArguments = new String[]{
        "input filename",
        "class",
        "cf"
    };
    public static void main(String[] args){
        args = new String[]{"anneal", "3", "0.7"};
        try{
            checkArgs(args);
            File inputFile = new File("datasets/" + args[0]);
            Scanner sc = new Scanner(inputFile);
    
            ArrayList<String> rules = new ArrayList<String>();
    
            while(sc.hasNextLine()){
                String line;
                if((line = sc.nextLine()).length() > 0){
                    rules.add(line);
                }
            }
            sc.close();
    
            ArrayList<String> newRules = parsingRules(rules, args[1], Double.parseDouble(args[2]));
            for(String rule : newRules){
                System.out.println(rule);
            }

        }catch(FileNotFoundException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }catch(ProgramException e){
            System.out.println(e.getMessage());
        }
    }

    public static ArrayList<String> parsingRules(ArrayList<String> rules, String c, double CF){
        ArrayList<String> outRules = new ArrayList();
        boolean filterByClass = true;
        boolean filterByCf = true;
        
        ArrayList<String> rulesToBeConcatinated = new ArrayList<>();

        for(String rule : rules){
            String hyperParams = rule.split("=> ")[1];

            String ruleClass = hyperParams.split("=")[1].split(" \\(")[0];
            double ruleCf = Double.parseDouble(hyperParams.split("CF = ")[1].split("\\)")[0]);
            if((!filterByClass || ruleClass.equals(c)) && (!filterByCf || ruleCf >= CF)){
                if(rule.matches(".* and .*")){
                    String convertedRules = "";
                    rule = rule.substring(0, rule.indexOf(" =>"));
                    String[] splitRule = rule.split(" and ");

                    for(int i = 0; i < splitRule.length; i++){
                        convertedRules += (i==0? "" : " and ") + convertSingleRule(splitRule[i]);
                    }

                    outRules.add(convertedRules);
                }else{
                    rulesToBeConcatinated.add(convertSingleRule(rule.substring(0, rule.indexOf(" =>"))));
                }
            }
        }

        String concatOr = "";
        if(rulesToBeConcatinated.size() > 0){
            for(int i = 0; i < rulesToBeConcatinated.size(); i++){
                concatOr += (i==0? "" : " or ") + rulesToBeConcatinated.get(i);
            }
            outRules.add(concatOr);
        }

        return outRules;
    }

    public static String convertSingleRule(String input){
        if(input.matches(".* = .*"))
            return input;

        String name = input.substring(1, input.indexOf(" "));
        String[] set = input.substring(input.indexOf("[")+1, input.indexOf("]")).split(", ");

        if(input.indexOf("-inf") != -1){
            return "(" + name + " <= " + set[2]  + ")";
        }else{
            return "(" + name + " >= " + set[1]  + ")";
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