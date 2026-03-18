package com.example.HAllTicket.service;

import com.example.HAllTicket.dto.SubjectDTO;
import com.example.HAllTicket.model.ExamModel;
import com.example.HAllTicket.model.HallTicketModel;
import com.example.HAllTicket.model.StudentModel;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class PdfGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(PdfGeneratorService.class);

    /**
     * Robust image loader — checks persistent uploads/ dir first, then classpath fallbacks.
     * Strategy 0: uploads/{path} — persistent, survives Maven rebuilds (primary)
     * Strategy 1: ClassPathResource.getFile() — works in IDE
     * Strategy 2: user.dir + src/main/resources/ — works in Maven run
     * Strategy 3: ClassPathResource.getInputStream() as bytes — works everywhere
     */
    private Image loadImage(String classpathRelativePath) {
        // Strategy 0: Persistent uploads/ directory (primary storage after upload)
        try {
            String uploadsDir = com.example.HAllTicket.config.WebConfig.getUploadsDir();
            // classpathRelativePath is like "static/img/foo.jpg" or "static/qr/bar.jpg"
            // Convert to "img/foo.jpg" or "qr/bar.jpg" for uploads dir
            String relativePart = classpathRelativePath.replace("static/", "");
            File f = new File(uploadsDir + relativePart);
            if (f.exists()) {
                logger.debug("Image loaded from uploads dir: {}", f.getAbsolutePath());
                return Image.getInstance(f.getAbsolutePath());
            }
        } catch (Exception e) {
            logger.debug("Strategy 0 (uploads dir) failed for {}: {}", classpathRelativePath, e.getMessage());
        }

        // Strategy 1: ClassPathResource.getFile()
        try {
            File f = new ClassPathResource(classpathRelativePath).getFile();
            if (f.exists()) {
                logger.debug("Image loaded via ClassPathResource.getFile(): {}", f.getAbsolutePath());
                return Image.getInstance(f.getAbsolutePath());
            }
        } catch (Exception e) {
            logger.debug("Strategy 1 failed for {}: {}", classpathRelativePath, e.getMessage());
        }

        // Strategy 2: Direct filesystem path (src/main/resources/...)
        try {
            String projectDir = System.getProperty("user.dir");
            File f = new File(projectDir + File.separator + "src" + File.separator + "main"
                    + File.separator + "resources" + File.separator
                    + classpathRelativePath.replace("/", File.separator));
            if (f.exists()) {
                logger.debug("Image loaded via filesystem path: {}", f.getAbsolutePath());
                return Image.getInstance(f.getAbsolutePath());
            }
        } catch (Exception e) {
            logger.debug("Strategy 2 failed for {}: {}", classpathRelativePath, e.getMessage());
        }

        // Strategy 3: InputStream as byte array (works in JAR and all environments)
        try (InputStream is = new ClassPathResource(classpathRelativePath).getInputStream()) {
            byte[] bytes = is.readAllBytes();
            logger.debug("Image loaded via InputStream for: {}", classpathRelativePath);
            return Image.getInstance(bytes);
        } catch (Exception e) {
            logger.warn("All strategies failed to load image: {}", classpathRelativePath);
        }

        return null;
    }

    // --- Design System ---
    private static final Color COLOR_PRIMARY = new Color(79, 70, 229); // Indigo 600
    private static final Color COLOR_HEADER_BG = new Color(30, 41, 59); // Slate 800
    private static final Color COLOR_SUCCESS = new Color(34, 197, 94);  // Green 500
    private static final Color COLOR_TEXT_LIGHT = new Color(248, 250, 252); // Slate 50
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139); // Slate 500
    private static final Color COLOR_BORDER = new Color(226, 232, 240); // Slate 200

    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.NORMAL, Color.WHITE);
    private static final Font FONT_SUBTITLE = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, COLOR_TEXT_LIGHT);
    private static final Font FONT_LABEL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Font.NORMAL, COLOR_TEXT_MUTED);
    private static final Font FONT_VALUE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.NORMAL, COLOR_HEADER_BG);
    private static final Font FONT_TABLE_HEAD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.NORMAL, Color.WHITE);
    private static final Font FONT_TABLE_BODY = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, COLOR_HEADER_BG);
    private static final Font FONT_BADGE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Font.NORMAL, Color.WHITE);

    public byte[] generateHallTicketPdf(HallTicketModel hall, ExamModel exam, StudentModel stud) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 30, 30, 30, 30);
            PdfWriter.getInstance(document, baos);
            document.open();

            // --- 1. Header Block (Card Style) ---
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{3f, 1f});

            PdfPCell headerCell = new PdfPCell();
            headerCell.setBackgroundColor(COLOR_HEADER_BG);
            headerCell.setPadding(20);
            headerCell.setBorder(Rectangle.NO_BORDER);

            Paragraph title = new Paragraph("E-HALL TICKET", FONT_TITLE);
            headerCell.addElement(title);
            headerCell.addElement(new Paragraph(hall.getExamName(), FONT_SUBTITLE));
            headerTable.addCell(headerCell);

            // Approved Badge in Header
            PdfPCell badgeCell = new PdfPCell();
            badgeCell.setBackgroundColor(COLOR_HEADER_BG);
            badgeCell.setBorder(Rectangle.NO_BORDER);
            badgeCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            badgeCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            badgeCell.setPaddingRight(20);

            PdfPTable badge = new PdfPTable(1);
            badge.setWidthPercentage(80);
            PdfPCell bContent = new PdfPCell(new Phrase("APPROVED", FONT_BADGE));
            bContent.setBackgroundColor(COLOR_SUCCESS);
            bContent.setHorizontalAlignment(Element.ALIGN_CENTER);
            bContent.setPadding(4);
            bContent.setBorder(Rectangle.NO_BORDER);
            badge.addCell(bContent);
            badgeCell.addElement(badge);
            headerTable.addCell(badgeCell);

            document.add(headerTable);
            document.add(new Paragraph(" "));

            // --- 2. Student Info & Photo ---
            PdfPTable mainInfo = new PdfPTable(2);
            mainInfo.setWidthPercentage(100);
            mainInfo.setWidths(new float[]{0.8f, 3.2f}); // Photo, Details
            mainInfo.setSpacingBefore(10);

            // Photo
            PdfPCell photoCell = new PdfPCell();
            photoCell.setBorder(Rectangle.NO_BORDER);
            photoCell.setPaddingRight(15);
            
            boolean photoAdded = false;
            if (stud != null && stud.getImageName() != null && !stud.getImageName().isEmpty()) {
                try {
                    Image img = loadImage("static/img/" + stud.getImageName());
                    if (img != null) {
                        img.scaleToFit(100, 120);
                        photoCell.addElement(img);
                        photoAdded = true;
                        logger.info("Student photo loaded for PDF: {}", stud.getImageName());
                    }
                } catch (Exception e) {
                    logger.warn("Failed to add student photo to PDF: {}", e.getMessage());
                }
            }
            if (!photoAdded) {
                // Placeholder box
                PdfPTable ph = new PdfPTable(1);
                ph.setWidthPercentage(100);
                PdfPCell phCell = new PdfPCell(new Phrase("PHOTO", FONT_LABEL));
                phCell.setFixedHeight(100);
                phCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                phCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                phCell.setBackgroundColor(COLOR_BORDER);
                phCell.setBorderColor(COLOR_PRIMARY);
                ph.addCell(phCell);
                photoCell.addElement(ph);
            }
            mainInfo.addCell(photoCell);

            // Info Grid
            PdfPTable infoGrid = new PdfPTable(2);
            infoGrid.setWidthPercentage(100);
            infoGrid.setSpacingBefore(0);

            addStyledInfoCell(infoGrid, "CANDIDATE NAME", hall.getStudentName());
            addStyledInfoCell(infoGrid, "SEAT NUMBER", hall.getSeatNo());
            addStyledInfoCell(infoGrid, "INSTITUTION", hall.getInstituteName());
            addStyledInfoCell(infoGrid, "HALL TICKET NO", String.valueOf(hall.getId()));

            PdfPCell gridContainer = new PdfPCell(infoGrid);
            gridContainer.setBorder(Rectangle.NO_BORDER);
            mainInfo.addCell(gridContainer);
            
            document.add(mainInfo);
            document.add(new Paragraph(" "));

            // --- 3. Examination Schedule ---
            Paragraph schedHead = new Paragraph("EXAMINATION SCHEDULE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.NORMAL, COLOR_HEADER_BG));
            schedHead.setSpacingBefore(20);
            schedHead.setSpacingAfter(10);
            document.add(schedHead);

            PdfPTable subTable = new PdfPTable(4);
            subTable.setWidthPercentage(100);
            subTable.setWidths(new float[]{1f, 4f, 2f, 2f});
            
            addStyedTableHeader(subTable, "SR.");
            addStyedTableHeader(subTable, "SUBJECT");
            addStyedTableHeader(subTable, "DATE");
            addStyedTableHeader(subTable, "TIME");

            List<SubjectDTO> subjects = getSubjectsForPdf(hall, exam);
            if (subjects.isEmpty()) {
                PdfPCell empty = new PdfPCell(new Phrase("No subjects scheduled.", FONT_TABLE_BODY));
                empty.setColspan(4);
                empty.setPadding(10);
                empty.setHorizontalAlignment(Element.ALIGN_CENTER);
                subTable.addCell(empty);
            } else {
                int idx = 1;
                for (SubjectDTO s : subjects) {
                    addStyledTableBodyCell(subTable, String.valueOf(idx++));
                    addStyledTableBodyCell(subTable, s.getName());
                    addStyledTableBodyCell(subTable, s.getDate() != null && !s.getDate().isEmpty() ? s.getDate() : "-");
                    addStyledTableBodyCell(subTable, s.getTime() != null && !s.getTime().isEmpty() ? s.getTime() : "-");
                }
            }
            document.add(subTable);

            // --- 4. Footer: QR & Signature ---
            document.add(new Paragraph(" "));
            PdfPTable footer = new PdfPTable(2);
            footer.setWidthPercentage(100);
            footer.setSpacingBefore(30);

            // QR Code
            PdfPCell qrCell = new PdfPCell();
            qrCell.setBorder(Rectangle.NO_BORDER);
            if (hall.getQrName() != null && !hall.getQrName().isEmpty()) {
                try {
                    Image qr = loadImage("static/qr/" + hall.getQrName());
                    if (qr != null) {
                        qr.scaleToFit(80, 80);
                        qrCell.addElement(qr);
                        qrCell.addElement(new Paragraph("SCAN TO VERIFY", FONT_LABEL));
                        logger.info("QR code loaded for PDF: {}", hall.getQrName());
                    } else {
                        qrCell.addElement(new Paragraph("QR N/A", FONT_LABEL));
                    }
                } catch (Exception e) {
                    logger.warn("Failed to add QR code to PDF: {}", e.getMessage());
                }
            }
            footer.addCell(qrCell);

            // Signature
            PdfPCell sigCell = new PdfPCell();
            sigCell.setBorder(Rectangle.NO_BORDER);
            sigCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            sigCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            Paragraph sig = new Paragraph("__________________________\nCONTROLLER OF EXAMINATIONS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.NORMAL, COLOR_HEADER_BG));
            sig.setAlignment(Element.ALIGN_RIGHT);
            sigCell.addElement(sig);
            footer.addCell(sigCell);

            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addStyledInfoCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(8);
        cell.setBorder(Rectangle.NO_BORDER);
        
        PdfPTable inner = new PdfPTable(1);
        PdfPCell innerCell = new PdfPCell();
        innerCell.setPadding(6);
        innerCell.setPaddingLeft(10);
        innerCell.setBorderColorLeft(COLOR_PRIMARY);
        innerCell.setBorderWidthLeft(3f);
        innerCell.setBorderWidthBottom(0.5f);
        innerCell.setBorderColorBottom(COLOR_BORDER);
        innerCell.setBorderWidthTop(0);
        innerCell.setBorderWidthRight(0);
        innerCell.setBackgroundColor(new Color(248, 250, 252));

        innerCell.addElement(new Paragraph(label, FONT_LABEL));
        innerCell.addElement(new Paragraph(value != null && !value.isEmpty() ? value : "-", FONT_VALUE));
        
        inner.addCell(innerCell);
        cell.addElement(inner);
        table.addCell(cell);
    }

    private void addStyedTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_TABLE_HEAD));
        cell.setBackgroundColor(COLOR_PRIMARY);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(Color.WHITE);
        table.addCell(cell);
    }

    private void addStyledTableBodyCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null && !text.isEmpty() ? text : "-", FONT_TABLE_BODY));
        cell.setPadding(6);
        cell.setBorderColor(COLOR_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private List<SubjectDTO> getSubjectsForPdf(HallTicketModel hall, ExamModel exam) {
        List<SubjectDTO> list = new ArrayList<>();
        if (hall != null && hall.getSubjects() != null && !hall.getSubjects().isEmpty()) {
            return hall.getSubjects();
        }
        if (exam != null && exam.getSubjects() != null && !exam.getSubjects().isEmpty()) {
            return exam.getSubjects();
        }
        for (int i = 1; i <= 6; i++) {
            String sub = getHallSub(hall, i);
            String date = (exam != null) ? getExamDate(exam, i) : "-";
            String time = (exam != null) ? getExamTime(exam, i) : "-";
            if (sub == null || sub.isEmpty()) {
                sub = (exam != null) ? getExamSub(exam, i) : null;
            }
            if (sub != null && !sub.isEmpty()) {
                list.add(new SubjectDTO(sub, date, time));
            }
        }
        return list;
    }

    private String getExamSub(ExamModel exam, int i) {
        if (exam == null) return null;
        return switch (i) {
            case 1 -> exam.getSub1();
            case 2 -> exam.getSub2();
            case 3 -> exam.getSub3();
            case 4 -> exam.getSub4();
            case 5 -> exam.getSub5();
            case 6 -> exam.getSub6();
            default -> null;
        };
    }

    private String getExamDate(ExamModel exam, int i) {
        if (exam == null) return null;
        return switch (i) {
            case 1 -> exam.getDate1();
            case 2 -> exam.getDate2();
            case 3 -> exam.getDate3();
            case 4 -> exam.getDate4();
            case 5 -> exam.getDate5();
            case 6 -> exam.getDate6();
            default -> null;
        };
    }

    private String getExamTime(ExamModel exam, int i) {
        if (exam == null) return null;
        return switch (i) {
            case 1 -> exam.getTime1();
            case 2 -> exam.getTime2();
            case 3 -> exam.getTime3();
            case 4 -> exam.getTime4();
            case 5 -> exam.getTime5();
            case 6 -> exam.getTime6();
            default -> null;
        };
    }

    private String getHallSub(HallTicketModel hall, int i) {
        if (hall == null) return null;
        return switch (i) {
            case 1 -> hall.getSub1();
            case 2 -> hall.getSub2();
            case 3 -> hall.getSub3();
            case 4 -> hall.getSub4();
            case 5 -> hall.getSub5();
            case 6 -> hall.getSub6();
            default -> null;
        };
    }
}
