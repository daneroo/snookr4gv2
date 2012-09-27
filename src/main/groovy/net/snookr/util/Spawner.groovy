package net.snookr.util;

import net.snookr.util.Progress;
import groovy.lang.Closure;

class Spawner {
    List workers = [];
    List list
    Closure action
    Progress progress
    Spawner(List aList,Closure anAction,int numThreads) {
        // make a copy of the list 
        this.list = []; list.addAll(aList)

        // track progress
        progress = new Progress(list.size());
        this.action = { anAction(it);  progress.increment();  }

        // create numThreads workers
        (1..numThreads).each() { workers.add(new Worker(this))  }

    }

    // maybe the list itself should be synchronized
    synchronized Object getNext() {
        return list.remove(0);
    }

    public void run() { // spawn the threads, and then wait for them to finish
        workers.each() { it.start() } 
        workers.each() { it.join()  } 
        int totalCount  = workers.sum() { it.count }
        println "Exited Last thread -- Processed ${totalCount} -- "
    }

}

class Worker implements Runnable {
    static int workerCounter=0;
    Thread thisThread;
    Spawner spawner;
    int count=0;
    Worker(Spawner spawner) {
        this.spawner = spawner;
        thisThread = new Thread(this,"Worker ${++workerCounter}");
    }
    public void start() { thisThread.start() }
    public void join() { thisThread.join() }
    public void run() {
        //getNext from parent
        def n;
        try {
            while ( (n=spawner.getNext())) {
                count++;
                spawner.action(n);
            }
        } catch (IndexOutOfBoundsException iobe) {
            //println "Exiting ${Thread.currentThread().getName()} -- Processed ${count} -- "
        }
    }
}
