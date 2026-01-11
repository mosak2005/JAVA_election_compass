package pl.project.sejm;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Voting {
    public int votingNumber;
    public int sitting;
    public String title;
    public String topic;
    public List<VoteDetail> votes;
}




