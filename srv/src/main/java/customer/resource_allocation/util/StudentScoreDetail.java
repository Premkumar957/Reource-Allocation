package customer.resource_allocation.util;

public class StudentScoreDetail {
    
    private String subjectName;
    private Double totalMarks;
    private Double marksObtained;

    public StudentScoreDetail() {
        super();
    }

    public StudentScoreDetail(String subjectName, Double totalMarks, Double marksObtained) {
        super();
        this.subjectName = subjectName;
        this.totalMarks = totalMarks;
        this.marksObtained = marksObtained;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Double getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Double totalMarks) {
        this.totalMarks = totalMarks;
    }

    public Double getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(Double marksObtained) {
        this.marksObtained = marksObtained;
    }
}
