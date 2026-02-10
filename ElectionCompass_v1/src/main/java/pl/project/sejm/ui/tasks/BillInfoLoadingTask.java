package pl.project.sejm.ui.tasks;

import java.util.ArrayList;
import java.util.List;

import javafx.concurrent.Task;

import pl.project.sejm.Print;
import pl.project.sejm.SejmApiClient;
import pl.project.sejm.SejmUtils;
import pl.project.sejm.Voting;
import pl.project.sejm.ui.model.BillInfo;

public class BillInfoLoadingTask extends Task<BillInfo> {
    private final SejmApiClient api;
    private final Voting current;

    public BillInfoLoadingTask(SejmApiClient api, Voting current) {
        this.api = api;
        this.current = current;
    }

    @Override
    protected BillInfo call() throws Exception {
        updateMessage("Status: pobieram opis głosowania");
        updateProgress(-1, 1);
        
        Voting details = api.getVotingDetails(current.sitting, current.votingNumber);
        String vTitle = details != null && details.title != null ? details.title : current.title;
        String topic = details != null ? details.topic : null;
        
        if (topic == null || topic.isBlank()) {
            topic = "Brak pola topic w API dla tego głosowania";
        }
        
        List<String> druki = SejmUtils.extractDruki(vTitle);
        List<Print> prints = new ArrayList<>();
        
        if (!druki.isEmpty()) {
            updateMessage("Status: pobieram tytuły druków");
            updateProgress(0, druki.size());
            
            for (int i = 0; i < druki.size(); i++) {
                if (isCancelled()) {
                    return null;
                }
                
                String nr = druki.get(i);
                try {
                    Print p = api.getPrintDetails(nr);
                    if (p != null) {
                        prints.add(p);
                    }
                } catch (Exception ignored) {
                }
                updateProgress(i + 1, druki.size());
            }
        }
        
        return new BillInfo(vTitle, topic, druki, prints);
    }
}
