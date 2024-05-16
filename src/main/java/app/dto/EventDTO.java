package app.dto;


import app.model.Category;
import app.model.Event;
import app.model.Location;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

@Getter
@NoArgsConstructor
@Setter
@ToString
public class EventDTO {
private int EventId;
    private String Title;
    private String Description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate Date;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime Time;
    private int Duration;
    private int Capacity;
    private Location location;
    private String Instructor;
    private double Price;
    private Category category;
    //private String Image;
    private String Status;

    public EventDTO(int eventId, String title, String description, LocalDate date, LocalTime time, int duration, int capacity, Location location, String instructor, double price, Category category, String status) {
        EventId = eventId;
        Title = title;
        Description = description;
        Date = date;
        Time = time;
        Duration = duration;
        Capacity = capacity;
        this.location = location;
        Instructor = instructor;
        Price = price;
        this.category = category;
        Status = status;
    }

    public EventDTO(String title, String description, LocalDate date, LocalTime time, int duration, int capacity, Location location, String instructor, double price, Category category, String status) {
        Title = title;
        Description = description;
        Date = date;
        Time = time;
        Duration = duration;
        Capacity = capacity;
        this.location = location;
        Instructor = instructor;
        Price = price;
        this.category = category;
        Status = status;
    }

    public EventDTO(Event event) {
        EventId = event.getEventId();
        Title = event.getTitle();
        Description = event.getDescription();
        Date = LocalDate.from(event.getDate());
        Time = LocalTime.from(event.getTime());
        Duration = event.getDuration();
        Capacity = event.getCapacity();
        this.location = event.getLocation();
        Instructor = event.getInstructor();
        Price = event.getPrice();
        this.category = event.getCategory();
        //Image = Arrays.toString(event.getImage());
        Status = event.getStatus();
    }

    public Integer getCategoryId() {
        // Check if category is not null to avoid NullPointerException
        if (this.category != null) {
            return this.category.getCategoryId();
        }
        return null; // Return null if category is null
    }
}
