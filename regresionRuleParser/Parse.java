import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * cli params  
 * <ruleFile> {String} file in the datasets folder
 */
class Parse {
    static String[] requiredArguments = new String[]{
        "input filename (String)",
        "remove duplicates (Boolean)"
    };
    public static void main(String[] args){
        args = new String[]{"rConceptsCond", "true"};
        try{
            checkArgs(args);
            File inputFile = new File("datasets/" + args[0]);
            FileInputStream sc = new FileInputStream(inputFile);
            String rawFile = "";
    
            TreeMap<String, Double> rules;
    
            while(sc.available() > 0){
                String line;
                rawFile += (char)sc.read();
            }
            rawFile = rawFile.replace("\r\n", "\n");
            rawFile = rawFile.substring(rawFile.lastIndexOf("\n\n")+2);
            System.out.println(rawFile);

            rules = parseCoefficients(rawFile.split("\n"));
            for(Map.Entry<String,Double> rule : rules.entrySet()){
                System.out.println(rule.getKey() + " : " + rule.getValue());
            }

            sc.close();

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

    public static TreeMap<String, Double> parseCoefficients(String[] rules){
        TreeMap<String, Double> outRules = new TreeMap<String, Double>();
        for(String rule : rules){
            if(rule.indexOf(" * ") != -1){
                String[] splitRule = rule.split(" \\* ");
                outRules.put(splitRule[1].split(" +")[0], Double.parseDouble(splitRule[0].replaceAll(" +", "")));
            }else{
                String name = "C";
                int number = 0;
                boolean nameInside = true;

                while(nameInside){
                    if(outRules.get(name + number) != null){
                        number++;
                    }else{
                        nameInside = false;
                    }
                }
                outRules.put(name+number, Double.parseDouble(rule.replaceAll(" +", "")));
            }
        }
        return outRules;
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