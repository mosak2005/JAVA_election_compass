package pl.project.sejm;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Print {
    public String number;
    public String title;
}