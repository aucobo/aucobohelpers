package com.aucobo.helpers.rabbitmq;

import java.util.Objects;
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

import com.aucobo.helpers.rabbitmq.exceptions.NoQueueFoundException;

/**
 * 
 * Handles/Creates/... rabbitmq queues and exchanges.
 * 
 * <p>
 * Since version 0.0.2 added function getQueueConsumerCount()
 * <p>
 * Since version 0.0.3 added constant for "QUEUE_CONSUMER_COUNT"
 * Added javadoc some information.
 * <p>
 * Since version 0.0.4 upgrade to spring boot 1.5.7
 * <p>
 * Since version 0.0.5 improved exception handling/throwing exceptions
 *
 *  @author Norman Möschter-Schenck
 *  @version 0.0.5
 *  @since 2017-01-01
 */
@Configuration
public class RabbitQueueController {
	private static final Logger logger = Logger.getLogger(RabbitQueueController.class);
	@Autowired AmqpAdmin admin;

	public RabbitQueueController(){
	}

	/**
	 * Checks whether queue exists.
	 *
	 * @param  queueName name of queue to check
	 * @return true when queue exists, else false
	 * @throws AmqpConnectException connection to rabbitmq not possible
	 * @throws AmqpTimeoutException connection to rabbitmq timed out
	 */
	public boolean doesQueueExist(String queueName) throws AmqpConnectException, AmqpTimeoutException {
		logger.info("Does queue exist: " + queueName);
		if( Objects.isNull(admin) ){
			logger.fatal("no amqp admin object for rabbitSender");
			return false;
		}
		if( Objects.isNull(queueName) ){
			logger.warn("no queue name given");
			return false;
		}
		if( Objects.isNull(admin.getQueueProperties(queueName)) ){
			logger.info("queue does not exist " + queueName);
			return false;
		}
		return true;
	}

  /**
	 * Create a queue.
	 *
	 * @param  queueName name of queue to check
	 * @param  durable whether the queue should be created as durable queue
	 * @return Queue the queue that was created
	 * @throws AmqpConnectException connection to rabbitmq not possible
	 * @throws AmqpTimeoutException connection to rabbitmq timed out
	 * @throws AmqpIOException read/write error with rabbitmq
	 */
	private Queue createQueue(String queueName, boolean durable) throws AmqpConnectException, AmqpIOException, AmqpTimeoutException {
		logger.info("create queue: " + queueName);
		Queue queue = null;
		if( Objects.isNull(admin) ){
			logger.fatal("no amqp admin object for rabbitSender");
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
	 * @param queueName name of queue to check
	 * @return Queue
	 * @throws AmqpConnectException connection to rabbitmq not possible
	 * @throws AmqpTimeoutException connection to rabbitmq timed out
	 * @throws AmqpIOException read/write error with rabbitmq
	 */
	public Queue createAutodeleteQueue(String queueName) throws AmqpConnectException, AmqpIOException, AmqpTimeoutException {
		return createQueue(queueName, false);
	}
	/**
	 * create durable queue
	 * e.g.: (action)worker queues, trigger topic queues
	 * @param queueName name of queue to check
	 * @return Queue
	 * @throws AmqpConnectException connection to rabbitmq not possible
	 * @throws AmqpTimeoutException connection to rabbitmq timed out
	 * @throws AmqpIOException read/write error with rabbitmq
	 */
	public Queue createDurableQueue(String queueName) throws AmqpConnectException, AmqpIOException, AmqpTimeoutException {
		return createQueue(queueName, true);
	}


	/**
	 * delete a queue
	 * @param queueName name of queue to check
	 * @return boolean whether queue was deleted
	 * @throws AmqpConnectException connection to rabbitmq not possible
	 * @throws AmqpTimeoutException connection to rabbitmq timed out
	 * @throws AmqpIOException read/write error with rabbitmq
	 */
	public boolean deleteQueue(String queueName) throws AmqpConnectException, AmqpIOException, AmqpTimeoutException {
		logger.info("delete queue: " + queueName);
		if( Objects.isNull(admin) ){
			logger.fatal("no amqp admin object for rabbitSender");
			return false;
		}
		if ( doesQueueExist(queueName) ) {
			admin.deleteQueue(queueName);
			logger.info("queue deleted: " + queueName);
			return true;
		}
		logger.warn("queue does not exist: " + queueName);
		return false;
	}

	/**
	 * purge (empty) a queue, do not delete it
	 * @param queueName name of queue to check
	 * @return boolean whether queue was purged
	 * @throws AmqpConnectException connection to rabbitmq not possible
	 * @throws AmqpTimeoutException connection to rabbitmq timed out
	 * @throws AmqpIOException read/write error with rabbitmq
	 */
	public boolean purgeQueue(String queueName) throws AmqpConnectException, AmqpIOException, AmqpTimeoutException {
		logger.info("purge queue: " + queueName);
		if( Objects.isNull(admin) ){
			logger.fatal("no amqp admin object for rabbitSender");
			return false;
		}
		if ( doesQueueExist(queueName) ) {
			admin.purgeQueue(queueName, true); // true = wait
			logger.info("queue purged: " + queueName);
			return true;
		}
		logger.warn("queue does not exist: " + queueName);
		return false;
	}

	/**
	 * get the count of consumers to a queue
	 * @param queueName name of queue to check
	 * @return Integer consumer count of rabbitmq queue
	 * @throws NoQueueFoundException when no properties could be read, because there is no rabbitmq queue
	 */
	public Integer getQueueConsumerCount(String queueName) throws NoQueueFoundException {
		if( Objects.isNull(admin) ){
			logger.fatal("no amqp admin object for rabbitSender");
			return -1;
		}
		Properties queueProperties = admin.getQueueProperties(queueName);
		if( Objects.isNull(queueProperties) ){
			throw new NoQueueFoundException("on getting the consuemr count, no queue exists with name: " + queueName);
//			logger.warn("no queueProperties exists for rabbitSender for queue: " + queueName);
//			return -1;
		}
		Integer consumerCount = (Integer) queueProperties.get(RabbitPropertyTypes.QUEUECONSUMERCOUNT);
		return consumerCount;
	}

	private Queue getNewQueueAndBindToExchange(String queueName, String exchangeName, boolean durable) throws AmqpConnectException, AmqpIOException {
		if( Objects.isNull(admin) ){
			logger.fatal("no amqp admin object for rabbitSender");
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
		if( Objects.isNull(admin) ){
			logger.fatal("no amqp admin object for rabbitSender");
			return exchange;
		}

		exchange = new FanoutExchange(exchangeName);
		try{
			admin.declareExchange(exchange);
		}catch(AmqpIOException e){
			logger.warn("exchange already declared differently " + exchange.getName());
			throw e;
		}
		logger.info("fanout exchange created: " + exchangeName);
		return exchange;
	}
}
