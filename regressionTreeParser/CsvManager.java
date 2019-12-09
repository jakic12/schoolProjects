import java.util.Scanner;
import java.util.LinkedHashMap;
import java.io.File;
import java.io.FileNotFoundException;
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

}