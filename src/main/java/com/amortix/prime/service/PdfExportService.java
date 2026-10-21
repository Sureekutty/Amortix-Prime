package com.amortix.prime.service;

import com.amortix.prime.model.Loan;
import com.amortix.prime.model.Member;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

@Service
public class PdfExportService {

    public byte[] generateLoanSanctionPdf(Loan loan, Member member) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Header
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Paragraph title = new Paragraph("AMORTIX | PRIME", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.GRAY);
            Paragraph sub = new Paragraph("COOPERATIVE CREDIT & CORE LEDGER ARCHITECTURE\nLOAN SANCTION & DISBURSEMENT ORDER", subTitleFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            document.add(sub);
            document.add(new Paragraph(" "));

            // Details Table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            addTableCell(table, "Loan Account Number:", loan.getLoanAccountNo());
            addTableCell(table, "Loan Product Type:", loan.getLoanType());
            addTableCell(table, "Borrower Member Code:", loan.getMemberCode());
            addTableCell(table, "Borrower Full Name:", loan.getMemberName());
            addTableCell(table, "Employee ID:", loan.getEmployeeCode());
            addTableCell(table, "Department:", loan.getDepartment());
            addTableCell(table, "Sanctioned Principal Amount:", "INR " + loan.getSanctionAmount());
            addTableCell(table, "Annual Interest Rate:", loan.getInterestRate() + "% p.a.");
            addTableCell(table, "Loan Tenure:", loan.getTenureMonths() + " Months");
            addTableCell(table, "Monthly Installment (EMI):", "INR " + loan.getMonthlyInstallment());
            addTableCell(table, "Sanction Date:", loan.getSanctionDate() != null ? loan.getSanctionDate().toString() : LocalDate.now().toString());
            addTableCell(table, "Status:", loan.getStatus());

            document.add(table);

            // Surety Section
            Paragraph suretyHeader = new Paragraph("Guarantor & Surety Details", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            suretyHeader.setSpacingBefore(10f);
            document.add(suretyHeader);

            PdfPTable sTable = new PdfPTable(3);
            sTable.setWidthPercentage(100);
            sTable.setSpacingBefore(5f);
            sTable.addCell(getHeaderCell("Surety Member Code"));
            sTable.addCell(getHeaderCell("Guarantor Name"));
            sTable.addCell(getHeaderCell("Status"));

            if (loan.getSureties() != null && !loan.getSureties().isEmpty()) {
                for (Loan.LoanSurety s : loan.getSureties()) {
                    sTable.addCell(s.getSuretyMemberCode() != null ? s.getSuretyMemberCode() : "-");
                    sTable.addCell(s.getSuretyMemberName() != null ? s.getSuretyMemberName() : "-");
                    sTable.addCell(s.getStatus() != null ? s.getStatus() : "CONFIRMED");
                }
            } else {
                PdfPCell cell = new PdfPCell(new Phrase("No sureties attached"));
                cell.setColspan(3);
                sTable.addCell(cell);
            }
            document.add(sTable);

            // Signature block
            Paragraph sign = new Paragraph("\n\n\n______________________                    ______________________\nBorrower Signature                          Sanctioning Authority",
                    FontFactory.getFont(FontFactory.HELVETICA, 10));
            sign.setAlignment(Element.ALIGN_CENTER);
            document.add(sign);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    private void addTableCell(PdfPTable table, String label, String value) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        c1.setBackgroundColor(new Color(245, 245, 245));
        c1.setPadding(6);
        PdfPCell c2 = new PdfPCell(new Phrase(value != null ? value : "-", FontFactory.getFont(FontFactory.HELVETICA, 10)));
        c2.setPadding(6);
        table.addCell(c1);
        table.addCell(c2);
    }

    private PdfPCell getHeaderCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
        cell.setBackgroundColor(new Color(30, 41, 59));
        cell.setPadding(6);
        return cell;
    }
}
