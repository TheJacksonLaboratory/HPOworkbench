package org.monarchinitiative.hpoworkbench.cmd;

import picocli.CommandLine;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
@CommandLine.Command(name = "count",
        mixinStandardHelpOptions = true,
        description = "Count and compare gene to disease associations.")
public class EncodingCheckCommand  extends HPOCommand implements Callable<Integer> {
    @CommandLine.Option(names={"--hp"}, required = true)
    String pathToHpOwlEdit;
    public EncodingCheckCommand(){}

    @Override
    public Integer call() {
        String pattern = "(HP_\\d+)";
        Pattern r = Pattern.compile(pattern);
        File file = new File(pathToHpOwlEdit);
        if (! file.isFile()) {
            System.err.printf("Error - pass the path to hp-edit.owl (could not find it at %s).\n",
                    pathToHpOwlEdit);
            return 1;
        }
        int i = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))){
            String line;
            String hp_term = "";
            while ((line = br.readLine()) != null) {
                Matcher m = r.matcher(line);
                if (m.find()) {
                    hp_term = m.group();
                }
                checkLine(line, ++i, hp_term);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void checkLine(String line, int lineno, String previous) throws UnsupportedEncodingException {
        byte[] bytes = line.getBytes(StandardCharsets.ISO_8859_1);
        String decodedLine = new String(bytes);
        if (line.equals(decodedLine)) return;
        else {
            for (int i=0; i< line.length();i++) {
                if (line.charAt(i) != decodedLine.charAt(i)) {
                    System.out.println(previous);
                    System.out.printf("L.%d:Pos:%d: ", lineno, i);
                    int b = Math.max(0, i-20);
                    int e = Math.min(line.length(), i+20);
                    String ss1 = line.substring(b, i);
                    String ss2 = line.substring(i+1, e);
                    System.out.printf("%s{%c}%sc\n\n", ss1, line.charAt(i), ss2);
                }
            }
        }
    }
}
