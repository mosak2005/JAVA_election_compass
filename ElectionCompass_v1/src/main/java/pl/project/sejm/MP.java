package pl.project.sejm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class MP {
    public int id;
    public String firstName;
    public String lastName;
    public String club;
}
