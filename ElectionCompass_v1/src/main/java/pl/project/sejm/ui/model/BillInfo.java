package pl.project.sejm.ui.model;

import pl.project.sejm.Print;

import java.util.List;

public final class BillInfo {

    private final String title;
    private final String topic;
    private final List<String> druki;
    private final List<Print> prints;

    public BillInfo(String title, String topic, List<String> druki, List<Print> prints) {
        this.title = title;
        this.topic = topic;
        this.druki = druki == null ? List.of() : List.copyOf(druki);
        this.prints = prints == null ? List.of() : List.copyOf(prints);
    }

    public String getTitle() { return title; }
    public String getTopic() { return topic; }
    public List<String> getDruki() { return druki; }
    public List<Print> getPrints() { return prints; }
}
