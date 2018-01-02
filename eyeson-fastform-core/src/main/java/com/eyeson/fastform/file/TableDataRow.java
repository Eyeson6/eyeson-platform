package com.eyeson.fastform.file;

import com.eyeson.fastform.common.TableData;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * 版权：融贯资讯 <br/>
 * 作者：zhengbin.jin@rogrand.com <br/>
 * 生成日期：2016年3月10日 <br/>
 * 描述：〈描述〉
 */
public class TableDataRow {
    private LinkedList<TableDataCell> cells;

    private TableData table;

    private int rowStyle = TableData.STYLE_TYPE_STRING;

    public void addCell(TableDataCell cell) {
        cells.add(cell);
    }

    public void addCell(String value) {
        TableDataCell cell = new TableDataCell(this);
        cell.setValue(value);
        cell.setCellStyle(rowStyle);
        addCell(cell);
    }

    public void addCell(Integer value) {
        TableDataCell cell = new TableDataCell(this);
        cell.setValue(value);
        cell.setCellStyle(rowStyle);
        addCell(cell);
    }

    public void addCell(Double value) {
        TableDataCell cell = new TableDataCell(this);
        cell.setValue(value);
        cell.setCellStyle(rowStyle);
        addCell(cell);
    }

    public void addCell(Object value) {
        if (value instanceof String) {
            addCell((String) value);
        } else if (value instanceof Integer) {
            addCell(value.toString());
        } else if (value instanceof Double) {
            addCell(value.toString());
        } else if (value instanceof BigDecimal) {
            addCell(value.toString());
        } else if (value instanceof Long) {
            addCell(value.toString());
        } else if (value == null) {
            addCell("");
        } else if (value instanceof Date) {
            SimpleDateFormat format = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            addCell(format.format(value));
        } else {
            addCell(value.toString());
        }
    }

    public TableDataCell getCellAt(int index) {
        return cells.get(index);
    }

    public List<TableDataCell> getCells() {
        return cells;
    }

    public TableData getTable() {
        return table;
    }

    public TableDataRow(TableData table) {
        cells = new LinkedList<TableDataCell>();
        this.table = table;
    }

    public void setRowStyle(int rowStyle) {
        this.rowStyle = rowStyle;
    }

    public int getRowStyle() {
        return rowStyle;
    }
}
