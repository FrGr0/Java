/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Yield;

/**
 *
 * @author D882758
 */
import java.util.Iterator;

public interface ClosableIterator<T> extends Iterator<T>, AutoCloseable {
    @Override
    void close();
}