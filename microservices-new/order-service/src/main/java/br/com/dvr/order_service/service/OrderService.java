package br.com.dvr.order_service.service;

import br.com.dvr.order_service.dto.InventoryResponse;
import br.com.dvr.order_service.dto.OrderLineItemsDto;
import br.com.dvr.order_service.dto.OrderRequest;
import br.com.dvr.order_service.model.Order;
import br.com.dvr.order_service.model.OrderLineItems;
import br.com.dvr.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;

    public void placeOrder(OrderRequest request) throws IllegalAccessException {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = request.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemsList().stream().map(OrderLineItems::getSkuCode)
                .toList();


        InventoryResponse[] inventoryResponseArray = webClient.get()
                .uri("http://localhost:8082/api/inventory",
                        uriBuilder ->
                                uriBuilder
                                        .queryParam("skuCode", skuCodes)
                                        .build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        boolean allProductsInStock = false;

        if (inventoryResponseArray != null) {
            allProductsInStock = Arrays.stream(inventoryResponseArray).allMatch(
                    InventoryResponse::isInStock);
        }

        if (allProductsInStock) {
            orderRepository.save(order);
        } else {
            throw new IllegalAccessException("Product is not in stock, please try again later");
        }


    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;

    }
}
