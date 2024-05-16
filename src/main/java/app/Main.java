package app;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.controllers.*;
import app.dao.EventDAO;
import app.dao.UserDAO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.security.RouteRole;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.get;

public class Main {

    private static ISecurityController securityController = new SecurityController();

    static EntityManagerFactory emf =HibernateConfig.getEntityManagerFactory();
    private static EventDAO eventDAO= new EventDAO(emf);
    private static IEventController eventController= new EventController(eventDAO);

    private static UserDAO userDAO= new UserDAO(emf);
    private static IUserController userController= new UserController(userDAO);
    private static ObjectMapper om = new ObjectMapper();

    public static void main(String[] args) {

        startServer(7070);

    }

    public static void startServer(int port){

        ObjectMapper om = new ObjectMapper();
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        ApplicationConfig applicationConfig = ApplicationConfig.getInstance();
        applicationConfig
                .initiateServer()
                .startServer(7070)
                .setExceptionHandling()
                .setupAccessManager()
                .configureCors()
                .setRoute(getSecurityRoutes())
                .setRoute(getSecuredRoutes())
                .setRoute(() -> {

                    getUserRoutes();
                    getRoutes();

                })
                .checkSecurityRoles();
    }

    public static void getRoutes(){
        before(securityController.authenticate());
        path("/events", () -> {
            path("/", () -> {
                before(securityController.authenticate());
                get("/", eventController.getAllEvents(), Role.ANYONE);
                get("{id}", eventController.getEventById(), Role.ANYONE);
                post("create", eventController.createEvent(), Role.INSTRUCTOR, Role.ADMIN);
                put("update/{id}", eventController.updateEvent(), Role.INSTRUCTOR, Role.ADMIN);
                delete("delete/{id}", eventController.deleteEvent(), Role.INSTRUCTOR, Role.ADMIN);
                get("allregistrations/{event_id}", eventController.getAllRegistrationsForEvent(), Role.INSTRUCTOR, Role.ADMIN);
                get("registration/{event_id}", eventController.getRegistrationById(), Role.INSTRUCTOR, Role.ADMIN);
                put("registrations/{event_id}", eventController.getRegistrationById(), Role.INSTRUCTOR, Role.ADMIN);
                post("eventregistration/{event_id}", eventController.registerUserForEvent(), Role.USER, Role.INSTRUCTOR, Role.ADMIN);
                post("removeuserevent/{event_id}", eventController.removeUserFromEvent(), Role.USER, Role.INSTRUCTOR, Role.ADMIN);
                get("eventsbycategory/{category_id}", eventController.getAllEventsByCategory(), Role.ANYONE);
                get("eventsbystatus/{status}", eventController.getAllEventsByStatus(), Role.ANYONE);
                get("error", ctx -> {
                    throw new Exception(String.valueOf(ApplicationConfig.getInstance().setExceptionHandling()));
                });
            });
    });
    }

    public static void getUserRoutes(){
        before(securityController.authenticate());
        path("/user", () -> {
            path("/", () -> {
            before(securityController.authenticate());
            get("/all", userController.getAllUsers(), Role.ADMIN);
            get("/{id}", userController.getUserById(), Role.ADMIN);
            post("/create", userController.createUser(), Role.ADMIN, Role.INSTRUCTOR);
            put("/update/{id}", userController.updateUser(), Role.USER, Role.INSTRUCTOR, Role.ADMIN);
            delete("/delete/{id}", userController.deleteUser(), Role.USER, Role.INSTRUCTOR, Role.ADMIN);
            post("/logout", userController.logout(), Role.USER, Role.ADMIN, Role.INSTRUCTOR);
            get("/error", ctx -> {
                throw new Exception(String.valueOf(ApplicationConfig.getInstance().setExceptionHandling()));
            });
        });
    });
    }


    public static void closeServer () {
        ApplicationConfig.getInstance().stopServer();
    }


    public static EndpointGroup getSecurityRoutes() {
        return ()->{
            path("/auth", ()->{
                post("/login", securityController.login(),Role.ANYONE);
                post("/register", securityController.register(),Role.ANYONE);
                post("/resetpassword", securityController.resetOfPassword(), Role.ANYONE);
            });
        };
    }

    public static EndpointGroup getSecuredRoutes(){
        return ()->{
            path("/protected", ()->{
                before(securityController.authenticate());
                get("/user",(ctx)->ctx.json(om.createObjectNode().put("msg",  "Hello from USER Protected")),Role.USER);
                get("/instructor",(ctx)->ctx.json(om.createObjectNode().put("msg",  "Hello from INSTRUCTOR Protected")),Role.INSTRUCTOR);
                get("/admin",(ctx)->ctx.json(om.createObjectNode().put("msg",  "Hello from ADMIN Protected")),Role.ADMIN);
            });
        };
    }

    public enum Role implements RouteRole {
        ANYONE,
        USER,
        ADMIN,
        INSTRUCTOR

    }
}