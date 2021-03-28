package edu.uno.advcomparch.controller;

import lombok.Data;

@Data
public class CacheBlock {

    //Beginning to wonder if there is really any reason for tag to be byte[]
    private Address address;
//    private byte[] tag;  //not ready to delete yet. We'll see.
    private byte[] block;

    //TODO: When, precisely, do you set the dirty bit?
        //For starters, it's always set on write
    //TODO: So, then, when do you reset the dirty bit?
        //I'm thinking that it might be on eviction
        //Also, clearly set to clean when block is created
    private boolean dirty;

    //TODO: When, precisely, do you set the valid bit?
        //Valid bit is false upon initialization
        //When valid data is put in there, then it's set to true, it's that easy!
    private boolean valid;

    public CacheBlock(int tagSize, int blockSize) {
//        tag = new byte[tagSize];
        block = new byte[blockSize];
        dirty = false;
        valid = false;
        address = new Address("", "", "");
    }

    //Foiled by Lombok. Rookie mistake. ;)
    public void setAddress(Address address) {
        this.address = new Address(address.getTag(), address.getIndex(), address.getOffset());
        // TODO - Seems odd to not set the full address intially if we have to run compartmentalize;
        //this.address.setAddress(address.getAddress());
        // TODO - Purpose was you could just call getAddress() Talk to Drew about this ;)
    }

    //Define isEmpty as both tag and block are empty
    public boolean isEmpty() {

        if(byteArrayHasAnyValues(address.getTag().getBytes()) && byteArrayHasAnyValues(block)) {
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

    public byte[] getTag() {
        return this.address.getTag().getBytes();
    }

    public String getTagString() {
        return address.getTag();
    }

    public boolean isNotDirty() {
        return ! this.dirty;
    }


    /**
     * A note on Dirty Bits:
     *
     * The dirty bit is supposed to be
     *      * true(dirty) if the data in the cache block has been modified
     *      * false(clean) if the data has not been modified
     *
     * If clean, then there is no need to write it into the main memory
     *
     */
}
