package pl.project.sejm;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
class Voting {
    public int votingNumber;
    public int sitting;
    public String title;
    public String topic;
    public List<VoteDetail> votes;
}

@JsonIgnoreProperties(ignoreUnknown = true)
class VoteDetail {
    public int MP;
    public String club;
    public String firstName;
    public String lastName;
    public String vote;
}

@JsonIgnoreProperties(ignoreUnknown = true)
class MP {
    public int id;
    public String firstName;
    public String lastName;
    public String club;
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Print {
    public String number;
    public String title;
}