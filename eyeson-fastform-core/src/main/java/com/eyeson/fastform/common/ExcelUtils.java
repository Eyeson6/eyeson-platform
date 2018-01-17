package com.eyeson.fastform.common;

import com.eyeson.fastform.file.TableColumn;
import com.eyeson.fastform.file.TableDataRow;
import com.eyeson.fastform.file.TableHeaderMetaData;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.Region;
import org.apache.poi.ss.usermodel.*;

import java.beans.PropertyDescriptor;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 版权：融贯资讯 <br/>
 * 作者：zhengbin.jin@rogrand.com <br/>
 * 生成日期：2016年3月9日 <br/>
 * 描述：〈描述〉
 */
public class ExcelUtils {

    /**
     * 读取excel返回list集合
     *
     * @param fs
     * @return
     */
    public static List<List<String>> readExcel(FileInputStream fs)
            throws Exception {
        List<List<String>> rowList = new ArrayList<List<String>>();
        try {
            Workbook workBook = WorkbookFactory.create(fs);
            for (int i = 0; i < workBook.getNumberOfSheets(); i++) {
                // 创建工作表
                Sheet sheet = workBook.getSheetAt(i);
                int rows = sheet.getPhysicalNumberOfRows(); // 获得行数
                if (rows > 0) {
                    sheet.getMargin(HSSFSheet.TopMargin);
                    for (int r = 0; r < rows; r++) { // 行循环
                        Row row = sheet.getRow(r);
                        if (row != null && r != 0) {// 不取第一行
                            int cells = row.getLastCellNum();// 获得列数
                            // 定义集合datas用于存Excel中一个行的数据
                            Vector<String> datas = new Vector<String>();
                            for (short c = 0; c < cells; c++) { // 列循环
                                Cell cell = row.getCell(c);
                                if (cell != null) {
                                    String value;
                                    value = getValue(cell);
                                    datas.add(value);
                                } else {
                                    datas.add(null);
                                }
                            }
                            rowList.add(datas);
                        }
                    }
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return rowList;
    }

    /**
     * 读取excel返回Map集合
     *
     * @param fs
     * @return
     */
    public static List<Map> readExcelForButton(FileInputStream fs, Map byttonMap)
            throws Exception {
        List<Map> rowList = new ArrayList<Map>();
        try {
            List<Map> maps = (List<Map>) byttonMap.get("conditions");
            Workbook workBook = WorkbookFactory.create(fs);
            for (int i = 0; i < workBook.getNumberOfSheets(); i++) {
                // 创建工作表
                Sheet sheet = workBook.getSheetAt(i);
                int rows = sheet.getPhysicalNumberOfRows(); // 获得行数
                if (rows > 0) {
                    sheet.getMargin(HSSFSheet.TopMargin);
                    for (int r = 0; r < rows; r++) { // 行循环
                        Row row = sheet.getRow(r);
                        if (row != null && r != 0) {// 不取第一行
                            int cells = row.getLastCellNum();// 获得列数
                            // 定义集合datas用于存Excel中一个行的数据
                            Map datas = new HashMap();
                            for (short c = 0; c < (cells > maps.size() ? maps
                                    .size() : cells); c++) { // 列循环
                                Cell cell = row.getCell(c);
                                if (cell != null) {
                                    String value;
                                    value = getValue(cell);
                                    datas.put(maps.get(c).get("@name"), value);
                                } else {
                                    datas.put(maps.get(c).get("@name"), null);
                                }
                            }
                            rowList.add(datas);
                        }
                    }
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return rowList;
    }

    /**
     * 读取excel返回Map集合
     *
     * @param fs
     * @return
     */
    public static List<Map> readExcelM(FileInputStream fs, Map templateMap)
            throws Exception {
        List<Map> rowList = new ArrayList<Map>();
        try {
            Map map = (Map) templateMap.get("insert");
            List<Map> maps = (List<Map>) map.get("conditions");
            Workbook workBook = WorkbookFactory.create(fs);
            for (int i = 0; i < workBook.getNumberOfSheets(); i++) {
                // 创建工作表
                Sheet sheet = workBook.getSheetAt(i);
                int rows = sheet.getPhysicalNumberOfRows(); // 获得行数
                if (rows > 0) {
                    sheet.getMargin(HSSFSheet.TopMargin);
                    for (int r = 0; r < rows; r++) { // 行循环
                        Row row = sheet.getRow(r);
                        if (row != null && r != 0) {// 不取第一行
                            int cells = row.getLastCellNum();// 获得列数
                            // 定义集合datas用于存Excel中一个行的数据
                            Map datas = new HashMap();
                            for (short c = 0; c < (cells > maps.size() ? maps
                                    .size() : cells); c++) { // 列循环
                                Cell cell = row.getCell(c);
                                if (cell != null) {
                                    String value;
                                    value = getValue(cell);
                                    datas.put(maps.get(c).get("@name"), value);
                                } else {
                                    datas.put(maps.get(c).get("@name"), null);
                                }
                            }
                            rowList.add(datas);
                        }
                    }
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return rowList;
    }

    /**
     * 获取Excel中某个单元格的值
     *
     * @param cell
     * @return
     * @throws ParseException
     */
    public static String getValue(Cell cell) throws ParseException {
        String value = "";
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC: // 数值型
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    // 如果是date类型则 ，获取该cell的date值
                    value = HSSFDateUtil.getJavaDate(cell.getNumericCellValue())
                            .toString();
                    @SuppressWarnings("deprecation")
                    java.util.Date date1 = new Date(value);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    value = format.format(date1);
                } else {// 纯数字
                    cell.setCellType(1);
                    value = String.valueOf(cell.getStringCellValue());
                }
                break;
            /* 此行表示单元格的内容为string类型 */
            case Cell.CELL_TYPE_STRING: // 字符串型
                value = cell.getStringCellValue();
                break;
            case Cell.CELL_TYPE_FORMULA:// 公式型
                // 读公式计算值
                value = String.valueOf(cell.getNumericCellValue());
                if (value.equals("NaN")) {// 如果获取的数据值为非法值,则转换为获取字符串
                    value = cell.getStringCellValue().toString();
                }
                cell.getCellFormula();
                break;
            case Cell.CELL_TYPE_BOOLEAN:// 布尔
                value = " " + cell.getBooleanCellValue();
                break;
            /* 此行表示该单元格值为空 */
            case Cell.CELL_TYPE_BLANK: // 空值
                value = "";
                break;
            case Cell.CELL_TYPE_ERROR: // 故障
                value = "";
                break;
            default:
                value = cell.getStringCellValue().toString();
        }
        return value;
    }

    /**
     * JavaBean转Map
     *
     * @param obj
     * @return
     */
    public static Map<String, Object> beanToMap(Object obj) {
        Map<String, Object> params = new HashMap<String, Object>(0);
        try {
            PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
            PropertyDescriptor[] descriptors = propertyUtilsBean
                    .getPropertyDescriptors(obj);
            for (int i = 0; i < descriptors.length; i++) {
                String name = descriptors[i].getName();
                if (!StringUtils.equals(name, "class")) {
                    params.put(name,
                            propertyUtilsBean.getNestedProperty(obj, name));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return params;
    }

    /**
     * 创建普通表头
     *
     * @param list 表头名称列表
     * @return
     */
    public static TableHeaderMetaData createTableHeader(List<String> list) {
        TableHeaderMetaData headMeta = new TableHeaderMetaData();
        for (String title : list) {
            TableColumn tc = new TableColumn();
            tc.setDisplay(title);
            headMeta.addColumn(tc);
        }
        return headMeta;
    }

    /**
     * 创建普通表头
     *
     * @param titls 表头名称数组
     * @return
     */
    public static TableHeaderMetaData createTableHeader(String[] titls) {
        TableHeaderMetaData headMeta = new TableHeaderMetaData();
        for (String title : titls) {
            TableColumn tc = new TableColumn();
            tc.setDisplay(title);
            headMeta.addColumn(tc);
        }
        return headMeta;
    }

    /**
     * 创建合并表头
     *
     * @param parents  父表头数组
     * @param children 子表头数组
     * @return
     */
    public static TableHeaderMetaData createTableHeader(String[] parents,
                                                        String[][] children) {
        TableHeaderMetaData headMeta = new TableHeaderMetaData();
        TableColumn parentColumn = null;
        TableColumn sonColumn = null;
        for (int i = 0; i < parents.length; i++) {
            parentColumn = new TableColumn();
            parentColumn.setDisplay(parents[i]);
            if (children != null && children[i] != null) {
                for (int j = 0; j < children[i].length; j++) {
                    sonColumn = new TableColumn();
                    sonColumn.setDisplay(children[i][j]);
                    parentColumn.addChild(sonColumn);
                }
            }
            headMeta.addColumn(parentColumn);
        }
        return headMeta;
    }

    /**
     * 拼装数据
     *
     * @param list     数据集
     * @param headMeta 表头
     * @param fields   对象或Map属性数组（注意：顺序要与表头标题顺序对应，如数据集为List<Object[]>，则该参数可以为null）
     * @return TableData
     */
    @SuppressWarnings({"unchecked", "rawtypes", "null"})
    public static TableData createTableData(List list,
                                            TableHeaderMetaData headMeta, String[] fields) {

        TableData td = new TableData(headMeta);
        TableDataRow row = null;
        if (list != null && list.size() > 0) {
            if (list.get(0).getClass().isArray()) {// 数组类型
                for (Object obj : list) {
                    row = new TableDataRow(td);
                    for (Object o : (Object[]) obj) {
                        row.addCell(o);
                    }
                    td.addRow(row);
                }
            } else {// JavaBean或Map类型
                for (Object obj : list) {
                    row = new TableDataRow(td);
                    Map<String, Object> map = (obj instanceof Map) ? (Map<String, Object>) obj
                            : beanToMap(obj);
                    for (String key : fields) {
                        row.addCell(map.get(key));
                    }
                    td.addRow(row);
                }
            }
        }
        return td;
    }

    @SuppressWarnings("deprecation")
    public static void copySheetStyle(HSSFWorkbook destwb, HSSFSheet dest,
                                      HSSFWorkbook srcwb, HSSFSheet src) {
        if (src == null || dest == null)
            return;

        dest.setAlternativeExpression(src.getAlternateExpression());
        dest.setAlternativeFormula(src.getAlternateFormula());
        dest.setAutobreaks(src.getAutobreaks());
        dest.setDialog(src.getDialog());
        if (src.getColumnBreaks() != null) {
            for (int col : src.getColumnBreaks()) {
                dest.setColumnBreak((short) col);
            }
        }
        dest.setDefaultColumnWidth(src.getDefaultColumnWidth());
        dest.setDefaultRowHeight(src.getDefaultRowHeight());
        dest.setDefaultRowHeightInPoints(src.getDefaultRowHeightInPoints());
        dest.setDisplayGuts(src.getDisplayGuts());
        dest.setFitToPage(src.getFitToPage());
        dest.setHorizontallyCenter(src.getHorizontallyCenter());
        dest.setDisplayFormulas(src.isDisplayFormulas());
        dest.setDisplayGridlines(src.isDisplayGridlines());
        dest.setDisplayRowColHeadings(src.isDisplayRowColHeadings());
        dest.setGridsPrinted(src.isGridsPrinted());
        dest.setPrintGridlines(src.isPrintGridlines());

        for (int i = 0; i < src.getNumMergedRegions(); i++) {
            Region r = src.getMergedRegionAt(i);
            dest.addMergedRegion(r);
        }

        if (src.getRowBreaks() != null) {
            for (int row : src.getRowBreaks()) {
                dest.setRowBreak(row);
            }
        }
        dest.setRowSumsBelow(src.getRowSumsBelow());
        dest.setRowSumsRight(src.getRowSumsRight());

        short maxcol = 0;
        for (int i = 0; i <= src.getLastRowNum(); i++) {
            HSSFRow row = src.getRow(i);
            if (row != null) {
                if (maxcol < row.getLastCellNum())
                    maxcol = row.getLastCellNum();
            }
        }
        for (short col = 0; col <= maxcol; col++) {
            if (src.getColumnWidth(col) != src.getDefaultColumnWidth())
                dest.setColumnWidth(col, src.getColumnWidth(col));
            dest.setColumnHidden(col, src.isColumnHidden(col));
        }
    }

    public static String dumpCellStyle(HSSFCellStyle style) {
        StringBuffer sb = new StringBuffer();
        sb.append(style.getHidden()).append(",");
        sb.append(style.getLocked()).append(",");
        sb.append(style.getWrapText()).append(",");
        sb.append(style.getAlignment()).append(",");
        sb.append(style.getBorderBottom()).append(",");
        sb.append(style.getBorderLeft()).append(",");
        sb.append(style.getBorderRight()).append(",");
        sb.append(style.getBorderTop()).append(",");
        sb.append(style.getBottomBorderColor()).append(",");
        sb.append(style.getDataFormat()).append(",");
        sb.append(style.getFillBackgroundColor()).append(",");
        sb.append(style.getFillForegroundColor()).append(",");
        sb.append(style.getFillPattern()).append(",");
        sb.append(style.getIndention()).append(",");
        sb.append(style.getLeftBorderColor()).append(",");
        sb.append(style.getRightBorderColor()).append(",");
        sb.append(style.getRotation()).append(",");
        sb.append(style.getTopBorderColor()).append(",");
        sb.append(style.getVerticalAlignment());

        return sb.toString();
    }

    public static String dumpFont(HSSFFont font) {
        StringBuffer sb = new StringBuffer();
        sb.append(font.getItalic()).append(",").append(font.getStrikeout())
                .append(",").append(font.getBoldweight()).append(",")
                .append(font.getCharSet()).append(",").append(font.getColor())
                .append(",").append(font.getFontHeight()).append(",")
                .append(font.getFontName()).append(",")
                .append(font.getTypeOffset()).append(",")
                .append(font.getUnderline());
        return sb.toString();
    }

    public static void copyCellStyle(HSSFWorkbook destwb, HSSFCell dest,
                                     HSSFWorkbook srcwb, HSSFCell src) {
        if (src == null || dest == null)
            return;

        HSSFCellStyle nstyle = findStyle(src.getCellStyle(), srcwb, destwb);
        if (nstyle == null) {
            nstyle = destwb.createCellStyle();
            copyCellStyle(destwb, nstyle, srcwb, src.getCellStyle());
        }
        dest.setCellStyle(nstyle);
    }

    private static boolean isSameColor(short a, short b, HSSFPalette apalette,
                                       HSSFPalette bpalette) {
        if (a == b)
            return true;
        HSSFColor acolor = apalette.getColor(a);
        HSSFColor bcolor = bpalette.getColor(b);
        if (acolor == null)
            return true;
        if (bcolor == null)
            return false;
        return acolor.getHexString().equals(bcolor.getHexString());
    }

    private static short findColor(short index, HSSFWorkbook srcwb,
                                   HSSFWorkbook destwb) {
        Integer id = new Integer(index);
        if (HSSFColor.getIndexHash().containsKey(id))
            return index;
        if (index == HSSFColor.AUTOMATIC.index)
            return index;
        HSSFColor color = srcwb.getCustomPalette().getColor(index);
        if (color == null) {
            return index;
        }

        HSSFColor ncolor = destwb.getCustomPalette().findColor(
                (byte) color.getTriplet()[0], (byte) color.getTriplet()[1],
                (byte) color.getTriplet()[2]);
        if (ncolor != null)
            return ncolor.getIndex();
        destwb.getCustomPalette().setColorAtIndex(index,
                (byte) color.getTriplet()[0], (byte) color.getTriplet()[1],
                (byte) color.getTriplet()[2]);
        return index;
    }

    public static HSSFCellStyle findStyle(HSSFCellStyle style,
                                          HSSFWorkbook srcwb, HSSFWorkbook destwb) {
        HSSFPalette srcpalette = srcwb.getCustomPalette();
        HSSFPalette destpalette = destwb.getCustomPalette();

        for (short i = 0; i < destwb.getNumCellStyles(); i++) {
            HSSFCellStyle old = destwb.getCellStyleAt(i);
            if (old == null)
                continue;

            if (style.getAlignment() == old.getAlignment()
                    && style.getBorderBottom() == old.getBorderBottom()
                    && style.getBorderLeft() == old.getBorderLeft()
                    && style.getBorderRight() == old.getBorderRight()
                    && style.getBorderTop() == old.getBorderTop()
                    && isSameColor(style.getBottomBorderColor(),
                    old.getBottomBorderColor(), srcpalette, destpalette)
                    && style.getDataFormat() == old.getDataFormat()
                    && isSameColor(style.getFillBackgroundColor(),
                    old.getFillBackgroundColor(), srcpalette,
                    destpalette)
                    && isSameColor(style.getFillForegroundColor(),
                    old.getFillForegroundColor(), srcpalette,
                    destpalette)
                    && style.getFillPattern() == old.getFillPattern()
                    && style.getHidden() == old.getHidden()
                    && style.getIndention() == old.getIndention()
                    && isSameColor(style.getLeftBorderColor(),
                    old.getLeftBorderColor(), srcpalette, destpalette)
                    && style.getLocked() == old.getLocked()
                    && isSameColor(style.getRightBorderColor(),
                    old.getRightBorderColor(), srcpalette, destpalette)
                    && style.getRotation() == old.getRotation()
                    && isSameColor(style.getTopBorderColor(),
                    old.getTopBorderColor(), srcpalette, destpalette)
                    && style.getVerticalAlignment() == old
                    .getVerticalAlignment()
                    && style.getWrapText() == old.getWrapText()) {

                HSSFFont oldfont = destwb.getFontAt(old.getFontIndex());
                HSSFFont font = srcwb.getFontAt(style.getFontIndex());
                if (oldfont.getBoldweight() == font.getBoldweight()
                        && oldfont.getItalic() == font.getItalic()
                        && oldfont.getStrikeout() == font.getStrikeout()
                        && oldfont.getCharSet() == font.getCharSet()
                        && isSameColor(oldfont.getColor(), font.getColor(),
                        srcpalette, destpalette)
                        && oldfont.getFontHeight() == font.getFontHeight()
                        && oldfont.getFontName().equals(font.getFontName())
                        && oldfont.getTypeOffset() == font.getTypeOffset()
                        && oldfont.getUnderline() == font.getUnderline()) {
                    return old;
                }
            }
        }
        return null;
    }

    public static void copyCellStyle(HSSFWorkbook destwb, HSSFCellStyle dest,
                                     HSSFWorkbook srcwb, HSSFCellStyle src) {
        if (src == null || dest == null)
            return;
        dest.setAlignment(src.getAlignment());
        dest.setBorderBottom(src.getBorderBottom());
        dest.setBorderLeft(src.getBorderLeft());
        dest.setBorderRight(src.getBorderRight());
        dest.setBorderTop(src.getBorderTop());
        dest.setBottomBorderColor(findColor(src.getBottomBorderColor(), srcwb,
                destwb));
        dest.setDataFormat(destwb.createDataFormat().getFormat(
                srcwb.createDataFormat().getFormat(src.getDataFormat())));
        dest.setFillPattern(src.getFillPattern());
        dest.setFillForegroundColor(findColor(src.getFillForegroundColor(),
                srcwb, destwb));
        dest.setFillBackgroundColor(findColor(src.getFillBackgroundColor(),
                srcwb, destwb));
        dest.setHidden(src.getHidden());
        dest.setIndention(src.getIndention());
        dest.setLeftBorderColor(findColor(src.getLeftBorderColor(), srcwb,
                destwb));
        dest.setLocked(src.getLocked());
        dest.setRightBorderColor(findColor(src.getRightBorderColor(), srcwb,
                destwb));
        dest.setRotation(src.getRotation());
        dest.setTopBorderColor(findColor(src.getTopBorderColor(), srcwb, destwb));
        dest.setVerticalAlignment(src.getVerticalAlignment());
        dest.setWrapText(src.getWrapText());

        HSSFFont f = srcwb.getFontAt(src.getFontIndex());
        HSSFFont nf = findFont(f, srcwb, destwb);
        if (nf == null) {
            nf = destwb.createFont();
            nf.setBoldweight(f.getBoldweight());
            nf.setCharSet(f.getCharSet());
            nf.setColor(findColor(f.getColor(), srcwb, destwb));
            nf.setFontHeight(f.getFontHeight());
            nf.setFontHeightInPoints(f.getFontHeightInPoints());
            nf.setFontName(f.getFontName());
            nf.setItalic(f.getItalic());
            nf.setStrikeout(f.getStrikeout());
            nf.setTypeOffset(f.getTypeOffset());
            nf.setUnderline(f.getUnderline());
        }
        dest.setFont(nf);
    }

    private static HSSFFont findFont(HSSFFont font, HSSFWorkbook src,
                                     HSSFWorkbook dest) {
        for (short i = 0; i < dest.getNumberOfFonts(); i++) {
            HSSFFont oldfont = dest.getFontAt(i);
            if (font.getBoldweight() == oldfont.getBoldweight()
                    && font.getItalic() == oldfont.getItalic()
                    && font.getStrikeout() == oldfont.getStrikeout()
                    && font.getCharSet() == oldfont.getCharSet()
                    && font.getColor() == oldfont.getColor()
                    && font.getFontHeight() == oldfont.getFontHeight()
                    && font.getFontName().equals(oldfont.getFontName())
                    && font.getTypeOffset() == oldfont.getTypeOffset()
                    && font.getUnderline() == oldfont.getUnderline()) {
                return oldfont;
            }
        }
        return null;
    }

    public static void copySheet(HSSFWorkbook destwb, HSSFSheet dest,
                                 HSSFWorkbook srcwb, HSSFSheet src) {
        if (src == null || dest == null)
            return;

        copySheetStyle(destwb, dest, srcwb, src);

        for (int i = 0; i <= src.getLastRowNum(); i++) {
            HSSFRow row = src.getRow(i);
            copyRow(destwb, dest.createRow(i), srcwb, row);
        }
    }

    @SuppressWarnings("deprecation")
    public static void copyRow(HSSFWorkbook destwb, HSSFRow dest,
                               HSSFWorkbook srcwb, HSSFRow src) {
        if (src == null || dest == null)
            return;
        for (short i = 0; i <= src.getLastCellNum(); i++) {
            if (src.getCell(i) != null) {
                HSSFCell cell = dest.createCell(i);
                copyCell(destwb, cell, srcwb, src.getCell(i));
            }
        }

    }

    public static void copyCell(HSSFWorkbook destwb, HSSFCell dest,
                                HSSFWorkbook srcwb, HSSFCell src) {
        if (src == null) {
            dest.setCellType(HSSFCell.CELL_TYPE_BLANK);
            return;
        }

        if (src.getCellComment() != null)
            dest.setCellComment(src.getCellComment());
        if (src.getCellStyle() != null) {
            HSSFCellStyle nstyle = findStyle(src.getCellStyle(), srcwb, destwb);
            if (nstyle == null) {
                nstyle = destwb.createCellStyle();
                copyCellStyle(destwb, nstyle, srcwb, src.getCellStyle());
            }
            dest.setCellStyle(nstyle);
        }
        dest.setCellType(src.getCellType());

        switch (src.getCellType()) {
            case HSSFCell.CELL_TYPE_BLANK:

                break;
            case HSSFCell.CELL_TYPE_BOOLEAN:
                dest.setCellValue(src.getBooleanCellValue());
                break;
            case HSSFCell.CELL_TYPE_FORMULA:
                dest.setCellFormula(src.getCellFormula());
                break;
            case HSSFCell.CELL_TYPE_ERROR:
                dest.setCellErrorValue(src.getErrorCellValue());
                break;
            case HSSFCell.CELL_TYPE_NUMERIC:
                dest.setCellValue(src.getNumericCellValue());
                break;
            default:
                dest.setCellValue(new HSSFRichTextString(src
                        .getRichStringCellValue().getString()));
                break;
        }
    }
}
