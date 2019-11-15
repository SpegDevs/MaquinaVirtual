package com.uca;

public class Data<DataType> {

    private DataType value;

    public Data(DataType value){
        this.value = value;
    }

    public DataType getValue() {
        return value;
    }

    public void setValue(DataType value) {
        this.value = value;
    }
}
