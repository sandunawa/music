package com.melodymart.service;

import com.melodymart.model.Invoice;
import com.melodymart.model.Order;
import com.melodymart.model.OrderItem;
import com.melodymart.model.Rental;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class InvoiceService {

    /**
     * Generates an invoice structure for a completed retail purchase order.
     */
    public Invoice generateOrderInvoice(Order order) {
        Invoice invoice = new Invoice();
        invoice.setId("INV-ORD-" + order.getId().replace("o-", ""));
        invoice.setType("ORDER");
        invoice.setReferenceId(order.getId());
        invoice.setIssueDate(order.getOrderDate());
        invoice.setCustomerName(order.getCustomerName());
        invoice.setCustomerEmail(order.getCustomerEmail());
        invoice.setBillingAddress(order.getShippingAddress());
        invoice.setPhoneNumber(order.getPhoneNumber());

        List<Invoice.InvoiceItem> invoiceItems = new ArrayList<>();
        double subtotal = 0.0;
        for (OrderItem orderItem : order.getItems()) {
            double itemSubtotal = orderItem.getPrice() * orderItem.getQuantity();
            subtotal += itemSubtotal;
            invoiceItems.add(new Invoice.InvoiceItem(
                    orderItem.getProductName(),
                    orderItem.getQuantity(),
                    orderItem.getPrice(),
                    itemSubtotal
            ));
        }

        invoice.setItems(invoiceItems);
        invoice.setSubtotal(Math.round(subtotal * 100.0) / 100.0);
        
        // Let's assume a 10% standard value added tax
        double tax = subtotal * 0.10;
        invoice.setTax(Math.round(tax * 100.0) / 100.0);
        invoice.setTotal(Math.round((subtotal + tax) * 100.0) / 100.0);

        return invoice;
    }

    /**
     * Generates an invoice structure for a completed rental booking contract.
     */
    public Invoice generateRentalInvoice(Rental rental) {
        Invoice invoice = new Invoice();
        invoice.setId("INV-REN-" + rental.getId().replace("r-", ""));
        invoice.setType("RENTAL");
        invoice.setReferenceId(rental.getId());
        invoice.setIssueDate(rental.getBookingDate());
        invoice.setCustomerName(rental.getCustomerName());
        invoice.setCustomerEmail(rental.getCustomerEmail());
        invoice.setBillingAddress("E-Rental Booking (Digital Address)");
        invoice.setPhoneNumber("N/A"); // rentals are booked online

        List<Invoice.InvoiceItem> invoiceItems = new ArrayList<>();
        String description = String.format("Instrument Rental: %s (%s to %s - %d Days)",
                rental.getProductName(),
                rental.getStartDate().toString(),
                rental.getEndDate().toString(),
                rental.getTotalDays()
        );

        invoiceItems.add(new Invoice.InvoiceItem(
                description,
                1,
                rental.getTotalCost(),
                rental.getTotalCost()
        ));

        invoice.setItems(invoiceItems);
        
        // In this rental invoice, let's treat the booking fee as subtotal
        double subtotal = rental.getTotalCost();
        invoice.setSubtotal(subtotal);

        // 10% VAT
        double tax = subtotal * 0.10;
        invoice.setTax(Math.round(tax * 100.0) / 100.0);
        invoice.setTotal(Math.round((subtotal + tax) * 100.0) / 100.0);

        return invoice;
    }
}
