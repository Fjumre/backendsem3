package app.controllers;

import app.config.HibernateConfig;
import app.dao.UserDAO;
import app.dto.UserDTO;
import app.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.http.Handler;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserController implements IUserController{

    UserDAO userDAO = new UserDAO(HibernateConfig.getEntityManagerFactory());
    ObjectMapper objectMapper = new ObjectMapper();
    //UserDTO userDTO= new UserDTO();
   // User user= new User();
    EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();


    public UserController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
    private UserDTO convertToUserDTO(User user) {
        return new UserDTO(user);
    }
    @Override
    public Handler getAllUsers() {
        return (ctx) -> {
            try {
                // Fetch all users from the database
                List<User> users = userDAO.getAlleUser();

                // Convert each User to UserDTO
                List<UserDTO> userDTOs = users.stream()
                        .map(this::convertToUserDTO)
                        .collect(Collectors.toList());


                ctx.json(userDTOs);
            } catch (Exception e) {
                ctx.status(500);
                ctx.json(Map.of("msg", "Internal server error"));
                System.out.println(e);
            }
        };
    }


    @Override
    public Handler createUser() {
        return (ctx) -> {
            UserDTO userDTO = ctx.bodyAsClass(UserDTO.class);

            try {
                User newUser = userDAO.createUser(userDTO.getName(), userDTO.getPassword(), userDTO.getEmail(), userDTO.getPhoneNumber());
                ctx.status(201);
                ctx.json(newUser);
            } catch (Exception e) {

                ctx.status(500); // Internal Server Error
                ctx.json("Internal server error");
            }
        };
    }

    @Override
    public Handler getUserById() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                User userById= userDAO.getUserById(id);
                UserDTO userDTO= convertToUserDTO(userById);
                ctx.json(userDTO);
            } catch (Exception e) {
                ctx.status(500);
                System.out.println(e);
                ctx.json(returnObject.put("msg", "Internal server error"));
            }
        };
    }

    @Override
    public Handler updateUser() {
        return (ctx) -> {

            UserDTO userDTO = ctx.bodyAsClass(UserDTO.class);

            int userId = Integer.parseInt(ctx.pathParam("id"));

            try {
                User userToUpdate = userDAO.getUserById(userId);


                userToUpdate.setName(userDTO.getName());
                userToUpdate.setEmail(userDTO.getEmail());
                userToUpdate.setPassword(userDTO.getPassword());
                userToUpdate.setPhoneNumber(userDTO.getPhoneNumber());


                User updatedUser = userDAO.update(userToUpdate);
                ctx.json(updatedUser);
            }catch (Exception e) {

                ctx.status(500);
                ctx.json("Internal server error");
            }
        };
    }

    @Override
    public Handler deleteUser() {
        return (ctx) -> {

            try {
                int userId = Integer.parseInt(ctx.pathParam("id"));
                userDAO.deleteUser(userId);

                ctx.json(204);
            } catch (Exception e) {
                ctx.status(500);
                System.out.println(e);
                ctx.json("Internal server error");
            }
        };
    }

    @Override
    public Handler logout() {
        return (ctx) -> {
            ctx.req().getSession().invalidate();
            ctx.redirect("/");
        };
    }



}
