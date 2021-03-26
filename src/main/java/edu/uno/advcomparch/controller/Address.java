package edu.uno.advcomparch.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.ObjectUtils;

import java.nio.channels.UnsupportedAddressTypeException;
import java.util.Objects;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
public class Address {

    private String address;

    private String tag;
    private String index;
    private String offset;

    public Address(String tag, String index, String offset) {
        this.tag = tag;
        this.index = index;
        this.offset = offset;
    }

    public String getAddress() {
        if(Stream.of(tag, index, offset).allMatch(Objects::isNull)) {
            return address;
        }

        return tag + index + offset;
    }

    public void setAddress(String address) {
        //basically, cannot set address if tag/index/offset already been set
        if(Stream.of(tag, index, offset).allMatch(Objects::nonNull)){
            throw new UnsupportedAddressTypeException();
        }

        this.address = address;
    }

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

    //The idea behind this method is that the controller knows it's address space and thus given a "full" address,
    //it will be able to split it into its appropriate parts. So, controllers should use this.
    public void componentize(int tagSize, int indexSize, int offsetSize) {

        int length = this.address.length();

        this.offset = this.address.substring(length - offsetSize);
        this.index = this.address.substring((length - offsetSize - indexSize), (length - offsetSize));
        this.tag = this.address.substring((length - offsetSize - indexSize - tagSize), (length - offsetSize - indexSize));

    }
}
