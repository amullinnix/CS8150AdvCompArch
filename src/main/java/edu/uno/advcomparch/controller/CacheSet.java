package edu.uno.advcomparch.controller;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

//I'm feeling like this class is kind of useless atm. We'll see.
@Data
public class CacheSet {

    private List<CacheBlock> blocks;

    public CacheSet() {
        this.blocks = new ArrayList<>();
    }

    public void add(CacheBlock block) {
        blocks.add(block);
    }
}
