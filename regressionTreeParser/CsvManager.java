import java.util.Scanner;
import java.util.LinkedHashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

class CsvManager{
    public static LinkedHashMap<String, ArrayList<Double>> openCsv(File file) throws FileNotFoundException{
        LinkedHashMap<String, ArrayList<Double>> out = new LinkedHashMap<>();
        Scanner scan = new Scanner(file);
        String[] columns = new String[0];

        int i = 0;
        while(scan.hasNextLine()){
            String[] line = scan.nextLine().split(",");
            if(i == 0){
                columns = line;
                for(int j = 0; j < line.length; j++){
                    out.put(columns[j], new ArrayList<>());
                }
            }else{
                for(int j = 0; j < line.length; j++){
                    out.get(columns[j]).add(Double.parseDouble(line[j]));
                }
            }
            i++;
        }
        return out;
    }

    public static void writeToFile(File file, LinkedHashMap<String, ArrayList<Double>> data) throws IOException{
        ArrayList<ArrayList<Double>> values = new ArrayList<>();
        
        for(int i = 0; i < data.get(data.keySet().iterator().next()).size(); i++){
            values.add(new ArrayList<>());
            for(String key : data.keySet()){
                values.get(i).add(data.get(key).get(i));
            }
        }

        PrintWriter pw = new PrintWriter(new FileWriter(file));
        int j = 0;
        for(String key : data.keySet()){
            pw.print(key + ((j == data.keySet().size()-1)?"":","));
            j++;
        }
        pw.println();

        for(ArrayList<Double> row : values){
            j = 0;
            for(Double value : row){
                pw.print(value + ((j == row.size()-1)?"":","));
                j++;
            }
            pw.println();
        }
    }

}