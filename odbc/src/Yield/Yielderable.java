/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Yield;

import java.util.Optional;

/**
 *
 * @author D882758
 * @param <T>
 */
public interface Yielderable<T> extends Iterable<T> {

    void execute(YieldDefinition<T> builder);

    @Override
    default ClosableIterator<T> iterator() {
        YieldDefinition<T> yieldDefinition = new YieldDefinition<>();
        Thread collectorThread = new Thread(() -> {
            yieldDefinition.waitUntilFirstValueRequested();
            try {
                execute(yieldDefinition);
            } catch (BreakException e) {
            }
            yieldDefinition.signalComplete();
        });
        collectorThread.setDaemon(true);
        collectorThread.start();
        yieldDefinition.onClose(collectorThread::interrupt);
        return yieldDefinition.iterator();
    }
}


interface Message<T> {
    Optional<T> value();
    static <T> Message<T> message(T value) {
        return () -> Optional.of(value);
    }
}

interface Completed<T> extends Message<T> {
    static <T> Completed<T> completed() { return () -> Optional.empty(); }
}

interface FlowControl {
    static FlowControl youMayProceed = new FlowControl() {};
}

class BreakException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	@Override
    public synchronized Throwable fillInStackTrace() {
        return null;
    }
}

interface Then<T> {
    void then(Runnable r);
}
class IfAbsent {
    public static <T> Then<T> ifAbsent(Optional<T> optional) {
        return runnable -> {
            if (!optional.isPresent()) runnable.run();
        };
    }
}
