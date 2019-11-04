import java.util.ArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// https://www.logicbig.com/how-to/code-snippets/jcode-java-cmd-command-line-table.html
class CommandLineTable {
    private static final String HORIZONTAL_SEP = "-";
    private String verticalSep;
    private String joinSep;
    private String[] headers;
    private List<String[]> rows = new ArrayList<>();
    private boolean rightAlign;

    public CommandLineTable() {
        setShowVerticalLines(false);
    }

    public void setRightAlign(boolean rightAlign) {
        this.rightAlign = rightAlign;
    }

    public void setShowVerticalLines(boolean showVerticalLines) {
        verticalSep = showVerticalLines ? "|" : "";
        joinSep = showVerticalLines ? "+" : " ";
    }

    public void setHeaders(String... headers) {
        this.headers = headers;
    }

    public void addRow(String... cells) {
        rows.add(cells);
    }

    public void print() {
        int[] maxWidths = headers != null ?
                Arrays.stream(headers).mapToInt(String::length).toArray() : null;

        for (String[] cells : rows) {
            if (maxWidths == null) {
                maxWidths = new int[cells.length];
            }
            if (cells.length != maxWidths.length) {
                throw new IllegalArgumentException("Number of row-cells and headers should be consistent");
            }
            for (int i = 0; i < cells.length; i++) {
                maxWidths[i] = Math.max(maxWidths[i], cells[i].length());
            }
        }

        if (headers != null) {
            printLine(maxWidths);
            printRow(headers, maxWidths);
            printLine(maxWidths);
        }
        for (String[] cells : rows) {
            printRow(cells, maxWidths);
        }
        if (headers != null) {
            printLine(maxWidths);
        }
    }

    private void printLine(int[] columnWidths) {
        for (int i = 0; i < columnWidths.length; i++) {
            String line = String.join("", Collections.nCopies(columnWidths[i] +
                    verticalSep.length() + 1, HORIZONTAL_SEP));
            System.out.print(joinSep + line + (i == columnWidths.length - 1 ? joinSep : ""));
        }
        System.out.println();
    }

    private void printRow(String[] cells, int[] maxWidths) {
        for (int i = 0; i < cells.length; i++) {
            String s = cells[i];
            String verStrTemp = i == cells.length - 1 ? verticalSep : "";
            if (rightAlign) {
                System.out.printf("%s %" + maxWidths[i] + "s %s", verticalSep, s, verStrTemp);
            } else {
                System.out.printf("%s %-" + maxWidths[i] + "s %s", verticalSep, s, verStrTemp);
            }
        }
        System.out.println();
    }
}
 
public class PercolationVisualizer {
    public static String millisToString(long diff) {
        long millis = diff%1000;
        long secs = (diff/1000)%60;
        long mins = (diff/(1000*60))%60;
        long hs = (diff/(1000*3600))%24;
        long days = diff/(1000*3600*24);
    
        if (days > 0) 
          return days+"d "+hs+"h "+mins+"m "+secs+"s "+millis+"ms";
    
        if (hs > 0)
          return hs+"h "+mins+"m "+secs+"s "+millis+"ms";
    
        if (mins > 0)
          return mins+"m "+secs+"s "+millis+"ms";
    
        if (secs > 0)
          return secs+"s "+millis+"ms";
    
        return millis+"ms";
      }
    public static void main(String[] args) {
        int n      = Integer.parseInt(args[0]);
        double p   = Double.parseDouble(args[1]);
        int trials = Integer.parseInt(args[2]);
        boolean time = args.length > 3 && Boolean.parseBoolean(args[3]);
        

        // repeatedly created n-by-n matrices and display them using standard draw
        if(!time){
            StdDraw.enableDoubleBuffering();
            for (int t = 0; t < trials; t++) {
                boolean[][] open = Percolation.random(n, p);
                StdDraw.clear();
                StdDraw.setPenColor(StdDraw.BLACK);
                Percolation.show(open, false);
                StdDraw.setPenColor(StdDraw.BOOK_LIGHT_BLUE);
                boolean[][] full = flowNonRec(open);
                Percolation.show(full, true);
                StdDraw.show();
                StdDraw.pause(2000);
            }
        }else{
            CommandLineTable outTable = new CommandLineTable();
            long sumTime = 0;
            long rekSumTime = 0;

            outTable.setHeaders("recursive", "iterative", "diff");

            for (int t = 0; t < trials; t++) {
                boolean[][] open = Percolation.random(n, p);

                long startTime = System.nanoTime();
                Percolation.flow(open);
                long endTime = System.nanoTime();
                long duration = (endTime - startTime)/1000000;

                long nonRekStartTime = System.nanoTime();
                flowNonRec(open);
                long nonRekEndTime = System.nanoTime();
                long nonRekDuration = (nonRekEndTime - nonRekStartTime)/1000000;
                
                outTable.addRow(millisToString(duration), millisToString(nonRekDuration), millisToString(nonRekDuration - duration));

                rekSumTime += duration;
                sumTime += nonRekDuration;
            }
            outTable.setShowVerticalLines(true);
            outTable.print();

            CommandLineTable outSumTable = new CommandLineTable();

            outSumTable.setHeaders("recursive sum", "iterative sum", "diff");
            outSumTable.addRow(millisToString(rekSumTime), millisToString(sumTime), millisToString(sumTime - rekSumTime));
            outSumTable.setShowVerticalLines(true);
            outSumTable.print();

            CommandLineTable outAvgTable = new CommandLineTable();
            outAvgTable.setHeaders("recursive avg", "iterative avg", "diff");
            outAvgTable.addRow(millisToString(rekSumTime/trials), millisToString(sumTime/trials), millisToString((sumTime/trials)-(rekSumTime/trials)));
            outAvgTable.setShowVerticalLines(true);
            outAvgTable.print();
        }
    }

    public static boolean[][] flowNonRec(boolean[][] field){
        boolean[][] out = new boolean[field.length][field[0].length];
        ArrayList<ArrayList<RowSection>> sections = new ArrayList<ArrayList<RowSection>>();

        boolean sameSection = false;

        for(int i = 0; i < field.length; i++){
            sections.add(new ArrayList<RowSection>());
            ArrayList<RowSection> row = sections.get(i);

            for(int j = 0; j < field[i].length; j++){
                if(!sameSection && field[i][j]){
                    row.add(new RowSection(j, j, i));
                    sameSection = true;
                }else if(field[i][j]){
                    row.get(row.size()-1).end = j;
                }else{
                    sameSection = false;
                }
                if(field[i].length-1 == j){
                    sameSection = false;
                }
            }

            for(int c = 0; c < row.size(); c++){
                RowSection section = row.get(c);
                if(i-1 < 0){
                    out = section.fill(out);
                }else for(int c1 = 0; c1 < sections.get(i-1).size(); c1++){
                    RowSection prevRowSection = sections.get(i-1).get(c1);
                    if((prevRowSection.end >= section.end && prevRowSection.start <= section.end) || 
                    (prevRowSection.end >= section.start && prevRowSection.start <= section.start) ||
                    (section.end >= prevRowSection.end && section.start <= prevRowSection.end) || 
                    (section.end >= prevRowSection.start && section.start <= prevRowSection.start)
                    ){
                        prevRowSection.children.add(section);
                        section.upper.add(prevRowSection);
                        if(prevRowSection.colored){
                            out = section.fill(out);
                        }
                    }
                }
            }
        }
        boolean repeat;
        do{
            repeat = false;
            for(int i = sections.size()-1; i >= 0; i--){
                for(RowSection section : sections.get(i)){
                    if(section.upper != null){
                        for(RowSection upp : section.upper){
                            if(section.colored && upp != null && !upp.colored){
                                out = upp.fill(out);
                                for(RowSection uppParent : upp.upper){
                                    if(!uppParent.colored)
                                        repeat = true;
                                }

                                for(RowSection child : upp.children){
                                    if(!child.colored)
                                        repeat = true;
                                }
                            }
                        }
                    }

                    if(section.children != null){
                        for(RowSection child : section.children){
                            if(section.colored && child != null && !child.colored){
                                out = child.fill(out);

                                for(RowSection uppParent : child.upper){
                                    if(!uppParent.colored)
                                        repeat = true;
                                }

                                for(RowSection uppChild : child.children){
                                    if(!uppChild.colored)
                                        repeat = true;
                                }
                            }
                        }
                    }
                }
            }
        }while(repeat);

        return out;
    }
}

class RowSection{
    public int start;
    public int end;
    public int row;
    public boolean colored = false;

    public ArrayList<RowSection> upper;
    public ArrayList<RowSection> children;

    public RowSection(int start, int end, int row){
        this.start = start;
        this.end = end;
        this.row = row;
        this.children = new ArrayList<RowSection>();
        this.upper = new ArrayList<RowSection>();
    }

    public boolean[][] fill(boolean[][] field){
        this.colored = true;
        for(int i = start; i <= end; i++){
            field[row][i] = true;
        }
        return field;
    }
}
