package moss.mystery.energymonitor.classifier;

public class ClassifiedApp {
    public final String name;
    public final int confidence;
    public final int classification;
    public final boolean unknownApp;
    public final boolean network;       //True if app is high when network is high

    public ClassifiedApp(String name, int classification, int confidence, boolean unknownApp, boolean network){
        this.name = name;
        this.confidence = confidence;
        this.classification = classification;
        this.unknownApp = unknownApp;
        this.network = network;
    }
}
