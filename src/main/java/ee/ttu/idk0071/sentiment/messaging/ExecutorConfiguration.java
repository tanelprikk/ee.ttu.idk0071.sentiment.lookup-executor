package ee.ttu.idk0071.sentiment.messaging;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ee.ttu.idk0071.sentiment.messaging.executors.DomainLookupExecutor;

@Configuration
public class ExecutorConfiguration extends MessageConfiguration {
	protected final String lookupQueue = "lookup-request-queue";
	@Value("${domain-lookups.executors.pool-size}") 
	protected int poolSize;

	@Autowired
	private DomainLookupExecutor lookupExecutor;

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setRoutingKey(this.lookupQueue);
		template.setQueue(this.lookupQueue);
		template.setMessageConverter(jsonMessageConverter());
		return template;
	}

	@Bean
	public SimpleMessageListenerContainer listenerContainer(ConnectionFactory connectionFactory) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(this.lookupQueue);
		container.setMessageListener(messageListenerAdapter());
		container.setConcurrentConsumers(poolSize);
		return container;
	}

	@Bean
	public MessageListenerAdapter messageListenerAdapter() {
		return new MessageListenerAdapter(lookupExecutor, jsonMessageConverter());
	}
}
