// Ethan Dingle
// Operating Systems Section W02
// Assignment: Mini Project

import java.util.concurrent.Semaphore;
import java.util.Scanner;

public class OSMiniProject {
    private static long duration; // will store the simulation time
	private static long beginning; // will store the beginning time of simulation

    public static void main(String[] args) {
		// Requesting and storing simulation data
        Scanner userInput = new Scanner(System.in);
        System.out.print("\nConsumer Delay (ms): ");
        int consDelay = userInput.nextInt();
        System.out.print("Producer Delay (ms): ");
        int prodDelay = userInput.nextInt();
        System.out.print("Simulation Time (ms): ");
        duration = userInput.nextLong();
		System.out.print("Buffer Size: ");
        int sizeOfBuffer = userInput.nextInt();
        System.out.println();
		
		// creating buffers and threads
        SharedBuffer buffer = new SharedBuffer(sizeOfBuffer);
        Thread producerThread = new Thread(new Producer(buffer, prodDelay), "Producer");
        Thread consumerThread = new Thread(new Consumer(buffer, consDelay), "Consumer");
		
		 // stores the time at the start of the simulation
		beginning = System.currentTimeMillis();
		
		// starting threads
        producerThread.start();
        consumerThread.start();
		
        try {
			 // sets the main thread to sleep for the requested duration
			Thread.sleep(duration);
			producerThread.interrupt();
            consumerThread.interrupt();
			producerThread.join();
			consumerThread.join();
        } catch (Exception e) { // catching exception
            System.out.println(e.getMessage());
        }
		
		// printing out final total times
        System.out.println("Total waiting time for producers: " + buffer.producerWaitingTime + " ms");
        System.out.println("Total waiting time for consumers: " + buffer.consumerWaitingTime + " ms");
    }
//------------------------------------------------------------------------------------------------------------------------------------------------------
    static class SharedBuffer {
        private char[] buffer;
        private Semaphore mutex = new Semaphore(1); // mutual exclusion
        private Semaphore empty; // tracking empty slots
        private Semaphore full = new Semaphore(0); // tracking filled slots
        public long producerWaitingTime = 0; // total producer waiting time
        public long consumerWaitingTime = 0; // total consumer waiting time

		public SharedBuffer(int sz){
			buffer = new char[sz]; // initializing buffer
			for (int i=0;i<sz;i++) buffer[i]='0'; // fills buffer with '0'
			empty = new Semaphore(sz); // Initialize empty semaphore with buffer size
		}
        public void produce(char value) throws InterruptedException {
			// start time equals current system time
            long startWaitingTime = System.currentTimeMillis();
			// wait for an empty slot
            empty.acquire();
			// calculates the current wait based on the starting time and system current time
            long currentWait = System.currentTimeMillis() - startWaitingTime;
			// adds current wait to the total waiting time of producers
            producerWaitingTime += currentWait; 
			 // mutual exclusion
            mutex.acquire();
			// adds value to the buffer after finding the closest open position
            buffer[indexOfOpenPos()] = value; 
			// display how long producer waited
            System.out.println("["+(System.currentTimeMillis()-beginning)+"ms] After waiting " + currentWait + "ms, Producer produced: " + value); 
			// increment filled slot count
            full.release(); 
			// release mutual exclusion semaphore
            mutex.release(); 
        }
        public char consume() throws InterruptedException {
			// gets start time based on the system time
            long startWaitingTime = System.currentTimeMillis(); 
			// Wait for a filled slot
            full.acquire(); 
			// calculates the current waiting time based on the system current time and starting time;
            long currentWait = System.currentTimeMillis() - startWaitingTime; 
			// increases total consumer waiting time by the current wait amount
            consumerWaitingTime += currentWait; 
			// mutual exclusion
            mutex.acquire(); 
			// stores the value that is beig removed from the buffer
			char value = buffer[indexOfRemovedPos()]; 
			// removes the value and replaces it with a 0
            buffer[indexOfRemovedPos()]='0'; 
			// display how long consumer waited
            System.out.println("["+(System.currentTimeMillis()-beginning)+"ms] After waiting " + currentWait + "ms, Consumer is consuming an item..."); 
			// increment empty slot count
            empty.release(); 
			// release mutual exclusion semaphore
            mutex.release(); 
			// returns the item that got consumed
            return value; 
        }
		public int indexOfOpenPos(){// finds the next position in the buffer that is empty so it can be filled by the producer
			for (int i=0;i<buffer.length;i++) if(buffer[i]=='0') return i; 
			return -1; // return -1 if buffer is full
		}
		public int indexOfRemovedPos(){//finds the next char in the buffer that can be consumed
			for (int i=0;i<buffer.length;i++) if(buffer[i]!='0') return i; 
			return -1; // return -1 if buffer is empty somehow
		}
    }
//------------------------------------------------------------------------------------------------------------------------------------------------------
    static class Producer implements Runnable {
        private SharedBuffer buffer;
        private int delay;

        public Producer(SharedBuffer buffer, int del) {
            this.buffer = buffer; // stores buffer
            delay = del; // sets delay
        }

        @Override
        public void run() {
            try { // while the current time running is less than the requested duration, it'll run
                while (System.currentTimeMillis() - beginning < duration) {
                    char value = (char) ('A' + Math.random() * 26); // makes a random character from A-Z to put in the buffer
                    buffer.produce(value); // adds chosen value to the array
                    Thread.sleep(this.delay); // sleeps for the requested delay length
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // interrupts the current thread if theres an exception
            }
        }
    }
//------------------------------------------------------------------------------------------------------------------------------------------------------
    static class Consumer implements Runnable {
        private SharedBuffer buffer;
        private int delay;

        public Consumer(SharedBuffer buffer, int del) {
            this.buffer = buffer; // stores buffer
            delay = del; // sets delay
        }

        @Override
        public void run() {
            try { // while the current time running is less than the requested duration, it'll run
                while (System.currentTimeMillis() - beginning < duration) {
                    char value = buffer.consume(); // consumes current value
                    System.out.println("["+(System.currentTimeMillis()-beginning)+"ms] Consumer consumed item: " + value); // prints out when its done
                    Thread.sleep(delay); // sleep for specified delay time
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // interrupts the current thread if theres an exception
            }
        }
    }
}