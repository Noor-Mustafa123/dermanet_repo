package io.github.nomus.dermanet.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import io.github.nomus.dermanet.dto.CheckoutRequest;
import io.github.nomus.dermanet.dto.CheckoutResponse;
import io.github.nomus.dermanet.dto.CheckoutSuccessRequest;
import io.github.nomus.dermanet.dto.CheckoutSuccessResponse;
import io.github.nomus.dermanet.entity.Coupon;
import io.github.nomus.dermanet.entity.Order;
import io.github.nomus.dermanet.entity.OrderItem;
import io.github.nomus.dermanet.entity.User;
import io.github.nomus.dermanet.repository.CouponRepository;
import io.github.nomus.dermanet.repository.OrderRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {
    
    @Value("${stripe.secret-key}")
    private String stripeSecretKey;
    
    @Value("${client.url}")
    private String clientUrl;
    
    @Autowired
    private CouponRepository couponRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CouponService couponService;
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }
    
    public CheckoutResponse createCheckoutSession(CheckoutRequest request, User user) {
        try {
            if (request.getProducts() == null || request.getProducts().isEmpty()) {
                throw new IllegalArgumentException("Invalid or empty products array");
            }
            
            long totalAmount = 0;
            List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
            
            for (CheckoutRequest.CheckoutProduct product : request.getProducts()) {
                long amount = product.getPrice().multiply(BigDecimal.valueOf(100)).longValue();
                totalAmount += amount * product.getQuantity();
                
                SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                        .setPriceData(
                                SessionCreateParams.LineItem.PriceData.builder()
                                        .setCurrency("usd")
                                        .setProductData(
                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                        .setName(product.getName())
                                                        .addImage(product.getImage())
                                                        .build()
                                        )
                                        .setUnitAmount(amount)
                                        .build()
                        )
                        .setQuantity((long) product.getQuantity())
                        .build();
                
                lineItems.add(lineItem);
            }
            
            Coupon coupon = null;
            if (request.getCouponCode() != null && !request.getCouponCode().isEmpty()) {
                coupon = couponRepository.findByCodeAndUserIdAndIsActiveTrue(
                        request.getCouponCode(), user.getId()).orElse(null);
                if (coupon != null) {
                    totalAmount -= (totalAmount * coupon.getDiscountPercentage()) / 100;
                }
            }
            
            Map<String, String> metadata = new HashMap<>();
            metadata.put("userId", user.getId().toString());
            metadata.put("couponCode", request.getCouponCode() != null ? request.getCouponCode() : "");
            
            StringBuilder productsJson = new StringBuilder("[");
            for (int i = 0; i < request.getProducts().size(); i++) {
                CheckoutRequest.CheckoutProduct p = request.getProducts().get(i);
                if (i > 0) productsJson.append(",");
                productsJson.append("{\"id\":").append(p.getId())
                        .append(",\"quantity\":").append(p.getQuantity())
                        .append(",\"price\":").append(p.getPrice()).append("}");
            }
            productsJson.append("]");
            metadata.put("products", productsJson.toString());
            
            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(clientUrl + "/purchase-success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(clientUrl + "/purchase-cancel")
                    .putAllMetadata(metadata);
            
            for (SessionCreateParams.LineItem item : lineItems) {
                paramsBuilder.addLineItem(item);
            }
            
            if (coupon != null) {
                com.stripe.model.Coupon stripeCoupon = createStripeCoupon(coupon.getDiscountPercentage());
                paramsBuilder.addDiscount(
                        SessionCreateParams.Discount.builder()
                                .setCoupon(stripeCoupon.getId())
                                .build()
                );
            }
            
            Session session = Session.create(paramsBuilder.build());
            
            if (totalAmount >= 20000) {
                couponService.createNewCoupon(user);
            }
            
            return CheckoutResponse.builder()
                    .id(session.getId())
                    .totalAmount(totalAmount / 100.0)
                    .build();
                    
        } catch (StripeException e) {
            throw new RuntimeException("Error processing checkout: " + e.getMessage());
        }
    }
    
    @Transactional
    public CheckoutSuccessResponse checkoutSuccess(CheckoutSuccessRequest request) {
        try {
            Session session = Session.retrieve(request.getSessionId());
            
            if ("paid".equals(session.getPaymentStatus())) {
                String couponCode = session.getMetadata().get("couponCode");
                if (couponCode != null && !couponCode.isEmpty()) {
                    Long userId = Long.parseLong(session.getMetadata().get("userId"));
                    couponRepository.findByCodeAndUserIdAndIsActiveTrue(couponCode, userId)
                            .ifPresent(coupon -> {
                                coupon.setIsActive(false);
                                couponRepository.save(coupon);
                            });
                }
                
                String productsJson = session.getMetadata().get("products");
                List<OrderItem> orderItems = parseProductsJson(productsJson);
                
                Order order = Order.builder()
                        .user(User.builder().id(Long.parseLong(session.getMetadata().get("userId"))).build())
                        .totalAmount(BigDecimal.valueOf(session.getAmountTotal() / 100.0))
                        .stripeSessionId(request.getSessionId())
                        .build();
                
                Order savedOrder = orderRepository.save(order);
                
                for (OrderItem item : orderItems) {
                    item.setOrder(savedOrder);
                }
                savedOrder.setOrderItems(orderItems);
                orderRepository.save(savedOrder);
                
                return CheckoutSuccessResponse.builder()
                        .success(true)
                        .message("Payment successful, order created, and coupon deactivated if used.")
                        .orderId(savedOrder.getId())
                        .build();
            }
            
            throw new RuntimeException("Payment not completed");
            
        } catch (StripeException e) {
            throw new RuntimeException("Error processing successful checkout: " + e.getMessage());
        }
    }
    
    private com.stripe.model.Coupon createStripeCoupon(Integer discountPercentage) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("percent_off", discountPercentage);
        params.put("duration", "once");
        return com.stripe.model.Coupon.create(params);
    }
    
    private List<OrderItem> parseProductsJson(String json) {
        List<OrderItem> items = new ArrayList<>();
        json = json.substring(1, json.length() - 1);
        String[] products = json.split("\\},\\{");
        
        for (String product : products) {
            product = product.replace("{", "").replace("}", "");
            String[] fields = product.split(",");
            
            Long productId = null;
            Integer quantity = null;
            BigDecimal price = null;
            
            for (String field : fields) {
                String[] kv = field.split(":");
                String key = kv[0].replace("\"", "").trim();
                String value = kv[1].replace("\"", "").trim();
                
                if ("id".equals(key)) productId = Long.parseLong(value);
                else if ("quantity".equals(key)) quantity = Integer.parseInt(value);
                else if ("price".equals(key)) price = new BigDecimal(value);
            }
            
            OrderItem item = OrderItem.builder()
                    .product(io.github.nomus.dermanet.entity.Product.builder().id(productId).build())
                    .quantity(quantity)
                    .price(price)
                    .build();
            items.add(item);
        }
        
        return items;
    }
}
