import java.io.FileInputStream;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

class Parser{
    static String[] requiredArguments = new String[]{
        "input filename (String)"
    };

    public static void main(String[] args){
        try{
            checkArgs(args);
    
            File inputFile = new File("models/" + args[0]);
            FileInputStream fs = new FileInputStream(inputFile);
        
            String rawFile = "";
            
            while(fs.available() > 0){
                rawFile += (char)fs.read();
            }
            
            fs.close();
            
            ArrayList<LinearFunction> linearFunctions = parseLinearFunctions(rawFile);
            for(LinearFunction lf : linearFunctions){
                System.out.println(lf);
            }

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

class LinearFunction{
    public ArrayList<Double> coefficients;
    public ArrayList<String> keys;
    public String lmNumber;

    public LinearFunction(String lmNumber, ArrayList<Double> coefficients, ArrayList<String> keys){
        this.coefficients = coefficients;
        this.keys = keys;
        this.lmNumber = lmNumber;
    }

    public String toString(){
        String out = "Lm:" + this.lmNumber + "\n";

        for(int i = 0; i < this.coefficients.size(); i++){
            out += this.coefficients.get(i);
            if(i < this.keys.size()){
                out += " * " + this.keys.get(i);
            }
            out += "\n";
        }

        return out;
    }
}