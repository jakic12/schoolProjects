import java.util.ArrayList;

class LinearFunction{
    public ArrayList<Double> coefficients;
    public ArrayList<String> keys;
    public String lmNumber;

    public LinearFunction(String lmNumber, ArrayList<Double> coefficients, ArrayList<String> keys){
        this.coefficients = coefficients;
        this.keys = keys;
        this.lmNumber = lmNumber;
    }

    Double evaluateFunction(ArrayList<String> dataKeys, ArrayList<Double> values){
        Double out = 0d;
        int i = 0;
        for(String key : this.keys){
            out += values.get(i) * this.coefficients.get(dataKeys.indexOf(key));
            i++;
        }
        return out;
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