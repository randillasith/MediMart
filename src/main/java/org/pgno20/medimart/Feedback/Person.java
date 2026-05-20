package org.pgno20.medimart.Feedback;

public class Person {
    protected String customerName;

    public Person(String customerName) {
        this.customerName = customerName;
    }

    public void displayName() {
        System.out.println(customerName);
    }
}
