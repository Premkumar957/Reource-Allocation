package customer.resource_allocation.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

public class PdfFooter extends PdfPageEventHelper {


    Font font =
            FontFactory.getFont(
                    FontFactory.HELVETICA,
                    9);

    @Override
    public void onEndPage(
            PdfWriter writer,
            Document document) {

        PdfContentByte cb =
                writer.getDirectContent();

        Phrase footer =
                new Phrase(
                        "Page "
                                + writer.getPageNumber(),
                        font);

        ColumnText.showTextAligned(
                cb,
                Element.ALIGN_CENTER,
                footer,
                (document.right()
                        + document.left()) / 2,
                document.bottom() - 15,
                0);

    }

    
}
