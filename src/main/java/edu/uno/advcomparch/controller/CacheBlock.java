package edu.uno.advcomparch.controller;

import lombok.Data;

@Data
public class CacheBlock {

    //Beginning to wonder if there is really any reason for tag to be byte[]
    private byte[] tag;
    private byte[] block;

    public CacheBlock(int tagSize, int blockSize) {
        tag = new byte[tagSize];
        block = new byte[blockSize];
    }

    //Define isEmpty as both tag and block are empty
    public boolean isEmpty() {

        if(byteArrayHasAnyValues(tag) && byteArrayHasAnyValues(block)) {
            return false;
        }

        return true;
    }

    private boolean byteArrayHasAnyValues(byte[] array) {

        for( int i = 0; i < array.length; i++ ) {
            if( array[i] != 0 ) {
                return true;
            }
        }

        return false;
    }
}
