package pl.project.sejm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class VoteDetail {
    @JsonProperty("MP")
    public int mpId;
    public String club;
    public String firstName;
    public String lastName;
    public String vote;
}
