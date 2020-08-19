package edu.metrostate.SurajPai.pa5;

import java.util.Arrays;
import java.util.LinkedList;

/****************************************************************************************************************
 * This class routes the packages from their source to their destination. 
 * Suraj Pai 
 */
public class Router implements Runnable {
	private LinkedList<Packet> list = new LinkedList<Packet>();
	private int[] routes;
	private Router[] routers;
	private int routerNum;
	private boolean end = false;
	private boolean isInNetwork;

	public Router(int[] routes, Router[] routers, int routerNum) {
		this.routes = routes;
		this.routers = routers;
		this.routerNum = routerNum;
	}
	/*
	 * We need exclusive access to the queue so we synchronize it. 
     * Then we add the packet ot the queue and set the flag to true. 
     * The flag means that there are stil packets in the network. 
     * Finally we notify all threads that are possibly waitin got let 
     * them know that the state of the queue has changed. 
	 */
	public void addWork(Packet packet) {
		synchronized (this) {		
			list.add(packet);	
			isInNetwork = true;
			this.notifyAll();
		}
	}
	/*
	 * While there are packets still in the netowrk, we should be waiting for
     * the task to complete. 
     * The isInNetwork boolean is used to check if there are packets in the 
     * network. 
     * Once there aren't any packets left in the network we set the end flag
     * to true to signal that it's over. 
     * Lastly, we notify all threads that are waiting for this to let them 
     * know the state has changed. 
	 */
	public synchronized void end() {
		while (isInNetwork) {
			try {			
				this.wait();
            }	
			catch (InterruptedException ex) {			
				ex.printStackTrace();
			}
		}	
		end = true;
		this.notifyAll();
	}
    /*
	 * This sets the flag isInNetwork to false when we realize the network is empty. 
     * It also notifies all threads who may be waiting for this flag. 
	 */
	public synchronized void networkEmpty() {		
		isInNetwork = false;
		this.notifyAll();	
    }
    /*
	 * This is where the packets are processed. 
     * We loop through while we haven't reached the end and synchronize the queue. 
     * The inner loop is broken out of when the end condition is met. 
     * The outer loop is broken out of if the queue is empty and we have reached the end. 
     * We make sure to record the start and end of the packets. 
     * Finally, we send the packet to the router and keep doing this until we reach the 
     * destination router. 
     * The path is noted down through every move from one router to the next. 
	 */
	@Override
	public void run() {		
		while (!end) {	
			Packet packet = null;
            int destination;	
            int source; 
			synchronized (this) {
				while (list.isEmpty()) {		
					try {		
						this.wait();
                    }	
					catch (InterruptedException ex) {	
						ex.printStackTrace();
					}	
					if (end) {			
						break;
					}
				}		
				packet = list.poll();	
				this.notifyAll();
			}		
			if (end && list.isEmpty()) {		
				break;
            }		
            if (packet != null)
            {
                source = packet.getSource();
                destination = packet.getDestination();
                packet.Record(routerNum);	
			    if (routerNum != destination && destination != source) {
				    int route = routes[destination];
				    routers[route].addWork(packet);
			    }
			    else {	
				    Routing.decPacketCount();
			    }
            }
            else 
            {
                System.out.println("Packet is null, please check input data.");
            }
			
		}
	}
}