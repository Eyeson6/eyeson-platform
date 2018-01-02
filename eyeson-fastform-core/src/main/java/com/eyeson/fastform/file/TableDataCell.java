package com.eyeson.fastform.file;

import com.eyeson.fastform.common.TableData;

import java.text.DecimalFormat;

/**
 * 版权：融贯资讯 <br/>
 * 作者：zhengbin.jin@rogrand.com <br/>
 * 生成日期：2016年3月10日 <br/>
 * 描述：〈描述〉
 */
public class TableDataCell {
    private String value;

    private int intValue;

    private double doubleValue;

    private TableDataRow row;

    private int columnIndex;

    private static DecimalFormat format2 = new DecimalFormat("0.##");

    private static DecimalFormat format3 = new DecimalFormat("0.###");

    private int cellStyle = TableData.STYLE_TYPE_STRING;

    public TableDataCell(TableDataRow row) {
        this(-1, row);
    }

    public TableDataCell(int index, TableDataRow row) {
        this.row = row;
        if (index == -1) {
            index = row.getCells().size();
        } else {
            this.columnIndex = index;
        }
        value = "";
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setValue(int value) {
        this.value = String.valueOf(value);
        this.intValue = value;
    }

    public void setValue(double value) {
        int type = row.getTable().getTableHeader().getColumnAt(columnIndex)
                .getColumnType();
        if (type == TableColumn.COLUMN_TYPE_FLOAT_2) {
            this.value = format2.format(value);
        } else if (type == TableColumn.COLUMN_TYPE_FLOAT_3) {
            this.value = format3.format(value);
        }
        this.doubleValue = value;
    }

    public String getValue() {
        return value;
    }

    public TableDataRow getRow() {
        return row;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public int getIntValue() {
        return intValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public void setCellStyle(int cellStyle) {
        this.cellStyle = cellStyle;
    }

    public int getCellStyle() {
        return cellStyle;
    }
}
