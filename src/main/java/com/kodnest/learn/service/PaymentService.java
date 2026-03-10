@Service
public class PaymentService {

    @Value("${razorpay.key_id}")
    private String razorpayKeyId;

    @Value("${razorpay.key_secret}")
    private String razorpayKeySecret;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;

    public PaymentService(OrderRepository orderRepository,
                          OrderItemRepository orderItemRepository,
                          CartRepository cartRepository) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
    }

    @Transactional
    public String createOrder(int userId, BigDecimal totalAmount) throws RazorpayException {

        RazorpayClient razorpayClient =
                new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", totalAmount.multiply(BigDecimal.valueOf(100)).intValue());
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "txn_" + System.currentTimeMillis());

        com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);

        Order order = new Order();
        order.setOrderId(razorpayOrder.get("id")); // Razorpay Order ID
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        orderRepository.save(order);

        return razorpayOrder.get("id");
    }

    @Transactional
    public boolean verifyPayment(String razorpayOrderId,
                                 String razorpayPaymentId,
                                 String razorpaySignature,
                                 int userId) {

        try {

            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", razorpayOrderId);
            attributes.put("razorpay_payment_id", razorpayPaymentId);
            attributes.put("razorpay_signature", razorpaySignature);

            boolean verified =
                    com.razorpay.Utils.verifyPaymentSignature(attributes, razorpayKeySecret);

            if (!verified) {
                return false;
            }

            Order order = orderRepository.findById(razorpayOrderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            order.setStatus(OrderStatus.SUCCESS);
            order.setUpdatedAt(LocalDateTime.now());

            orderRepository.save(order);

            // Fetch cart items
            List<CartItem> cartItems =
                    cartRepository.findCartItemsWithProductDetails(userId);

            for (CartItem cartItem : cartItems) {

                OrderItem orderItem = new OrderItem();

                orderItem.setOrder(order);
                orderItem.setProductId(cartItem.getProduct().getProductId());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setPricePerUnit(cartItem.getProduct().getPrice());

                orderItem.setTotalPrice(
                        cartItem.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity()))
                );

                orderItemRepository.save(orderItem);
            }

            // Clear cart AFTER payment success
            cartRepository.deleteAllCartItemsByUserId(userId);

            return true;

        } catch (Exception e) {

            e.printStackTrace();
            return false;

        }
    }
}
