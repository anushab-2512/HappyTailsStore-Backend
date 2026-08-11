package com.registration.order;

import com.registration.entity.Order;
import com.registration.entity.OrderItem;
import com.registration.entity.User;
import com.registration.product.Product;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.properties.BorderRadius;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class InvoiceService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private static final DeviceRgb WHITE         = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb BRAND_BROWN   = new DeviceRgb(155, 69, 0);
    private static final DeviceRgb BRAND_ORANGE  = new DeviceRgb(255, 145, 77);
    private static final DeviceRgb BRAND_CREAM   = new DeviceRgb(252, 249, 244);
    private static final DeviceRgb TEXT_DARK     = new DeviceRgb(45, 45, 45);
    private static final DeviceRgb TEXT_MEDIUM   = new DeviceRgb(138, 122, 109);
    private static final DeviceRgb TEXT_LIGHT    = new DeviceRgb(160, 160, 160);
    private static final DeviceRgb DIVIDER       = new DeviceRgb(234, 223, 206);
    private static final DeviceRgb TABLE_HDR     = new DeviceRgb(254, 243, 199);
    private static final DeviceRgb TABLE_ALT     = new DeviceRgb(249, 246, 241);
    private static final DeviceRgb GREEN_BG      = new DeviceRgb(220, 252, 231);
    private static final DeviceRgb GREEN_TXT     = new DeviceRgb(22, 163, 74);

    private final OrderItemRepository orderItemRepository;

    public InvoiceService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    public byte[] generateInvoice(Order order, User user) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
            Document doc = new Document(pdf);
            doc.setMargins(0, 0, 0, 0);

            PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            addAccentBar(doc);
            addHeader(doc, order, bold, regular);
            addMetaInfo(doc, order, user, bold, regular);
            addItemsTable(doc, order, bold, regular);
            addTotalsSection(doc, order, bold, regular);
            addPaymentNote(doc, bold, regular);
            addFooter(doc, bold, regular);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }

    private void addAccentBar(Document doc) {
        Table bar = new Table(1).useAllAvailableWidth();
        bar.setBorder(null);
        Cell c = new Cell().setBorder(null).setHeight(8).setBackgroundColor(BRAND_BROWN);
        bar.addCell(c);
        doc.add(bar);
    }

    private void addHeader(Document doc, Order order, PdfFont bold, PdfFont regular) {
        Table t = new Table(UnitValue.createPercentArray(new float[]{3, 2})).useAllAvailableWidth();
        t.setBorder(null);
        t.setMarginTop(30);
        t.setMarginBottom(28);
        t.setPaddingLeft(55);
        t.setPaddingRight(55);

        Cell brand = new Cell().setBorder(null).setVerticalAlignment(VerticalAlignment.MIDDLE).setPaddingRight(20);
        brand.add(new Paragraph("\uD83D\uDC3E HappyTailsStore").setFont(bold).setFontSize(22).setFontColor(BRAND_BROWN).setMarginBottom(2));
        brand.add(new Paragraph("Premium Pet Products & Accessories").setFont(regular).setFontSize(9).setFontColor(TEXT_MEDIUM));
        t.addCell(brand);

        Cell right = new Cell().setBorder(null).setTextAlignment(TextAlignment.RIGHT).setVerticalAlignment(VerticalAlignment.MIDDLE);
        right.add(new Paragraph("INVOICE").setFont(bold).setFontSize(26).setFontColor(BRAND_BROWN).setMarginBottom(2));

        String shortId = order.getOrderId() != null
                ? order.getOrderId().substring(0, Math.min(8, order.getOrderId().length())) : "N/A";
        right.add(new Paragraph("#" + shortId).setFont(regular).setFontSize(9).setFontColor(TEXT_LIGHT).setMarginBottom(6));

        String st = "SUCCESS";
        DeviceRgb sbg = GREEN_BG, sfg = GREEN_TXT;
        if ("CANCELLED".equalsIgnoreCase(String.valueOf(order.getStatus()))) {
            sbg = new DeviceRgb(254, 226, 226);
            sfg = new DeviceRgb(220, 38, 38);
            st = "CANCELLED";
        }
        Table badge = new Table(1);
        badge.setBorder(null);
        badge.setWidth(UnitValue.createPercentValue(55));
        Cell bc = new Cell().setBorder(null).setBackgroundColor(sbg)                .setBorderRadius(new BorderRadius(4)).setPadding(5).setTextAlignment(TextAlignment.CENTER);
        bc.add(new Paragraph(st).setFont(bold).setFontSize(9).setFontColor(sfg).setTextAlignment(TextAlignment.CENTER));
        badge.addCell(bc);

        Table wrap = new Table(1);
        wrap.setBorder(null);
        wrap.setWidth(UnitValue.createPercentValue(100));
        Cell wc = new Cell().setBorder(null).setTextAlignment(TextAlignment.RIGHT);
        wc.add(badge);
        right.add(wrap);

        t.addCell(right);
        doc.add(t);
    }

    private void addMetaInfo(Document doc, Order order, User user, PdfFont bold, PdfFont regular) {
        doc.add(new Paragraph("").setBorder(new SolidBorder(DIVIDER, 0.8f))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginLeft(55).setMarginRight(55).setMarginBottom(20));

        Table t = new Table(UnitValue.createPercentArray(new float[]{2, 1.5f, 1.5f})).useAllAvailableWidth();
        t.setBorder(null);
        t.setPaddingLeft(55).setPaddingRight(55);
        t.setMarginBottom(24);

        Cell b1 = metaCell("BILLED TO", bold);
        b1.add(new Paragraph(user.getUserName()).setFont(bold).setFontSize(11).setFontColor(TEXT_DARK).setMarginTop(6).setMarginBottom(1));
        if (!user.getUserName().equalsIgnoreCase(user.getEmail())) {
            b1.add(new Paragraph(user.getEmail()).setFont(regular).setFontSize(9.5f).setFontColor(TEXT_MEDIUM));
        }
        t.addCell(b1);

        Cell b2 = metaCell("INVOICE DATE", bold);
        b2.add(new Paragraph(order.getCreatedAt().format(DATE_FMT)).setFont(bold).setFontSize(11).setFontColor(TEXT_DARK).setMarginTop(6).setMarginBottom(1));
        b2.add(new Paragraph("at " + order.getCreatedAt().format(TIME_FMT)).setFont(regular).setFontSize(9.5f).setFontColor(TEXT_MEDIUM));
        t.addCell(b2);

        Cell b3 = metaCell("PAYMENT METHOD", bold);
        b3.add(new Paragraph("Razorpay").setFont(bold).setFontSize(11).setFontColor(TEXT_DARK).setMarginTop(6).setMarginBottom(1));
        b3.add(new Paragraph("Online Payment").setFont(regular).setFontSize(9.5f).setFontColor(TEXT_MEDIUM));
        t.addCell(b3);

        doc.add(t);
    }

    private Cell metaCell(String label, PdfFont bold) {
        Cell c = new Cell().setBorder(null).setPadding(0);
        c.add(new Paragraph(label).setFont(bold).setFontSize(8).setFontColor(BRAND_ORANGE).setMarginBottom(0));
        return c;
    }

    private void addItemsTable(Document doc, Order order, PdfFont bold, PdfFont regular) {
        float[] cols = {3.5f, 1, 1.5f, 1.5f};
        Table t = new Table(UnitValue.createPercentArray(cols)).useAllAvailableWidth();
        t.setBorder(null);
        t.setPaddingLeft(55).setPaddingRight(55);
        t.setMarginBottom(20);

        String[] headers = {"Item", "Qty", "Unit Price", "Total"};
        TextAlignment[] aligns = {TextAlignment.LEFT, TextAlignment.CENTER, TextAlignment.RIGHT, TextAlignment.RIGHT};
        for (int i = 0; i < headers.length; i++) {
            Cell h = new Cell().setBackgroundColor(TABLE_HDR).setPadding(12).setBorder(null);
            h.add(new Paragraph(headers[i]).setFont(bold).setFontSize(9).setFontColor(BRAND_BROWN).setTextAlignment(aligns[i]));
            t.addCell(h);
        }

        List<OrderItem> items = orderItemRepository.findByOrder_OrderId(order.getOrderId());
        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            Product product = item.getProduct();
            DeviceRgb bg = (i % 2 == 0) ? WHITE : TABLE_ALT;

            Cell nameC = new Cell().setBackgroundColor(bg).setPadding(12).setBorder(new SolidBorder(DIVIDER, 0.5f));
            nameC.add(new Paragraph(product.getName()).setFont(regular).setFontSize(10.5f).setFontColor(TEXT_DARK));
            t.addCell(nameC);

            Cell qtyC = new Cell().setBackgroundColor(bg).setPadding(12).setBorder(new SolidBorder(DIVIDER, 0.5f));
            qtyC.add(new Paragraph(String.valueOf(item.getQuantity())).setFont(regular).setFontSize(10.5f).setFontColor(TEXT_DARK).setTextAlignment(TextAlignment.CENTER));
            t.addCell(qtyC);

            Cell priceC = new Cell().setBackgroundColor(bg).setPadding(12).setBorder(new SolidBorder(DIVIDER, 0.5f));
            priceC.add(new Paragraph("\u20B9" + item.getPricePerUnit().setScale(2, BigDecimal.ROUND_HALF_UP))
                    .setFont(regular).setFontSize(10.5f).setFontColor(TEXT_DARK).setTextAlignment(TextAlignment.RIGHT));
            t.addCell(priceC);

            Cell totalC = new Cell().setBackgroundColor(bg).setPadding(12).setBorder(new SolidBorder(DIVIDER, 0.5f));
            totalC.add(new Paragraph("\u20B9" + item.getTotalPrice().setScale(2, BigDecimal.ROUND_HALF_UP))
                    .setFont(regular).setFontSize(10.5f).setFontColor(TEXT_DARK).setTextAlignment(TextAlignment.RIGHT));
            t.addCell(totalC);
        }
        doc.add(t);
    }

    private void addTotalsSection(Document doc, Order order, PdfFont bold, PdfFont regular) {
        Table outer = new Table(UnitValue.createPercentArray(new float[]{2, 1})).useAllAvailableWidth();
        outer.setBorder(null);
        outer.setPaddingLeft(55).setPaddingRight(55);
        outer.setMarginBottom(20);

        Cell left = new Cell().setBorder(null).setPadding(0);
        left.add(new Paragraph("Terms & Conditions").setFont(bold).setFontSize(11).setFontColor(TEXT_DARK).setMarginBottom(6));
        left.add(new Paragraph("All payments are due within 30 days. Late payment fees may apply.")
                .setFont(regular).setFontSize(8.5f).setFontColor(TEXT_MEDIUM));
        outer.addCell(left);

        BigDecimal sub = order.getTotalAmount();
        BigDecimal tax = sub.multiply(new BigDecimal("0.10"));
        BigDecimal shipping = sub.compareTo(new BigDecimal("1000")) < 0 ? new BigDecimal("100") : BigDecimal.ZERO;
        BigDecimal total = sub.add(tax).add(shipping);

        Table totals = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
        totals.setBorder(null);

        totals.addCell(labelCell("Sub Total", regular));
        totals.addCell(valueCell("\u20B9" + sub.setScale(2, BigDecimal.ROUND_HALF_UP), regular));

        totals.addCell(labelCell("Tax (10%)", regular));
        totals.addCell(valueCell("\u20B9" + tax.setScale(2, BigDecimal.ROUND_HALF_UP), regular));

        totals.addCell(labelCell("Shipping", regular));
        totals.addCell(valueCell(shipping.compareTo(BigDecimal.ZERO) == 0 ? "Free" : "\u20B9" + shipping.setScale(2, BigDecimal.ROUND_HALF_UP), regular));

        Cell dividerLabel = new Cell().setBorderTop(new SolidBorder(BRAND_BROWN, 1.5f)).setPaddingTop(8).setPaddingBottom(6).setBorder(null);
        dividerLabel.add(new Paragraph(""));
        totals.addCell(dividerLabel);
        Cell dividerVal = new Cell().setBorderTop(new SolidBorder(BRAND_BROWN, 1.5f)).setPaddingTop(8).setPaddingBottom(6).setBorder(null);
        dividerVal.add(new Paragraph(""));
        totals.addCell(dividerVal);

        Cell grandL = new Cell().setBorder(null).setPadding(6);
        grandL.add(new Paragraph("Total").setFont(bold).setFontSize(14).setFontColor(BRAND_BROWN).setTextAlignment(TextAlignment.RIGHT));
        totals.addCell(grandL);
        Cell grandV = new Cell().setBorder(null).setPadding(6);
        grandV.add(new Paragraph("\u20B9" + total.setScale(2, BigDecimal.ROUND_HALF_UP))
                .setFont(bold).setFontSize(14).setFontColor(BRAND_BROWN).setTextAlignment(TextAlignment.RIGHT));
        totals.addCell(grandV);

        Cell right = new Cell().setBorder(null).setPadding(0).setBorderRadius(new BorderRadius(8))
                .setBackgroundColor(BRAND_CREAM).setPadding(14);
        right.add(totals);
        outer.addCell(right);

        doc.add(outer);
    }

    private Cell labelCell(String text, PdfFont font) {
        Cell c = new Cell().setBorder(null).setPadding(4);
        c.add(new Paragraph(text).setFont(font).setFontSize(10).setFontColor(TEXT_MEDIUM).setTextAlignment(TextAlignment.RIGHT));
        return c;
    }

    private Cell valueCell(String text, PdfFont font) {
        Cell c = new Cell().setBorder(null).setPadding(4);
        c.add(new Paragraph(text).setFont(font).setFontSize(10).setFontColor(TEXT_DARK).setTextAlignment(TextAlignment.RIGHT));
        return c;
    }

    private void addPaymentNote(Document doc, PdfFont bold, PdfFont regular) {
        doc.add(new Paragraph("")
                .setBorder(new SolidBorder(DIVIDER, 0.8f))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginLeft(55).setMarginRight(55).setMarginBottom(14));

        Table t = new Table(1).useAllAvailableWidth();
        t.setBorder(null);
        t.setPaddingLeft(55).setPaddingRight(55);
        t.setMarginBottom(24);

        Cell c = new Cell().setBorder(null).setBackgroundColor(BRAND_CREAM).setPadding(14).setBorderRadius(new BorderRadius(6));
        c.add(new Paragraph("\uD83D\uDC3E  This invoice has been paid in full via Razorpay. No further action is required.")
                .setFont(regular).setFontSize(10).setFontColor(TEXT_MEDIUM));
        t.addCell(c);
        doc.add(t);
    }

    private void addFooter(Document doc, PdfFont bold, PdfFont regular) {
        Table bar = new Table(1).useAllAvailableWidth();
        bar.setBorder(null);
        Cell bc = new Cell().setBorder(null).setHeight(4).setBackgroundColor(BRAND_BROWN);
        bar.addCell(bc);
        doc.add(bar);

        doc.add(new Paragraph("\uD83D\uDC3E \uD83D\uDC3E \uD83D\uDC3E \uD83D\uDC3E \uD83D\uDC3E")
                .setFont(regular).setFontSize(12).setFontColor(BRAND_ORANGE)
                .setTextAlignment(TextAlignment.CENTER).setMarginTop(16).setMarginBottom(6));

        doc.add(new Paragraph("Thank you fur-shopping with HappyTailsStore!")
                .setFont(bold).setFontSize(13).setFontColor(BRAND_BROWN)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(4));

        doc.add(new Paragraph("support@happytailsstore.com  |  www.happytailsstore.com")
                .setFont(regular).setFontSize(8.5f).setFontColor(TEXT_LIGHT)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(2));

        doc.add(new Paragraph("This is a computer-generated invoice and does not require a signature.")
                .setFont(regular).setFontSize(7.5f).setFontColor(TEXT_LIGHT)
                .setTextAlignment(TextAlignment.CENTER).setMarginTop(8));
    }
}
