package org.javaee7.wildfly.samples.everest.checkout;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import org.javaee7.wildfly.samples.services.discovery.ServiceDiscovery;

/**
 * @author Heiko Braun
 * @since 31/05/16
 */
public class OrderCommand extends HystrixCommand<String> {

    private final ServiceDiscovery services;

    private final Entity<String> order;

    public OrderCommand(ServiceDiscovery services, Entity<String> orderJson) {
        super(HystrixCommandGroupKey.Factory.asKey(OrderCommand.class.getName()));
        this.services = services;
        this.order = orderJson;
    }

    @Override
    protected String run() throws Exception {

        Response response = services.getOrderService().request()
                .header("Host", "order") // l5d proxy
                .post(order);

        Response.StatusType statusInfo = response.getStatusInfo();
        String status = null;

        if (statusInfo.getFamily() == Response.Status.Family.SUCCESSFUL) {
            JsonObject jsonResponse = Json.createReader(new StringReader(response.readEntity(String.class))).readObject();
            status = "Order successful, order number: " + jsonResponse.get("orderId");;
        } else {
            status = statusInfo.getReasonPhrase();
        }

        return status;
    }



}
