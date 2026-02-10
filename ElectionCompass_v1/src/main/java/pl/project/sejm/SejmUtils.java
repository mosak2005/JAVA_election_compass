package pl.project.sejm;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SejmUtils {

    private static final Pattern DRUKI_PATTERN =
            Pattern.compile("druk[i]?\\s*nr\\s*([\\d\\s,iA-Z-]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    private SejmUtils() {
    }

    public static List<String> extractDruki(String text) {
        List<String> results = new ArrayList<>();
        if (text == null) {
            return results;
        }

        Matcher m = DRUKI_PATTERN.matcher(text);
        if (m.find()) {
            Matcher nm = NUMBER_PATTERN.matcher(m.group(1));
            while (nm.find()) {
                results.add(nm.group());
            }
        }
        return results;
    }
}
