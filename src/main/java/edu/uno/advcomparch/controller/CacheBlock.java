package edu.uno.advcomparch.controller;

import lombok.Data;

@Data
public class CacheBlock {

    //Beginning to wonder if there is really any reason for tag to be byte[]
    private byte[] tag;
    private byte[] block;

    //TODO: When, precisely, do you set the dirty bit?
    private boolean dirty;

    //TODO: When, precisely, do you set the valid bit?
    private boolean valid;

    public CacheBlock(int tagSize, int blockSize) {
        tag = new byte[tagSize];
        block = new byte[blockSize];
        dirty = false;
    }

    //Define isEmpty as both tag and block are empty
    public boolean isEmpty() {

        if(byteArrayHasAnyValues(tag) && byteArrayHasAnyValues(block)) {
            return false;
        }

        return true;
    }

    //TODO: A bug? Zero is a valid value to put in our byte array. Think about that :/
    private boolean byteArrayHasAnyValues(byte[] array) {

        for( int i = 0; i < array.length; i++ ) {
            if( array[i] != 0 ) {
                return true;
            }
        }

        return false;
    }

    public String getTagString() {
        return new String(tag);
    }

    public boolean isNotDirty() {
        return ! this.dirty;
    }

}
