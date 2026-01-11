package pl.project.sejm;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VoteDetail {
    public int MP;
    public String club;
    public String firstName;
    public String lastName;
    public String vote;
}