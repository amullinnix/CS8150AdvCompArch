package edu.uno.advcomparch.repository;

import edu.uno.advcomparch.model.Data;

public interface DataRepository {

    Data<?> get();

    Data<?> victimize();

    void write(Data<?> data);
}
