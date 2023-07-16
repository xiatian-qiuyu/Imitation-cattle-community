package com.nowcoder.community;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 阻塞队列
 */
public class BlockQueueTest {
    public static void main(String[] args) {
        ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<>(10);
        new Thread(new Prodecer(queue)).start();
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();
    }
}

//Prodecer实现了Runnable接口，可以作为线程任务，生产者
class Prodecer implements Runnable{
    //BlockingQueue的作是阻塞队列，当队列满了，生产者会被阻塞，当队列空了，消费者会被阻塞
    private BlockingQueue<Integer> queue;

    public Prodecer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try{
            for(int i=0;i<100;i++){
                Thread.sleep(20);
                //put()方法是阻塞方法，当队列满了，生产者会被阻塞，未满的put()方法会将元素放入队列
                queue.put(i);
                System.out.println(Thread.currentThread().getName() +"生产"+queue.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

//消费者
class Consumer implements Runnable{
    private BlockingQueue<Integer> queue;
    public Consumer(BlockingQueue<Integer> queue){
        this.queue=queue;
    }
    @Override
    public void run() {
        try{
            while (true){
                Thread.sleep(new Random().nextInt(1000));
                //take()方法是阻塞方法，当队列空了，消费者会被阻塞，未空的take()方法会将元素从队列中取出
                queue.take();
                System.out.println(Thread.currentThread().getName()+"消费："+queue.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

//上面这个程序模拟了阻塞队列的使用，生产者生产数据，消费者消费数据，当队列满了，生产者会被阻塞，当队列空了，消费者会被阻塞