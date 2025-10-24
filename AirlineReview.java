import java.util.Arrays;

public class AirlineReview {
    private String airline;
    private String[] tokenizedReview;
    private String overallRating;
    private String recommended;
    public String getAirline() {
        return airline;
    }
    public void setAirline(String airline) {
        this.airline = airline;
    }
    public String[] getTokenizedReview() {
        return tokenizedReview;
    }
    public void setTokenizedReview(String[] tokenizedReview) {
        this.tokenizedReview = tokenizedReview;
    }
    public String getOverallRating() {
        return overallRating;
    }
    public void setOverallRating(String overallRating) {
        this.overallRating = overallRating;
    }
    public String getRecommended() {
        return recommended;
    }
    public void setRecommended(String recommended) {
        this.recommended = recommended;
    }
    public AirlineReview(String airline, String[] tokenizedReview, String overallRating, String recommended) {
        this.airline = airline;
        this.tokenizedReview = tokenizedReview;
        this.overallRating = overallRating;
        this.recommended = recommended;
    }
    public AirlineReview() {}
    @Override
    public String toString() {
        return "AirlineReview [airline=" + airline + ", tokenizedReview=" + Arrays.toString(tokenizedReview)
                + ", overallRating=" + overallRating + ", recommended=" + recommended + "]";
    }
    
}