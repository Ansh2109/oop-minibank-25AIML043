package model;

public class Customer implements Cloneable {

    private String name;
    private String email;
    private String mobile;
    private final String customerId;
    private Address address;

    private static long customerCounter = 100;

    private static String generateCustomerId() {

        customerCounter++;

        return "CUST" + customerCounter;
    }

    public Customer(
            String name,
            String email,
            String mobile,
            Address address
    ) {

        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.address = address;
        this.customerId = generateCustomerId();
    }

    public Address getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public String getCustomerId() {
        return customerId;
    }

    @Override
    public Customer clone() {

        try {
            return (Customer) super.clone();

        } catch (CloneNotSupportedException e) {

            return null;
        }
    }

    public static class Address {

        private String line;
        private String city;
        private String pincode;

        public Address(
                String line,
                String city,
                String pincode
        ) {

            this.line = line;
            this.city = city;
            this.pincode = pincode;
        }

        public String getLine() {
            return line;
        }

        public String getCity() {
            return city;
        }

        public String getPincode() {
            return pincode;
        }
    }
}