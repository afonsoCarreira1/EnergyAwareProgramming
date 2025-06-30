import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class T {
    static String frequency = ".1";

    public static void main(String[] args) {
        String s = readPowerjoularCsv(1, "p.csv");
        System.out.println("s = "+s);
    }


    private static String readPowerjoularCsv(int iter, String csvFile) {
        try {Thread.sleep(100);
        } catch (InterruptedException e) {e.printStackTrace();}
        List<String> cpuPowerValues = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            String[] headers = br.readLine().split(",");
            int cpuPowerColumnIndex = -1;

            for (int i = 0; i < headers.length; i++) {
                if ("CPU Power".equalsIgnoreCase(headers[i])) {
                    cpuPowerColumnIndex = i;
                    break;
                }
            }
            boolean firstLineSkipped = false;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                
                String powerValue = values[cpuPowerColumnIndex].trim();

                // Skip the first line if it's invalid
                if (!firstLineSkipped && !isNumeric(powerValue)) {
                    //log.append("Skipped invalid first line: " + powerValue + "\n");
                    firstLineSkipped = true;
                    continue;
                }
                cpuPowerValues.add(values[cpuPowerColumnIndex]);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("Program ran so fast it did not create a CSV file or other error.\n");
        }
        Double cpuPower = 0.0;
        //TODO fix this so when it catches "+Inf***********" ignores it
        try {
            
            Double freq = Double.parseDouble(frequency);
            for (int i = 0; i < cpuPowerValues.size(); i++) {
                cpuPower += Double.parseDouble(cpuPowerValues.get(i)) * freq;
            }
            // return Double.toString(Math.round(cpuPower));
            cpuPower /= iter;
            
        } catch (Exception e) {
            cpuPower = 0.0;
            System.out.println("Error with powerjoular csv.\n");
        }
        return "" + cpuPower;// String.format("%.5f", cpuPower);
    }

    private static boolean isNumeric(String str) {
    try {
        Double.parseDouble(str);
        return true;
    } catch (NumberFormatException e) {
        return false;
    }
}
    
}
