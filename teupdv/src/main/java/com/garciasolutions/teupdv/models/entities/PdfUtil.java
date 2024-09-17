package com.garciasolutions.teupdv.models.entities;

import javafx.embed.swing.SwingFXUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import javafx.scene.image.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class PdfUtil {

    public static Image convertPdfPageToImage(InputStream pdfStream, int pageIndex, float resolution) throws IOException {
        // Usando try-with-resources para garantir que o PDDocument seja fechado corretamente
        try (PDDocument document = PDDocument.load(pdfStream)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(pageIndex, resolution);
            return SwingFXUtils.toFXImage(bufferedImage, null);
        }
    }
}
