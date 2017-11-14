/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servise;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author 1
 */
public class ThreadBlock {

    private LinkedBlockingDeque blockingQueue;

    public ThreadBlock() {

        blockingQueue = new LinkedBlockingDeque();
    }


    /**
     * Ждать  пока какой нибуть поток не выполнит функцию resume()
     * @param milisec -ожидание в миллисекундах
     * @return
     */
    public boolean wait(int milisec) {
        try {
            blockingQueue.clear();

            Object o = blockingQueue.poll(milisec, TimeUnit.MILLISECONDS);

            if (o != null) {

                return true;
            } else {

                return false;
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return false;

    }

    /**
     * Приостановить выполнение потока
     */
    public void suspend() {

        blockingQueue.clear();
        try {
            blockingQueue.take();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }


    /**
     * Продолжить выполнение потока
     */
    public void resume() {
        blockingQueue.add(1);
    }

}
