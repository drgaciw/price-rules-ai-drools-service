import com.example.pricerulesaidrools.model.Deal;
import com.example.pricerulesaidrools.model.Customer;
import com.example.pricerulesaidrools.ai.dto.EnhancedPricingRequest;
import com.example.pricerulesaidrools.model.PricingRequest;

import java.math.BigDecimal;

public class TestRoutingDemo {
    public static void main(String[] args) {
        System.out.println("=== AI Routing Service Demo ===\n");

        // Test 1: Billing Review Route (Enterprise deal > $100k)
        System.out.println("Test 1: Enterprise Deal > $100k");
        Deal enterpriseDeal = Deal.builder()
                .dealId("DEAL-001")
                .customerId("CUST-001")
                .type(Deal.DealType.ENTERPRISE)
                .value(new BigDecimal("150000"))
                .complexity(Deal.DealComplexity.MEDIUM)
                .build();
        System.out.println("Expected Route: billing-review");
        System.out.println("Reason: Enterprise deal with value $150,000\n");

        // Test 2: Technical Review Route (High complexity)
        System.out.println("Test 2: High Complexity Deal");
        Deal complexDeal = Deal.builder()
                .dealId("DEAL-002")
                .customerId("CUST-002")
                .type(Deal.DealType.STANDARD)
                .value(new BigDecimal("50000"))
                .complexity(Deal.DealComplexity.HIGH)
                .technicalRequirements("Custom integration required")
                .build();
        System.out.println("Expected Route: technical-review");
        System.out.println("Reason: Deal complexity is HIGH\n");

        // Test 3: Risk Review Route (Customer risk score > 70)
        System.out.println("Test 3: High Risk Customer");
        Customer riskyCustomer = Customer.builder()
                .customerId("CUST-003")
                .name("High Risk Corp")
                .churnRiskScore(new BigDecimal("85"))
                .paymentIssuesCount(4)
                .build();
        System.out.println("Expected Route: risk-review");
        System.out.println("Reason: Customer risk score 85 > 70\n");

        // Test 4: Fallback Route (Low confidence)
        System.out.println("Test 4: Standard Deal (Fallback)");
        Deal standardDeal = Deal.builder()
                .dealId("DEAL-004")
                .customerId("CUST-004")
                .type(Deal.DealType.STANDARD)
                .value(new BigDecimal("10000"))
                .complexity(Deal.DealComplexity.LOW)
                .build();
        System.out.println("Expected Route: general-review (fallback)");
        System.out.println("Reason: Confidence below threshold\n");

        System.out.println("=== Routing Decision Logic ===");
        System.out.println("1. Billing Review: type == ENTERPRISE && value > 100000");
        System.out.println("2. Technical Review: complexity == HIGH");
        System.out.println("3. Risk Review: customer.riskScore > 70");
        System.out.println("4. General Review: Fallback when confidence < 0.7");
    }
}