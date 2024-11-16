
package game;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.io.IOException;

public class PDFReader {
    private String pdfContent;

    public String readPDF(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            pdfContent = stripper.getText(document);
            return pdfContent;
        }
    }

    public String getContent() {
        return pdfContent;
    }
}