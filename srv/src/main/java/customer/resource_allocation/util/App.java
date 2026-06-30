package customer.resource_allocation.util;

import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperExportManager;

public class App {
    
    public static void main( String[] args ) {

        String jasperFilePath = "C:\\Projects\\jasperDesigns\\demo1.jasper";
        
        try {
            // STEP 1 Obtain JasperReport Class Object
            JasperReport jasperReport = (JasperReport) JRLoader.loadObjectFromFile(jasperFilePath);

            // STEP 2 Create JRDatasource Class Object
            List<StudentDetails> studentDetailList = new ArrayList<StudentDetails>();
            studentDetailList.add(new StudentDetails("ABCD", "2023", "EFGH", "2022-2023"));

            JRBeanCollectionDataSource datasource = new JRBeanCollectionDataSource(studentDetailList);

            List<StudentScoreDetail> studentScoreCardDetailList = new ArrayList<StudentScoreDetail>();
            studentScoreCardDetailList.add(new StudentScoreDetail("SUBJECT 1", 100.0, 80.0));
            studentScoreCardDetailList.add(new StudentScoreDetail("SUBJECT 2", 100.0, 60.0));
            studentScoreCardDetailList.add(new StudentScoreDetail("SUBJECT 3", 100.0, 75.0));
            studentScoreCardDetailList.add(new StudentScoreDetail("SUBJECT 4", 100.0, 90.0));
            studentScoreCardDetailList.add(new StudentScoreDetail("SUBJECT 5", 100.0, 85.0));



            JRBeanCollectionDataSource tableDatasource = new JRBeanCollectionDataSource(studentScoreCardDetailList);

            // Step 3 Create a HashMap Object to store parameters
            Map<String, Object> parameters = new HashMap<String, Object>();

            parameters.put("TABLE_DATA_SOURCE", tableDatasource); 

            // Step 4 Create JasperPrint Object
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, datasource);

            // Step 5 Export our Jasper Print Object to desired report format
            JasperExportManager.exportReportToPdfFile(jasperPrint, "C:\\Projects\\generatedReports\\StudentScoreCard.pdf");

            System.out.println("Report Generated Successfully");

        } catch (JRException e) {
            e.printStackTrace();
        }
        
    } 
}
