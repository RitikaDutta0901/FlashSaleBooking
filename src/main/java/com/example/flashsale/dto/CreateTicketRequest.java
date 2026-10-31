package com.example.flashsale.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreateTicketRequest {

    @NotBlank(message = "eventName is required")
    private String eventName;

    @NotNull(message = "price is required")
    @DecimalMin(value = "0.01", message = "price must be greater than zero")
    private BigDecimal price;

    @NotNull(message = "availableStock is required")
    @Min(value = 0, message = "availableStock cannot be negative")
    private Integer availableStock;

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(Integer availableStock) {
        this.availableStock = availableStock;
    }
}
