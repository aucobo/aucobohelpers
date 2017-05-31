package com.aucobo.helpers;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpIOException;
import org.springframework.amqp.AmqpTimeoutException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;





@Configuration
public class RabbitQueueController {
	private static final Logger logger = Logger.getLogger(RabbitQueueController.class);
	@Autowired AmqpAdmin admin;
    
	/*
	 * does queue exist
	 */	
	public RabbitQueueController(){
	}
	
	public boolean doesQueueExist(String queueName) throws AmqpConnectException, AmqpTimeoutException {
		logger.info("Does queue exist: " + queueName);
		if(admin == null){
			logger.warn("no amqp admin object for rabbitSender");
			return false;
		}
		if(queueName == null){
			logger.info("new queue name given");
			return false;
		}				
		if(admin.getQueueProperties(queueName) == null) {
			logger.info("queue does not exist " + queueName);
			return false;
		}
		
		logger.info("queue exists " + queueName);
		return true;
	}
	
	private Queue createQueue(String queueName, boolean durable) throws AmqpConnectException, AmqpIOException, AmqpTimeoutException {
		logger.info("create queue: " + queueName);
		Queue queue = null;
		if(admin == null){
			logger.warn("no amqp admin object for rabbitSender");
			return queue;
		}

		if(!doesQueueExist(queueName)) {
			// exclusive: only one consumer and delete when channel is closed; autodelete: delete when no subscribers left
			// parameters: queue name, durable, exclusive, auto-delete
			queue = new Queue(queueName, durable, !durable, !durable);
//			queue = new Queue(queueName, false, true, true);
			
			admin.declareQueue(queue); 
			logger.info("queue created: " + queueName);
			return queue;
		}
		/**
		 * presumption:
		 * exclusiv, autodelete queues, which are deleted after workdistributer is stopped 
		 * or rabbitmq server stops
		 */
		return queue;
	}
	
	/**
	 * create exclusive and autodelete queue
	 * e.g.: every instance of the workDistributer
	 * @param queueName
	 * @return
	 * @throws AmqpConnectException
	 * @throws AmqpIOException
	 * @throws AmqpTimeoutException
	 */
	public Queue createAutodeleteQueue(String queueName) throws AmqpConnectException, AmqpIOException, AmqpTimeoutException {
		return createQueue(queueName, false);
	}
	/**
	 * create durable queue
	 * e.g.: (action)worker queues, trigger topic queues
	 * @param queueName
	 * @return
	 * @throws AmqpConnectException
	 * @throws AmqpIOException
	 * @throws AmqpTimeoutException
	 */
	public Queue createDurableQueue(String queueName) throws AmqpConnectException, AmqpIOException, AmqpTimeoutException {
		return createQueue(queueName, true);
	}
	
	
	/*
	 * delete the queue completely
	 */
	public boolean deleteQueue(String queueName) throws AmqpConnectException, AmqpIOException, AmqpTimeoutException {
		logger.info("delete queue: " + queueName);
		if(admin == null){
			logger.warn("no amqp admin object for rabbitSender");
			return false;
		}
		if (doesQueueExist(queueName)) {
			admin.deleteQueue(queueName);
			logger.info("queue deleted: " + queueName);
			return true;
		}
		logger.warn("queue does not exist: " + queueName);
		return false;
	}
	/*
	 * purge a queue (empty the queue), but do not delete it
	 */
	public boolean purgeQueue(String queueName) throws AmqpConnectException, AmqpIOException, AmqpTimeoutException {
		logger.info("purge queue: " + queueName);
		if(admin == null){
			logger.warn("no amqp admin object for rabbitSender");
			return false;
		}
		if (doesQueueExist(queueName)) {
			admin.purgeQueue(queueName, true); // true = wait
			logger.info("queue purged: " + queueName);
			return true;
		}
		logger.warn("queue does not exist: " + queueName);
		return false;
	}
	
	/*
	 * scaling the workDistributer
	 * --> create a new queue with a different name
	 * 	--> findNewQueueName builds a queueName based on "postRUleQueueName" in RabbitConfiguration
	 * 	--> getNewQueue returns the given queue or a new Queue with a new name
	 */
	public Integer getQueueConsumerCount(String queueName){
		if(admin == null){
			logger.warn("no amqp admin object for rabbitSender");
			return -1;
		}
		Properties queueProperties = admin.getQueueProperties(queueName);
		if(queueProperties == null){
			logger.warn("no queueProperties exists for rabbitSender");
			return -1;
		}
		Integer consumerCount = (Integer) queueProperties.get("QUEUE_CONSUMER_COUNT");
		logger.debug(queueName + ": QUEUE_CONSUMER_COUNT = " + consumerCount.toString());
		return (Integer) queueProperties.get("QUEUE_CONSUMER_COUNT");
	}

	private Queue getNewQueueAndBindToExchange(String queueName, String exchangeName, boolean durable) throws AmqpConnectException, AmqpIOException {
		if(admin == null){
			logger.error("no amqp admin object for rabbitSender");
			return null;
		}
		
		FanoutExchange exchange = CreateFanoutExchange(exchangeName);
		
		Queue queue = createQueue(queueName, durable);
		admin.declareBinding(BindingBuilder.bind(queue).to(exchange));
		return queue;
	}
	public Queue getNewAutodeleteQueueAndBindToExchange(String queueName, String exchangeName) throws AmqpConnectException, AmqpIOException {
		return getNewQueueAndBindToExchange(queueName, exchangeName, false);
	}
	public Queue getNewDurableQueueAndBindToExchange(String queueName, String exchangeName) throws AmqpConnectException, AmqpIOException {
		return getNewQueueAndBindToExchange(queueName, exchangeName, true);
	}
	
	/*
	 * create new fanoutExchange, if it does not exists
	 */
	public FanoutExchange CreateFanoutExchange(String exchangeName) throws AmqpConnectException, AmqpIOException, AmqpTimeoutException {
		logger.info("create fanout exchange: " + exchangeName);
		FanoutExchange exchange = null;
		if(admin == null){
			logger.warn("no amqp admin object for rabbitSender");
			return exchange;
		}
			
		exchange = new FanoutExchange(exchangeName);
		try{			
			admin.declareExchange(exchange);
		}catch(AmqpIOException e){
			logger.error("exchange already declared differently " + exchange.getName());
			throw e;
		}
		logger.info("fanout exchange created: " + exchangeName);
		return exchange;
	}
}
