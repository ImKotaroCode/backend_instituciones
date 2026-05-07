package backend_instituciones.backend_instituciones.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "lms.topic";
    public static final String DLX = "lms.dlx";

    public static final String EMAIL_QUEUE = "EMAIL_QUEUE";
    public static final String PUSH_QUEUE = "PUSH_QUEUE";
    public static final String REPORT_EXPORT_QUEUE = "REPORT_EXPORT_QUEUE";
    public static final String GRADE_AUDIT_QUEUE = "GRADE_AUDIT_QUEUE";
    public static final String ANNOUNCEMENT_QUEUE = "ANNOUNCEMENT_QUEUE";
    public static final String FAILED_QUEUE = "FAILED_QUEUE";

    @Bean
    public TopicExchange lmsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", FAILED_QUEUE)
                .build();
    }

    @Bean
    public Queue pushQueue() {
        return QueueBuilder.durable(PUSH_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .build();
    }

    @Bean
    public Queue reportExportQueue() {
        return QueueBuilder.durable(REPORT_EXPORT_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .build();
    }

    @Bean
    public Queue gradeAuditQueue() {
        return QueueBuilder.durable(GRADE_AUDIT_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .build();
    }

    @Bean
    public Queue announcementQueue() {
        return QueueBuilder.durable(ANNOUNCEMENT_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .build();
    }

    @Bean
    public Queue failedQueue() {
        return QueueBuilder.durable(FAILED_QUEUE).build();
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue()).to(lmsExchange()).with("notification.email.#");
    }

    @Bean
    public Binding pushBinding() {
        return BindingBuilder.bind(pushQueue()).to(lmsExchange()).with("notification.push.#");
    }

    @Bean
    public Binding reportExportBinding() {
        return BindingBuilder.bind(reportExportQueue()).to(lmsExchange()).with("report.export.#");
    }

    @Bean
    public Binding gradeAuditBinding() {
        return BindingBuilder.bind(gradeAuditQueue()).to(lmsExchange()).with("grade.audit.#");
    }

    @Bean
    public Binding announcementBinding() {
        return BindingBuilder.bind(announcementQueue()).to(lmsExchange()).with("announcement.#");
    }

    @Bean
    public Binding failedBinding() {
        return BindingBuilder.bind(failedQueue()).to(dlxExchange()).with(FAILED_QUEUE);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }
}
