package app.model;

import app.dto.EventDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "event")
@Entity
@NamedQuery(name = "Event.findAll", query = "SELECT e FROM Event e")

public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "event_id", nullable = false, unique = true)
    private int EventId;
    @Column(name= "title")
    private String Title;
    @Column(name= "description")
    private String Description;
    @Column(name= "date")
    private LocalDateTime Date;
    @Column(name= "time")
    private LocalTime Time;
    @Column(name= "duration")
    private int Duration;
    @Column(name= "capacity")
    private int Capacity;
    @Enumerated(EnumType.STRING)
    private Location location;
    @Column(name= "instructor")
    private String Instructor;
    @Column(name= "price")
    private double Price;
    @Column(name= "status")
    private String Status;
    //    @Lob // This annotation is used to specify that the column should be treated as a Large Object
//    @Column(name = "image", columnDefinition = "bytea", nullable = true) // For PostgreSQL, bytea type is used to store binary data
//    private byte[] image;
    @ManyToOne
    private Category category;
    @ManyToMany
    private  Set <User> users = new HashSet<>();

    public Event(String title, String description, LocalDateTime date, LocalTime time, int duration, int capacity, Location location, String instructor, double price, String status, Category category) {
        Title = title;
        Description = description;
        Date = date;
        Time = time;
        Duration = duration;
        Capacity = capacity;
        this.location = location;
        Instructor = instructor;
        Price = price;
        Status = status;
        this.category = category;
    }


    public void addUser(User user) {
        users.add(user);
        user.getEvents().add(this);
    }
    public void removeUser(User user) {
        users.remove(user);
        user.getEvents().remove(this);
    }

    public Event(EventDTO eventDTO) {
        this.EventId = eventDTO.getEventId();
        this.Title = eventDTO.getTitle();
        this.Description = eventDTO.getDescription();
        this.Date = LocalDate.from(eventDTO.getDate()).atStartOfDay();
        this.Time = eventDTO.getTime();
        this.Duration = eventDTO.getDuration();
        this.Capacity = eventDTO.getCapacity();
        this.location = eventDTO.getLocation();
        this.Instructor = eventDTO.getInstructor();
        this.Price = eventDTO.getPrice();
        this.Status = eventDTO.getStatus();
        this.category = eventDTO.getCategory();
    }
}