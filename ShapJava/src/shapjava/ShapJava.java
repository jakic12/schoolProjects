/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shapjava;

import java.io.*;
import java.util.Scanner;

/**
 *
 * @author e19
 */
public class ShapJava {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        String filename = "tempOut";
        Process p = Runtime.getRuntime().exec("python main.py " + filename);
        p.waitFor();

        double[][] data = parseCsv(readFile(filename));

        for(double[] r : data){
            for(double c : r){
                System.out.print(c + ", ");
            }
            System.out.println();
        }
    }

    static String readFile(String path) throws IOException{
        String rawFile = "";
        Scanner sc = new Scanner(new File(path));

        while(sc.hasNextLine())
            rawFile += sc.nextLine() + "\r\n";
        
        sc.close();
        return rawFile;
    }
    
    static double[][] parseCsv(String input){
        String[] rows = input.split("\r\n");
        String[] temp;

        double[][] out = new double[rows.length][];

        for(int i = 0; i < rows.length; i++){
            temp = rows[i].split(",");
            out[i] = new double[temp.length];
            for(int j = 0; j < temp.length; j++){
                out[i][j] = Double.parseDouble(temp[j]);
            }
        }
        return out;
    }
    
}
