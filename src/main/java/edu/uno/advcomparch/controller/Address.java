package edu.uno.advcomparch.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    private String tag;
    private String index;
    private String offset;

    public int getTagDecimal() {
        return Integer.parseInt(tag, 2);
    }

    public int getIndexDecimal() {
        return Integer.parseInt(index, 2);
    }

    public int getOffsetDecimal() {
        return Integer.parseInt(offset, 2);
    }

    public void incrementOffset() {

        Integer offsetDecimal = getOffsetDecimal();
        String s = Integer.toBinaryString(++offsetDecimal);

        //why can't I use guava?
        this.offset = String.format("%1$" + offset.length() + "s", s).replace(' ', '0');

    }
}
