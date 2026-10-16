import java.util.Date;
import java.util.ArrayList;
import java.util.List;

// 增加这个主类，为了匹配你的文件名 ShoppingSystem.java
public class ShoppingSystem {
    // 可以在这里写 main 方法作为系统的入口
}

// 状态枚举，去掉 public
enum OrderStatus {
    CREATE, PAID, SHIPPING, DELIVERED, CANCEL
}

// 以下类均去掉 public
class Customer {
    private String name;
    private String contact;
    private String deliveryAddress;
    private boolean active;
    
    // 1对多：一个客户可以有多个订单
    private List<Order> orders = new ArrayList<>();
}

class Order {
    private Date createDate;
    private OrderStatus status;
    
    // 组合关系：一个订单包含多个订单详情
    private List<OrderDetail> orderDetails = new ArrayList<>();
    // 关联关系：一个订单对应支付方式
    private List<Payment> payments = new ArrayList<>();
}

class OrderDetail {
    private int quantity;
    
    // 关联关系：订单详情对应具体的商品
    private Product product;

    public double calculateSubTotal() {
        if(product != null) {
            return product.getPriceForQuantity() * quantity;
        }
        return 0.0;
    }

    public double calculateWeight() {
        if(product != null) {
            return product.getWeight() * quantity;
        }
        return 0.0;
    }
}

class Product {
    private String title;
    private double weight;
    private String description;

    public double getPriceForQuantity() {
        return 0.0; // 实际业务逻辑
    }

    public double getWeight() {
        return weight;
    }
}

// 支付方式抽象类
abstract class Payment {
    protected double amount;
}

// 以下为继承 Payment 的 5 种具体支付方式
class Credit extends Payment {
    private String number;
    private String type;
    private Date expireDate;
}

class Cash extends Payment {
    private double cashTendered;
}

class WireTransfer extends Payment {
    private String bankID;
    private String bankName;
}

class AliPay extends Payment {
    private String number;
}

class WeixinPay extends Payment {
    private String number;
}