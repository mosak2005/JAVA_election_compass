package pl.project.sejm;
import java.util.*;
import java.util.regex.*;

public class SejmUtils {
    public static List<String> extractDruki(String text) {
        List<String> results = new ArrayList<>();
        if (text == null) return results;
        Pattern p = Pattern.compile("druk[i]?\\s*nr\\s*([\\d\\s,iA-Z-]+)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);

        if (m.find()) {
            Matcher nm = Pattern.compile("\\d+").matcher(m.group(1));
            while (nm.find()) {
                results.add(nm.group());
            }
        }
        return results;
    }
}