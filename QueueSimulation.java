import java.util.Random;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
public class QueueSimulation{
	
	//declare global vars for the system
	private static final int Q_LIMIT  = 1000;
	private static final int BUSY = 1;
	private static final int IDLE = 0;
	private Random random;

	private int next_event_type, num_cust_delayed, num_delays_required,
			num_events, num_in_q, server_status;
	private double area_num_in_q, area_server_status, mean_interarrival,
			mean_service, time, time_last_event, total_of_delays;
	private double time_arrival[] = new double[Q_LIMIT + 1];
	private double time_next_event[] = new double[3];

	private Path inFile = Paths.get("infile.txt");
	private Path outFile = Paths.get("outfile.txt");

	public QueueSimulation(){

	 	random = new Random(1);
	 	//specify number of events for the timing function
		num_events = 2;

		//initialize input params
		try{
			Scanner scanner = new Scanner(inFile, StandardCharsets.UTF_8.name());
			mean_interarrival = Double.valueOf(scanner.nextLine());
			mean_service = Double.valueOf(scanner.nextLine());
			num_delays_required = Integer.valueOf(scanner.nextLine());
		}catch(Exception e){
			e.printStackTrace();
		}

		//initialize the simulation function
		time = 0.0;

		//initialize the state vars
		server_status = IDLE;
		num_in_q = 0;
		time_last_event = 0.0;

		//initialize the statistical counter
		num_cust_delayed = 0;
		total_of_delays = 0.0;
		area_num_in_q = 0.0;
		area_server_status = 0.0;

		/* initialize events list. since no customers are present
		* , the departure (service completion) event is eliminated
		* from consideration
		*/
		time_next_event[1] = time + expon(mean_interarrival);
		time_next_event[2] = Double.POSITIVE_INFINITY;
	}

	public void timing(){
		int i;
		double min_time_next_event = Double.POSITIVE_INFINITY;
		/* Determine the event type of the next event to occur. */
		for (i = 1; i <=num_events; ++i) {
			if (time_next_event[i] < min_time_next_event){
				min_time_next_event = time_next_event[i];
				next_event_type = i;
			}
		}
		/* Check to see whether the event list is empty. */
		if (next_event_type == 0) {
			/* The event list is empty, so stop the simulation. */
			System.out.println("\nEvent list empty at time" + String
				.valueOf(time));
			System.exit(1);
		}
		/* The event list is not empty, so advance the simulation clock
		*/
		time = min_time_next_event;
	}

	public void arrive(){
		double delay;
		/* Schedule next arrival. */
		time_next_event[1] = time + expon(mean_interarrival);
		/* Check to see whether server is busy. */
		if (server_status == BUSY) {
			/* Server is busy, so increment number of customers in queue.
			*/
			++num_in_q;
			/* Check to see whether an overflow condition exists. */
			if (num_in_q > Q_LIMIT){
				/* The queue has overflowed, so stop the simulation. */
				System.out.println("\nOverflow of the array time_arrival at");
				System.out.println(" time : " + time);
				System.exit(2) ;
			}
			/* There is still room in the queue, so store the time of
			arrival of the arriving customer at the (new) _end of
			time_arrival. */
			time_arrival[num_in_q] = time;
		}else{
			/* Server is idle, so arriving customer has a delay of zero. (The following two statements are for program clarity and do
			not affect the results of the simulation.) */
			delay = 0.0;
			total_of_delays += delay;
			/* Increment the number of customers delayed, and make server busy. _*/
			++num_cust_delayed;
			server_status = BUSY;
			/* Schedule a departure (service completion). */
			time_next_event[2] = time + expon(mean_service); 
		}

	}

	public void depart(){
		int i; 
		double delay;

		//check to see whether the queue is empty
		if (num_in_q == 0) {
			/* The queue is empty so make the server idle and eliminate the
			departure (service completion) event from consideration. */
			server_status = IDLE;
			time_next_event[2] = Double.POSITIVE_INFINITY; 
		}else{
			/* The queue is nonempty, so decrement the nUmber of customers
			in queue. */
			--num_in_q;
			/* Compute the delay of the customer who is beginning service
			and update the total delay accumulator. */
			delay = time - time_arrival[1];
			total_of_delays += delay;
			/* Increment the number of customers delayed, and schedule
			departure. */
			++num_cust_delayed;
			time_next_event[2] = time + expon(mean_service);
			/* Move each customer in queue (if any) up one place. */
			for (i = 1; i <= num_in_q; ++i)
				time_arrival[i] = time_arrival[i + 1]; 

		}

	}

	public void report(){

		//write report heading and input params
		String h1 = ("Single server queuing system");
		String h2 = ("Mean interarrival " + String
			.valueOf(mean_interarrival) + " minutes \n\n");
		String h3 = ("Mean service " + String
			.valueOf(mean_service) + " minutes\n\n");
		String h4 = ("Number of customers " + String
			.valueOf(num_delays_required));

		/* compute and write estimates of desired measures of performance.
		*/
		String s1 = ("Average delay in queue  minutes : " + total_of_delays / num_cust_delayed);
		String s2 = ("Average number in queue : " + area_num_in_q / time);
		String s3 = ("Server utilization : " + area_server_status / time);
		String s4 = ("Time simulation ended: " + time);

		try{
			BufferedWriter writer = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8);
			writer.write(h1);
			writer.newLine();
			writer.write(h2);
			writer.newLine();
			writer.write(h3);
			writer.newLine();
			writer.write(h4);
			writer.newLine();


			writer.write(s1);
			writer.newLine(); 
			writer.write(s2);
			writer.newLine(); 
			writer.write(s3);
			writer.newLine(); 
			writer.write(s4);
			writer.newLine(); 
			writer.close();
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	public void update_time_avg_stats(){
		double time_since_last_event;
		/*compute the time since last event,  update last-event-time
		marker*/
		time_since_last_event = time - time_last_event;
		time_last_event = time;

		/*update number-in-queue function*/
		area_num_in_q += num_in_q * time_since_last_event;
		//update server status
		area_server_status += server_status * time_since_last_event;
	}

	public double expon(double mean){ //exponential variate generation function
		double u;
		/* Generate a U(0,1) random variate. */
		u = random.nextDouble();
		/* Return an exponential random variate with mean "mean". */
		return -mean * Math.log(u); 

	}

	public static void main(String[] args){
		System.out.println("starting simulation...");
		//initialize the simulation
		QueueSimulation simulation = new QueueSimulation();

		//run the simulation while more delays are still needed
		while(simulation.num_cust_delayed < simulation.num_delays_required){
			//determine the next event
			simulation.timing();
			//update time-average statistical accumulators
			simulation.update_time_avg_stats();

			//invoke the appropriate event funtion
			switch(simulation.next_event_type){
				case 1:
					simulation.arrive();
					break;
				case 2:
					simulation.depart();
					break;
			}
		}

		//invoke the report generator and end the simulation
		simulation.report();
		System.out.println("Simulation finished. open out file to see simulation report");
	}
}