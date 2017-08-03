package acl.replication;

import java.io.IOException;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import acl.ACL;
import acl.command.*;
import io.grpc.ServerBuilder;

public class ReceiverGrpc{
	private static final Logger logger = Logger.getLogger(ReceiverGrpc.class.getName());
	
	private static ACL acl;
	private static Server server;
	public static void setAcl(ACL otherAcl){
		acl = otherAcl;
	}
	public static void run() throws Exception {
		start();
	    blockUntilShutdown();		
	}
	private static void start() throws IOException {
	    /* The port on which the server should run */
	    int port = 5051;
	    server = ServerBuilder.forPort(port).addService(new BroadcastServiceImpl())
	        .build()
	        .start();
	    
	    
	    logger.info("Server started, listening on " + port);
	    Runtime.getRuntime().addShutdownHook(new Thread() {
	      @Override
	      public void run() {
	        // Use stderr here since the logger may have been reset by its JVM shutdown hook.
	        System.err.println("*** shutting down gRPC server since JVM is shutting down");
	        stop();
	        System.err.println("*** server shut down");
	      }
	    });
	}
	
	private static void stop() {
		if (server != null) {
			server.shutdown();
		}
	}
	
	/**
	 * Await termination on the main thread since the grpc library uses daemon threads.
	 */
	private static void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}
	
	static class BroadcastServiceImpl extends BroadcastServiceGrpc.BroadcastServiceImplBase {
		
		@Override
		public void send(BroadcastCommandGrpc cmd, StreamObserver<Empty> responseObserver) {
			Empty reply = Empty.newBuilder().build();
			
			try{
				acl.execute(CommandFactory.get(new CommandData(CommandData.getTypeFromInt(
		    															cmd.getCmd()),
		    															cmd.getArgumentsList())
		    									)
				);
			} catch (Exception e){
		    	e.printStackTrace();
		    }
			
			 
		    responseObserver.onNext(reply);
		    responseObserver.onCompleted();
		}
	}
	

}