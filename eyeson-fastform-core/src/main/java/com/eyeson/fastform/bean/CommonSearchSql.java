package com.eyeson.fastform.bean;

public class CommonSearchSql {
    private String sql;
    private Integer keyProperty;

    public Integer getKeyProperty() {
        return keyProperty;
    }

    public void setKeyProperty(Integer keyProperty) {
        this.keyProperty = keyProperty;
    }

    public CommonSearchSql(String sql) {
        // TODO Auto-generated constructor stub
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
