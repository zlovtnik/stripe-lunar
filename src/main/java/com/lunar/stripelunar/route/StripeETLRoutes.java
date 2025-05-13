package com.lunar.stripelunar.route;

import com.lunar.stripelunar.component.ErrorHandlingProcessor;
import com.lunar.stripelunar.component.ETLMetricsProcessor;
import com.lunar.stripelunar.component.StripeETLProcessor;
import com.lunar.stripelunar.service.StripeService;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StripeETLRoutes extends RouteBuilder {

    private final StripeService stripeService;
    private final StripeETLProcessor stripeETLProcessor;
    private final ErrorHandlingProcessor errorHandlingProcessor;
    private final ETLMetricsProcessor etlMetricsProcessor;

    @Override
    public void configure() throws Exception {
        // Global error handler
        errorHandler(defaultErrorHandler()
                .logExhaustedMessageHistory(true)
                .maximumRedeliveries(3)
                .redeliveryDelay(1000)
                .backOffMultiplier(2)
                .useExponentialBackOff());
                
        // Exception handling
        onException(Exception.class)
            .handled(true)
            .process(errorHandlingProcessor)
            .log("Error handled for ${exchangeId}");
            
        // Configure REST DSL
        restConfiguration()
                .component("servlet")
                .bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true")
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "Stripe Lunar ETL API")
                .apiProperty("api.version", "1.0.0");

        // Define direct routes first
        from("direct:getAllCustomers")
            .bean(stripeService, "getAllCustomers");
            
        from("direct:syncCustomers")
            .setHeader("operation", constant("syncCustomers"))
            .process(etlMetricsProcessor)
            .process(stripeETLProcessor);
            
        from("direct:getCustomerById")
            .bean(stripeService, "getCustomer(${header.id})");
            
        from("direct:getAllPayments")
            .bean(stripeService, "getAllPayments");
            
        from("direct:syncPayments")
            .setHeader("operation", constant("syncPayments"))
            .process(etlMetricsProcessor)
            .process(stripeETLProcessor);
            
        from("direct:getPaymentById")
            .bean(stripeService, "getPayment(${header.id})");
            
        from("direct:getPaymentsByCustomerId")
            .bean(stripeService, "getPaymentsByCustomer(${header.customerId})");
            
        from("direct:syncAll")
            .setHeader("operation", constant("syncAll"))
            .process(etlMetricsProcessor)
            .process(stripeETLProcessor);
            
        from("direct:getStatus")
            .setHeader("operation", constant("status"))
            .process(stripeETLProcessor);
            
        from("direct:getMetrics")
            .bean(etlMetricsProcessor, "getAllMetrics");

        // REST endpoints
        rest("/stripe")
            .description("Stripe ETL API")
            
            // Customer endpoints
            .get("/customers")
                .description("Get all customers from database")
                .to("direct:getAllCustomers")
            
            .get("/customers/sync")
                .description("Sync customers from Stripe to database")
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .to("direct:syncCustomers")
            
            .get("/customers/{id}")
                .description("Get customer by ID")
                .to("direct:getCustomerById")
            
            // Payment endpoints
            .get("/payments")
                .description("Get all payments from database")
                .to("direct:getAllPayments")
            
            .get("/payments/sync")
                .description("Sync payments from Stripe to database")
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .to("direct:syncPayments")
            
            .get("/payments/{id}")
                .description("Get payment by ID")
                .to("direct:getPaymentById")
            
            .get("/customers/{customerId}/payments")
                .description("Get payments by customer ID")
                .to("direct:getPaymentsByCustomerId")
            
            // ETL operations
            .get("/sync/all")
                .description("Sync all data from Stripe")
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .to("direct:syncAll")
            
            .get("/status")
                .description("Get ETL status")
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .to("direct:getStatus")
            
            // Metrics endpoint
            .get("/metrics")
                .description("Get ETL metrics")
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .to("direct:getMetrics");

        // Scheduled ETL job routes
        from("quartz://stripeETL/customerSync?cron=0+0+0+*+*+?")
            .routeId("customerSyncScheduled")
            .log("Starting scheduled customer sync from Stripe")
            .setHeader("operation", constant("syncCustomers"))
            .process(etlMetricsProcessor)
            .process(stripeETLProcessor)
            .log("Completed scheduled customer sync from Stripe");

        from("quartz://stripeETL/paymentSync?cron=0+0+1+*+*+?")
            .routeId("paymentSyncScheduled")
            .log("Starting scheduled payment sync from Stripe")
            .setHeader("operation", constant("syncPayments"))
            .process(etlMetricsProcessor)
            .process(stripeETLProcessor)
            .log("Completed scheduled payment sync from Stripe");
            
        // Full sync job - runs at 2 AM every Sunday
        from("quartz://stripeETL/fullSync?cron=0+0+2+?+*+SUN")
            .routeId("fullSyncScheduled")
            .log("Starting full Stripe data sync")
            .setHeader("operation", constant("syncAll"))
            .process(etlMetricsProcessor)
            .process(stripeETLProcessor)
            .log("Completed full Stripe data sync");
    }
}
