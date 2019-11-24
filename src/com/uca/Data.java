package com.uca;

public class Data<DataType> {

    private DataType value;
    private Type type;

    public Data(DataType value, Type type){
        this.value = value;
        this.type = type;
    }

    public DataType getValue() {
        return value;
    }

    public void setValue(DataType value) {
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}