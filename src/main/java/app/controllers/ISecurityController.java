package app.controllers;

import app.dto.UserDTO;
import io.javalin.http.Handler;

import java.util.Set;

public interface ISecurityController {
    Handler register();

    Handler login();

    String createToken(UserDTO user);

    boolean authorize(UserDTO user, Set<String> allowedRoles);

    Handler authenticate();
    UserDTO verifyToken(String token);

    Handler resetOfPassword();

}
