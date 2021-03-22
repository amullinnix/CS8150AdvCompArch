package edu.uno.advcomparch.cpu;

/**
 * CPU Interface used to stub the interactions between the CPU and the L1 Controller
 */
public interface CentralProcessingUnit<T> {

    void read();

    void write();
    
    void data(T data);
}
