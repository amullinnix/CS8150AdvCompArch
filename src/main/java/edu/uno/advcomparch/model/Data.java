package edu.uno.advcomparch.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Generic Representation of Data;
 * @param <T>
 */
@lombok.Data
@AllArgsConstructor
@NoArgsConstructor
public class Data<T> {

    private T data;

}
