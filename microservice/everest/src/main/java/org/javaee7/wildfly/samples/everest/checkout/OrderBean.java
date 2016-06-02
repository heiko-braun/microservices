package org.javaee7.wildfly.samples.everest.checkout;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Future;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.netflix.ribbon.ClientOptions;
import com.netflix.ribbon.Ribbon;
import com.netflix.ribbon.http.HttpRequestTemplate;
import com.netflix.ribbon.http.HttpResourceGroup;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.javaee7.wildfly.samples.everest.cart.Cart;
import org.javaee7.wildfly.samples.everest.cart.CartItem;
import org.javaee7.wildfly.samples.services.discovery.ServiceDiscovery;
import rx.Observable;
import rx.Observer;

/**
 * @author arungupta
 */
@Named
@SessionScoped
public class OrderBean implements Serializable {

    @Inject
    Order order;

    @Inject
    Cart cart;

    String status;

    @Inject ServiceDiscovery services;

    public void saveOrder() {

        List<CartItem> cartItems = cart.getItems();
        cartItems.stream().map((cartItem) -> {
            OrderItem orderItem = new OrderItem();
            orderItem.itemId = cartItem.getItemId();
            orderItem.itemCount = cartItem.getItemCount();
            return orderItem;
        }).forEach((orderItem) -> {
            order.getOrderItems().add(orderItem);
        });

        try {

            // the request context thread locals
            HystrixRequestContext context = HystrixRequestContext.initializeContext();

            try {
                HttpResourceGroup httpResourceGroup = Ribbon.createHttpResourceGroup(
                        "order", // the name of the service in the registry
                        ClientOptions.create()
                                .withMaxAutoRetriesNextServer(3)
                );

                HttpRequestTemplate<ByteBuf> submitOrderTemplate = httpResourceGroup.newTemplateBuilder("submitOrder", ByteBuf.class)
                        .withMethod("POST")
                        .withUriTemplate("/order/resources/order")
                        /*.withFallbackProvider(new RecommendationServiceFallbackHandler())*/
                        .build();

                Observable<ByteBuf> obs = submitOrderTemplate.requestBuilder()
                        .withContent(Observable.just(Unpooled.wrappedBuffer(order.asJsonString().getBytes())))
                        .build()
                        .toObservable();

                obs.subscribe(new Observer<ByteBuf>() {
                    @Override
                    public void onCompleted() {
                        status = "Order processed successfully";
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        status = "Failed to process order";
                    }

                    @Override
                    public void onNext(ByteBuf byteBuf) {
                        // ignore
                    }
                });

            } finally {
                context.shutdown();
            }

            cart.clearCart();      // bummer, if the req fails everything is lost

        } catch (Exception e) {
            status = e.getLocalizedMessage();
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
