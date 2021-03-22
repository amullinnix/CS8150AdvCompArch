package edu.uno.advcomparch.repository;

import edu.uno.advcomparch.model.Data;
import lombok.NoArgsConstructor;

// We could provide default implementations here or make this an abstract class.
@NoArgsConstructor
@lombok.Data
public class DataRepository<T,A> {

    public DataResponse get(A address) {
        throw new UnsupportedOperationException("Get - Unsupported Operation");
    }

    public Data<T> victimize(A address) {
        throw new UnsupportedOperationException("Victimize - Unsupported Operation");
    }

    public void write(Data<?> data) {
        throw new UnsupportedOperationException("Write - Unsupported Operation");
    }
}
