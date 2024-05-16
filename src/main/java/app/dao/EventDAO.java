package app.dao;


import app.model.Event;
import app.model.User;
import jakarta.persistence.*;

import java.util.List;

public class EventDAO {
    private static EventDAO instance;
    private static EntityManagerFactory emf;
public EventDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public static EventDAO getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            EventDAO.emf = emf;
            instance = new EventDAO(EventDAO.emf);
        }
        return instance;
    }

    public EventDAO() {
    }
    // As a user, I want to see all the events/workshops that are going to be held.

    public List<Event> getAlleEvents() {
        EntityManager em = emf.createEntityManager();
        return em.createQuery("SELECT e FROM Event e", Event.class).getResultList();

    }

    public Event getEventById(int id) {
        EntityManager em = emf.createEntityManager();
        return em.find(Event.class, id);
    }

    public List<Event> getEventsByCategory(Integer categoryId) {
        String jpql = "SELECT e FROM Event e WHERE e.category.CategoryID = :categoryId";
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(jpql, Event.class)
                    .setParameter("categoryId", categoryId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Event> getEventByStatus(String status) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT e FROM Event e WHERE e.Status = :status";
            TypedQuery<Event> query = em.createQuery(jpql, Event.class);
            query.setParameter("status", status);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Event create(Event event) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(event);
        em.getTransaction().commit();
        return event;
    }
    public Event read(int id) {
        EntityManager em = emf.createEntityManager();
        return em.find(Event.class, id);
    }


    public Event update(Event event) {

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.merge(event);
        em.getTransaction().commit();
        return event;
    }


    public void delete(int id) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Event event = em.find(Event.class, id);
        em.remove(event);
        em.getTransaction().commit();
    }

    public void addUserToEvent(User user, Event createdEvent) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Event event = em.find(Event.class, createdEvent.getEventId());
        event.addUser(user);
        em.getTransaction().commit();
    }

    public void removeUserFromEvent(User user, Event createdEvent) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Event event = em.find(Event.class, createdEvent.getEventId());
        event.removeUser(user);
        em.getTransaction().commit();
    }


    public List<User> getRegistrationsForEventById(int eventId) {
        EntityManager em = emf.createEntityManager();
        try {

            String jpql = "SELECT DISTINCT u FROM User u JOIN FETCH u.roles JOIN FETCH u.events e WHERE e.EventId = :eventId";
            List<User> users = em.createQuery(jpql, User.class)
                    .setParameter("eventId", eventId)
                    .getResultList();
            return users;
        } finally {
            em.close();
        }
    }

    public long getRegistrationsCountById(int id) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(u) " +
                    "FROM User u " +
                    "JOIN u.events e " +
                    "WHERE e.id = :id", Long.class);
            query.setParameter("id", id);
            Long count = query.getSingleResult();
            return count;
        } catch (NoResultException e) {
            System.out.println(e);
            return 0; // If there's no such event, returning count as 0
        } finally {
            em.close();
        }
    }

    public void addUserToEvent(int userId, int eventId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            User user = em.find(User.class, userId);
            Event event = em.find(Event.class, eventId);

            if (user != null && event != null) {
                // Add the user to the event
                event.getUsers().add(user);

                // Also update the user's side of the relationship if it's bidirectional
                user.getEvents().add(event);
                em.merge(user);
                em.merge(event);
            } else {
                System.out.println("User or event not found");
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error adding user to event", e);
        } finally {
            em.close();
        }
    }



    public void removeUserEvent(int userId, int eventId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            User user = em.find(User.class, userId);
            Event event = em.find(Event.class, eventId);

            if (user != null && event != null) {

                event.getUsers().remove(user);
                user.getEvents().remove(event);

                em.merge(user);
                em.merge(event);
            } else {
                System.out.println("User or event not found");
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error removing user from event", e);
        } finally {
            em.close();
        }
    }
}

