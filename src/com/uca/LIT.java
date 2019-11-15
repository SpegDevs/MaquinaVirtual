package com.uca;

public class LIT<DataType> extends PInstruction{

    private DataType value;

    public LIT(DataType value) {
        this.value = value;
    }

    public DataType getValue() {
        return value;
    }

    public void setValue(DataType value) {
        this.value = value;
    }
}
