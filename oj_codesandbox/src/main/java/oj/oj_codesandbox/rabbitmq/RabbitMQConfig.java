package oj.oj_codesandbox.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    // 定义交换机和队列名称
    public static final String JUDGE_EXCHANGE = "judge.exchange";
    public static final String JUDGE_QUEUE = "judge.queue";
    public static final String JUDGE_ROUTING_KEY = "judge.routing.key";

    @Bean
    public DirectExchange judgeExchange() {
        return ExchangeBuilder.directExchange(JUDGE_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue judgeQueue() {
        return QueueBuilder.durable(JUDGE_QUEUE)
                .withArgument("x-message-ttl", 300000) // 5分钟TTL
                .withArgument("x-dead-letter-exchange", "judge.dlx")
                .build();
    }

    @Bean
    public Binding judgeBinding() {
        return BindingBuilder.bind(judgeQueue())
                .to(judgeExchange())
                .with(JUDGE_ROUTING_KEY);
    }

    @Bean
    public Declarables declarables() {
        return new Declarables(
                judgeExchange(),
                judgeQueue(),
                judgeBinding()
        );
    }
}