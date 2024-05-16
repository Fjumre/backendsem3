package app.controllers;

import app.config.HibernateConfig;
import app.dao.EventDAO;
import app.dao.UserDAO;
import app.dto.EventDTO;
import app.dto.UserDTO;

import app.model.Category;
import app.model.Event;
import app.model.Location;
import app.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.http.HttpStatus;
import io.javalin.http.Handler;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class EventController implements IEventController {
    EventDAO eventDAO = new EventDAO();
    ObjectMapper objectMapper = new ObjectMapper();


    EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    EntityManager em = emf.createEntityManager();

    public EventController(EventDAO eventDAO) {
        this.eventDAO = eventDAO;
    }


    private List<UserDTO> convertToUserDTO(List<User> users) {
        return users.stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }
//    private UserDTO convertToUserDTO(User user) {
//        return new UserDTO(user);
//    }

    private Event convertToEntity(EventDTO eventDTO) {

        return new Event(eventDTO);
    }

    @Override
    public Handler getAllEvents() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                List<Event> events = eventDAO.getAlleEvents();

                List<EventDTO> eventDTOS = events.stream()
                        .map(EventDTO::new)
                        .collect(Collectors.toList());

                ctx.json(eventDTOS);
            } catch (Exception e) {
                ctx.status(500);
                ctx.json(returnObject.put("msg", "Internal server error"));
            }
        };
    }


    @Override
    public Handler getEventById() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                Event event = eventDAO.getEventById(id);
                EventDTO eventDTO = new EventDTO(event);
                ctx.json(eventDTO);
            } catch (Exception e) {
                ctx.status(500);
                ctx.json(returnObject.put("msg", "Internal server error"));
            }
        };
    }

    @Override
    public Handler createEvent() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                // Parse the incoming JSON to an EventDTO
                EventDTO eventInput = ctx.bodyAsClass(EventDTO.class);

                // Convert EventDTO to Event entity
                Event eventToCreate = convertToEntity(eventInput);

                // Create the event in the database
                Event createdEvent = eventDAO.create(eventToCreate);

                // Convert the created Event back to EventDTO for response
                EventDTO createdEventDTO = new EventDTO(createdEvent);


                // Set status as CREATED and return the created event
                ctx.status(HttpStatus.CREATED).json(createdEventDTO);
            } catch (Exception e) {

                e.printStackTrace();
                ctx.status(500).json(returnObject.put("msg", "Internal server error: " + e.getMessage()));
            }
        };
    }


    @Override
    public Handler updateEvent() {
        return ctx -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                // Parse the incoming JSON to an EventDTO
                EventDTO eventInput = ctx.bodyAsClass(EventDTO.class);

                // Retrieve the event to be updated
                int id = Integer.parseInt(ctx.pathParam("id"));
                Event eventToUpdate = eventDAO.getEventById(id);

                // Check if event exists
                if (eventToUpdate == null) {
                    ctx.status(404).json(returnObject.put("msg", "Event not found"));
                    return;
                }

                // Update the event entity with new values from EventDTO
                updateEventEntityWithDTO(eventToUpdate, eventInput);

                // Update the event in the database
                Event updatedEvent = eventDAO.update(eventToUpdate);

                // Respond with the updated event ID
                ctx.json(returnObject.put("updatedEventId", updatedEvent.getEventId()));

            } catch (NumberFormatException e) {
                ctx.status(400).json(returnObject.put("msg", "Invalid format for event ID"));
            } catch (Exception e) {
                ctx.status(500).json(returnObject.put("msg", "Internal server error: " + e.getMessage()));
            }
        };
    }

    private void updateEventEntityWithDTO(Event event, EventDTO dto) {
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());

        event.setDate(dto.getDate().atStartOfDay());
        event.setTime(dto.getTime());
        event.setDuration(dto.getDuration());
        event.setCapacity(dto.getCapacity());
        event.setLocation(dto.getLocation());
        event.setInstructor(dto.getInstructor());
        event.setPrice(dto.getPrice());
        event.setStatus(dto.getStatus());

        if (dto.getCategoryId() != null) {
            Category category = findCategoryById(dto.getCategoryId());
            event.setCategory(category);
        }
    }

    private Category findCategoryById(Integer categoryId) {
        return em.find(Category.class, categoryId);
    }

    @Override
    public Handler deleteEvent() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                eventDAO.delete(id);
                ctx.status(204);
            } catch (Exception e) {
                ctx.status(500);
                ctx.json(returnObject.put("msg", "Internal server error"));
            }
        };
    }

    @Override
    public Handler getAllRegistrationsForEvent() {
        return ctx -> {
            try {
                int eventId = Integer.parseInt(ctx.pathParam("event_id"));
                List<User> registrations = eventDAO.getRegistrationsForEventById(eventId);
                List<UserDTO> registrationDTOs = convertToUserDTO(registrations);
                ctx.json(registrationDTOs);
            } catch (NumberFormatException e) {
                ctx.status(400).json(Map.of("msg", "Invalid event ID format"));
            } catch (Exception e) {
                ctx.status(500).json(Map.of("msg", "Internal server error"));
                e.printStackTrace();
            }
        };
    }


    @Override
    public Handler getRegistrationById() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                int id = Integer.parseInt(ctx.pathParam("event_id"));
                ctx.json("There are " + eventDAO.getRegistrationsCountById(id) + " users registered");
            } catch (Exception e) {
                ctx.status(500);
                System.out.println(e);
                ctx.json(returnObject.put("msg", "Internal server error"));
            }
        };
    }


    @Override
    public Handler registerUserForEvent() {
        return ctx -> {


            int eventId = Integer.parseInt(ctx.pathParam("event_id"));
            JsonObject requestBody = JsonParser.parseString(ctx.body()).getAsJsonObject();

            int userId = requestBody.get("id").getAsInt();

            eventDAO.addUserToEvent(userId, eventId);

            ctx.status(200).result("User registered for the event successfully");
        };
    }

    @Override
    public Handler removeUserFromEvent() {
        return ctx -> {
            int eventId = Integer.parseInt(ctx.pathParam("event_id"));
            JsonObject requestBody = JsonParser.parseString(ctx.body()).getAsJsonObject();

            int userId = requestBody.get("id").getAsInt();

            eventDAO.removeUserEvent(userId, eventId);

            ctx.status(200).result("User removed for the event successfully");
        };


    }

    @Override
    public Handler getAllEventsByCategory() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                int categoryId = Integer.parseInt(ctx.pathParam("category_id"));
                List<Event> events = eventDAO.getEventsByCategory(categoryId);

                List<EventDTO> eventDTOS = events.stream()
                        .map(EventDTO::new)
                        .collect(Collectors.toList());

                ctx.json(eventDTOS);
            } catch (NumberFormatException e) {
                ctx.status(400).json(returnObject.put("msg", "Invalid category ID format"));
            } catch (Exception e) {
                ctx.status(500);
                System.out.println(e);
                ctx.json(returnObject.put("msg", "Internal server error"));
            }
        };
    }

    @Override
    public Handler getAllEventsByStatus() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                String status = ctx.pathParam("status");
                List<Event> events = eventDAO.getEventByStatus(status);

                List<EventDTO> eventDTOS = events.stream()
                        .map(EventDTO::new)
                        .collect(Collectors.toList());

                ctx.json(eventDTOS);
            } catch (NumberFormatException e) {
                ctx.status(400).json(returnObject.put("msg", "Invalid category ID format"));
            } catch (Exception e) {
                ctx.status(500);
                System.out.println(e);
                ctx.json(returnObject.put("msg", "Internal server error"));
            }
        };
    }
}
