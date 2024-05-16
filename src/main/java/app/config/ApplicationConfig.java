package app.config;

import app.Main;
import app.controllers.ISecurityController;
import app.controllers.SecurityController;
import app.dto.UserDTO;
import app.exceptions.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.HttpStatus;
import io.javalin.security.AccessManager;
import io.javalin.security.RouteRole;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ApplicationConfig {
    ObjectMapper om = new ObjectMapper();
    ISecurityController securityController = new SecurityController();
    private Javalin app;
    private static ApplicationConfig instance;
    private ApplicationConfig(){}
    public static ApplicationConfig getInstance(){
        if(instance == null){
            instance = new ApplicationConfig();
        }
        return instance;
    }
    public ApplicationConfig initiateServer(){
        app = Javalin.create(config->{
            config.http.defaultContentType = "application/json";
            config.routing.contextPath = "/api";
        });

        return instance;
    }

    public ApplicationConfig checkSecurityRoles() {
        // Check roles on the user (ctx.attribute("username") and compare with permittedRoles using securityController.authorize()
        app.updateConfig(config -> {

            config.accessManager((handler, ctx, permittedRoles) -> {
                // permitted roles are defined in the last arg to routes: get("/", ctx -> ctx.result("Hello World"), Role.ANYONE);

                Set<String> allowedRoles = permittedRoles.stream().map(role -> role.toString().toUpperCase()).collect(Collectors.toSet());
                if(allowedRoles.contains("ANYONE") || ctx.method().toString().equals("OPTIONS")) {
                    // Allow requests from anyone and OPTIONS requests (preflight in CORS)
                    handler.handle(ctx);
                    return;
                }

                UserDTO user = ctx.attribute("user");
                System.out.println("USER IN CHECK_SEC_ROLES: "+user);
                if(user == null)
                    ctx.status(HttpStatus.FORBIDDEN)
                            .json(om.createObjectNode()
                                    .put("msg","Not authorized. No username were added from the token"));

                if (securityController.authorize(user, allowedRoles))
                    handler.handle(ctx);
                else
                    throw new ApiException(HttpStatus.FORBIDDEN.getCode(), "Unauthorized with roles: "+allowedRoles);
            });
        });
        return instance;
    }
    public ApplicationConfig startServer(int port){
        app.start(port);
        return instance;
    }
    public ApplicationConfig setRoute(EndpointGroup route){
        app.routes(route);
        return instance;
    }

    public ApplicationConfig setExceptionHandling(){
        app.exception(Exception.class, (e,ctx)->{
            ObjectNode node = om.createObjectNode().put("errorMessage",e.getMessage());
            ctx.status(500).json(node);
        });
        return instance;
    }

    public ApplicationConfig setupAccessManager() {
        AccessManager accessManager = (handler, ctx, permittedRoles) -> {
            // Check if the route is accessible to anyone without authentication
            if (permittedRoles.contains(Main.Role.ANYONE)) {
                handler.handle(ctx);
                return;
            }

            // For all other roles, check the Authorization header for a token
            String token = ctx.header("Authorization");

            if (token != null && !token.startsWith("Bearer ")) {
                ctx.status(401).result("Unauthorized: No Bearer Token Found");
                return;
            }

            try {
                // Extract the token without the "Bearer " prefix
                //token = token.substring(7);
                token= Arrays.toString(token.split(", "));
                UserDTO user = securityController.verifyToken(token);

                // Check if the user has one of the required roles
                if (userHasRequiredRole(user, permittedRoles)) {
                    ctx.attribute("user", user); // Add the user info to the context
                    handler.handle(ctx);
                } else {
                    ctx.status(403).result("Forbidden: User does not have the required role");
                }
            } catch (Exception e) {
                // Log the exception or handle it more specifically if needed
                ctx.status(401).result("Unauthorized: Invalid Token");
            }
        };

        return this;
    }
    private boolean userHasRequiredRole(UserDTO user, Set<? extends RouteRole> permittedRoles) {
        if (user.getRoles() == null) {
            return false;
        }
        return permittedRoles.stream().anyMatch(role -> user.getRoles().contains(role.toString()));
    }
    public void stopServer(){
        app.stop();
    }

    //public enum Role implements RouteRole { ANYONE, USER, ADMIN }

    public ApplicationConfig configureCors() {
        app.before(ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "Content-Type");
        });

        app.options("/*", ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "Content-Type");
        });

        return this;
    }
}
