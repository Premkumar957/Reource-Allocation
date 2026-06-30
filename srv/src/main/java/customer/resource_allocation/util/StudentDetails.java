package customer.resource_allocation.util;

public class StudentDetails {
    
    private String name;
    private String year;
    private String advisor;
    private String gradingPeriod;

    public StudentDetails() {
        super();
    }
    public StudentDetails(String name, String year, String advisor, String gradingPeriod) {
        super();
        this.name = name;
        this.year = year;
        this.advisor = advisor;
        this.gradingPeriod = gradingPeriod;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getAdvisor() {
        return advisor;
    }

    public void setAdvisor(String advisor) {
        this.advisor = advisor;
    }

    public String getGradingPeriod() {
        return gradingPeriod;
    }

    public void setGradingPeriod(String gradingPeriod) {
        this.gradingPeriod = gradingPeriod;
    }

}
