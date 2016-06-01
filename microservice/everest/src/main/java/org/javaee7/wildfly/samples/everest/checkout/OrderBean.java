package org.javaee7.wildfly.samples.everest.checkout;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

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
            // This command is essentially a blocking command but provides an Observable facade
            Observable<String> observable = new OrderCommand(services, order.asJson()).toObservable();
            observable.subscribe(
                    new Observer<String>() {

                        @Override
                        public void onCompleted() {
                            cart.clearCart();
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            status = e.getMessage();
                        }

                        @Override
                        public void onNext(String v) {
                            status = v;
                        }

                    }
            );

        } catch (Exception e) {
            e.printStackTrace();
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
