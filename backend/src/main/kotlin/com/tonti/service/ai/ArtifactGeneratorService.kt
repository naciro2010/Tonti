package com.tonti.service.ai

import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import mu.KotlinLogging
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

@Service
class ArtifactGeneratorService {

    fun generatePdf(data: Map<String, Any>): ByteArray {
        val outputStream = ByteArrayOutputStream()

        val pdfWriter = PdfWriter(outputStream)
        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument, PageSize.A4)
        document.setMargins(40f, 40f, 40f, 40f)

        // Title
        val title = data["title"] as? String ?: "Rapport Tonti"
        document.add(
            Paragraph(title)
                .setFontSize(24f)
                .setBold()
                .setFontColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10f)
        )

        // Date
        val dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        document.add(
            Paragraph("Généré le $dateStr")
                .setFontSize(10f)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30f)
        )

        // Sections
        @Suppress("UNCHECKED_CAST")
        val sections = data["sections"] as? List<Map<String, String>> ?: emptyList()
        for (section in sections) {
            val heading = section["heading"] ?: ""
            val content = section["content"] ?: ""

            if (heading.isNotBlank()) {
                document.add(
                    Paragraph(heading)
                        .setFontSize(16f)
                        .setBold()
                        .setFontColor(ColorConstants.DARK_GRAY)
                        .setMarginTop(15f)
                        .setMarginBottom(8f)
                )
            }

            if (content.isNotBlank()) {
                document.add(
                    Paragraph(content)
                        .setFontSize(11f)
                        .setMarginBottom(10f)
                )
            }
        }

        // Table data if present
        @Suppress("UNCHECKED_CAST")
        val headers = data["headers"] as? List<String>
        @Suppress("UNCHECKED_CAST")
        val rows = data["rows"] as? List<List<Any>>

        if (headers != null && rows != null) {
            val table = Table(UnitValue.createPercentArray(headers.size)).useAllAvailableWidth()
            table.setMarginTop(15f)

            // Headers
            for (header in headers) {
                table.addHeaderCell(
                    Cell().add(Paragraph(header).setBold().setFontSize(10f))
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setPadding(5f)
                )
            }

            // Rows
            for (row in rows) {
                for (cell in row) {
                    table.addCell(
                        Cell().add(Paragraph(cell.toString()).setFontSize(10f))
                            .setPadding(4f)
                    )
                }
            }

            document.add(table)
        }

        // Footer
        val footer = data["footer"] as? String ?: "Tonti - Rapport généré automatiquement"
        document.add(
            Paragraph(footer)
                .setFontSize(8f)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30f)
        )

        document.close()
        logger.info { "Generated PDF: $title (${outputStream.size()} bytes)" }
        return outputStream.toByteArray()
    }

    fun generateXlsx(data: Map<String, Any>): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val workbook = XSSFWorkbook()

        val sheetName = data["sheetName"] as? String ?: "Données"
        val sheet = workbook.createSheet(sheetName)

        // Header style
        val headerStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.DARK_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            val font = workbook.createFont().apply {
                color = IndexedColors.WHITE.index
                bold = true
                fontHeightInPoints = 11
            }
            setFont(font)
        }

        // Data style
        val dataStyle = workbook.createCellStyle().apply {
            borderBottom = org.apache.poi.ss.usermodel.BorderStyle.THIN
            borderTop = org.apache.poi.ss.usermodel.BorderStyle.THIN
            borderLeft = org.apache.poi.ss.usermodel.BorderStyle.THIN
            borderRight = org.apache.poi.ss.usermodel.BorderStyle.THIN
        }

        @Suppress("UNCHECKED_CAST")
        val headers = data["headers"] as? List<String> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val rows = data["rows"] as? List<List<Any>> ?: emptyList()

        // Headers
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }

        // Data rows
        rows.forEachIndexed { rowIndex, rowData ->
            val row = sheet.createRow(rowIndex + 1)
            rowData.forEachIndexed { colIndex, value ->
                val cell = row.createCell(colIndex)
                when (value) {
                    is Number -> cell.setCellValue(value.toDouble())
                    is Boolean -> cell.setCellValue(value)
                    else -> cell.setCellValue(value.toString())
                }
                cell.cellStyle = dataStyle
            }
        }

        // Auto-size columns
        headers.indices.forEach { sheet.autoSizeColumn(it) }

        workbook.write(outputStream)
        workbook.close()

        logger.info { "Generated XLSX: $sheetName (${outputStream.size()} bytes)" }
        return outputStream.toByteArray()
    }
}
