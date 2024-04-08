package top.linrty.netdisk.common.util;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hslf.extractor.QuickButCruddyTextExtractor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xslf.extractor.XSLFExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class ReadFileUtil {
    public static String getContentByInputStream(String fileType, InputStream inputStream) throws IOException {
        if (!"doc".equals(fileType) && !"docx".equals(fileType)) {
            if (!"xlsx".equals(fileType) && !"xls".equals(fileType)) {
                if ("txt".equals(fileType)) {
                    return readTxt(inputStream, fileType);
                } else if ("pdf".equals(fileType)) {
                    return readPdf(inputStream);
                } else if (!"ppt".equals(fileType) && !"pptx".equals(fileType)) {
                    System.out.println("不支持的文件类型！");
                    return "";
                } else {
                    return readPPT(inputStream, fileType);
                }
            } else {
                return readExcel(inputStream, fileType);
            }
        } else {
            return readWord(inputStream, fileType);
        }
    }

    public static String readPdf(InputStream inputStream) {
        PDDocument pdDocument = null;
        String content = "";

        try {
            PDFParser pdfParser = new PDFParser(new RandomAccessReadBuffer(inputStream));
            pdDocument = pdfParser.parse();
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            content = pdfTextStripper.getText(pdDocument);
        } catch (IOException var8) {
            var8.printStackTrace();
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(pdDocument);
        }

        return content;
    }

    private static String readTxt(InputStream inputStream, String extendName) throws IOException {
        String var4;
        try {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            byte[] result = CharsetUtils.convertTxtCharsetToUTF8(bytes, extendName);
            var4 = IOUtils.toString(result, "UTF-8");
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        return var4;
    }

    private static String readExcel(InputStream inputStream, String extendName) {
        Workbook wb = null;

        try {
            if ("xls".equalsIgnoreCase(extendName)) {
                wb = new HSSFWorkbook(inputStream);
            } else {
                if (!"xlsx".equalsIgnoreCase(extendName)) {
                    System.out.println("文件类型错误!");
                    String var19 = "";
                    return var19;
                }

                wb = new XSSFWorkbook(inputStream);
            }

            StringBuilder sb = new StringBuilder();

            for(int i = 0; i < ((Workbook)wb).getNumberOfSheets(); ++i) {
                Sheet sheet = ((Workbook)wb).getSheetAt(i);
                sb.append(sheet.getSheetName()).append("_");
                int firstRowIndex = sheet.getFirstRowNum() + 1;
                int lastRowIndex = sheet.getLastRowNum();

                for(int rIndex = firstRowIndex; rIndex <= lastRowIndex; ++rIndex) {
                    Row row = sheet.getRow(rIndex);
                    if (row != null) {
                        int firstCellIndex = row.getFirstCellNum();
                        int lastCellIndex = row.getLastCellNum();

                        for(int cIndex = firstCellIndex; cIndex < lastCellIndex; ++cIndex) {
                            Cell cell = row.getCell(cIndex);
                            if (cell != null) {
                                sb.append(cell);
                            }
                        }
                    }
                }
            }

            String var20 = sb.toString();
            return var20;
        } catch (Exception var17) {
            var17.printStackTrace();
        } finally {
            IOUtils.closeQuietly((Closeable)wb);
            IOUtils.closeQuietly(inputStream);
        }

        return "";
    }

    public static String readWord(InputStream inputStream, String fileType) {
        String buffer = "";

        try {
            if ("doc".equalsIgnoreCase(fileType)) {
                WordExtractor ex = new WordExtractor(inputStream);
                buffer = ex.getText();
                ex.close();
            } else if ("docx".equalsIgnoreCase(fileType)) {
                XWPFWordExtractor extractor = new XWPFWordExtractor(OPCPackage.open(inputStream));
                buffer = extractor.getText();
                extractor.close();
            } else {
                System.out.println("此文件不是word文件！");
            }
        } catch (Exception var7) {
            var7.printStackTrace();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        return buffer;
    }

    private static String readPPT(InputStream inputStream, String fileType) {
        String buffer = "";

        try {
            if ("ppt".equalsIgnoreCase(fileType)) {
                QuickButCruddyTextExtractor extractor = new QuickButCruddyTextExtractor(inputStream);
                buffer = extractor.getTextAsString();
                extractor.close();
            } else if ("pptx".equalsIgnoreCase(fileType)) {
                XSLFExtractor extractor = new XSLFExtractor(new XMLSlideShow(OPCPackage.open(inputStream)));
                buffer = extractor.getText();
                extractor.close();
            }
        } catch (IOException var4) {
            var4.fillInStackTrace();
        } catch (OpenXML4JException var5) {
            var5.getMessage();
        }

        return buffer;
    }
}
