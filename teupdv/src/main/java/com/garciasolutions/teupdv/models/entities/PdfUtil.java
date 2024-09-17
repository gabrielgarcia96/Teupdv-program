package com.garciasolutions.teupdv.models.entities;

import javafx.embed.swing.SwingFXUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.*;

public class PdfUtil {

    public static Image convertPdfPageToImage(InputStream pdfStream, int pageIndex, int resolution) throws IOException {
        PDDocument document = PDDocument.load(pdfStream);
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(pageIndex, resolution);
        document.close();
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

}
