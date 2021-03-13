package edu.uno.advcomparch.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    //TODO: I keep feeling like the Address just needs to have the "17 bit" representation, and then derive the
    // tag, index, and offset. Namely because tag is different sizes for level 1 and level 2.

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
