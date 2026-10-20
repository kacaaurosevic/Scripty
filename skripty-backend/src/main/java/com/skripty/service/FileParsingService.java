package com.skripty.service;

import com.skripty.exception.ApiException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Extracts plain text from an uploaded file so it can be fed to the AI generation step.
 * The original file itself is never persisted — only the extracted text — which keeps
 * storage simple and avoids having to manage a file store as the app scales.
 */
@Service
public class FileParsingService {

    private static final long MAX_CHARS = 60_000; // keeps a single Gemini request reasonably sized

    public String extractText(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Uploaded file has no name.");
        }
        String lower = filename.toLowerCase(Locale.ROOT);

        try (InputStream in = file.getInputStream()) {
            String text;
            if (lower.endsWith(".pdf")) {
                text = extractFromPdf(in);
            } else if (lower.endsWith(".docx")) {
                text = extractFromDocx(in);
            } else if (lower.endsWith(".txt")) {
                text = new String(in.readAllBytes());
            } else {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Unsupported file type. Please upload a .pdf, .docx or .txt file.");
            }

            return truncate(text);

        } catch (IOException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Could not read the uploaded file. Is it corrupted?");
        }
    }

    public String cleanPastedText(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Pasted text is empty.");
        }
        return truncate(rawText.trim());
    }

    private String extractFromPdf(InputStream in) throws IOException {
        try (PDDocument document = Loader.loadPDF(in.readAllBytes())) {
            if (document.isEncrypted()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "This PDF is password-protected.");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractFromDocx(InputStream in) throws IOException {
        try (XWPFDocument document = new XWPFDocument(in);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String truncate(String text) {
        if (text.length() <= MAX_CHARS) return text;
        return text.substring(0, (int) MAX_CHARS);
    }
}
