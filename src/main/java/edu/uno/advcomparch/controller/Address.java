package edu.uno.advcomparch.controller;

import lombok.Data;

//TODO: It occurs to me that we might not need this. Are all our addresses just going to be "A", "B", etc? Or will we
// actually have to compute these like we did in the first few lectures?
@Data
public class Address {

    private String tag;
    private String index;
    private String offset;
}
