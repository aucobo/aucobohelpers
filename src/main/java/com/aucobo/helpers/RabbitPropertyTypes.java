package com.aucobo.helpers;

/**
 * 
 * Collection of rabbitmq properties
 * when interacting with queue properties/queues/....
 * 
 * @author Norman Möschter-Schenck
 * @version 0.0.1
 * @since 2017-11-14
 */

public enum RabbitPropertyTypes {
		QUEUECONSUMERCOUNT("QUEUE_CONSUMER_COUNT")
		;
		
		private String value;

        private RabbitPropertyTypes(String value) {
        	this.value = value;
        }

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
};
