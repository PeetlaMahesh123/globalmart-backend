package com.kodnest.learn.admincontrollers;

import com.kodnest.learn.entity.User;
import com.kodnest.learn.adminservices.AdminUserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }


    // ✅ MODIFY USER (PUT)
    @PutMapping("/modify")
    public ResponseEntity<?> modifyUser(
            @RequestBody Map<String, Object> userRequest) {

        try {

            Integer userId =
                    Integer.parseInt(userRequest.get("userId").toString());

            String username =
                    userRequest.get("username").toString();

            String email =
                    userRequest.get("email").toString();

            String role =
                    userRequest.get("role").toString();


            User updatedUser =
                    adminUserService.modifyUser(
                            userId,
                            username,
                            email,
                            role
                    );


            Map<String, Object> response = new HashMap<>();

            response.put("userId",
                    updatedUser.getUserId());

            response.put("username",
                    updatedUser.getUsername());

            response.put("email",
                    updatedUser.getEmail());

            response.put("role",
                    updatedUser.getRole().name());

            response.put("createdAt",
                    updatedUser.getCreatedAt());

            response.put("updatedAt",
                    updatedUser.getUpdatedAt());


            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(response);

        }
        catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());

        }
        catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");

        }

    }



    // ✅ GET USER BY ID (CORRECT FIX)
    @GetMapping("/getbyid")
    public ResponseEntity<?> getUserById(
            @RequestParam Integer userId) {

        try {

            User user =
                    adminUserService.getUserById(userId);

            Map<String, Object> response = new HashMap<>();

            response.put("userId",
                    user.getUserId());

            response.put("username",
                    user.getUsername());

            response.put("email",
                    user.getEmail());

            response.put("role",
                    user.getRole().name());

            response.put("createdAt",
                    user.getCreatedAt());

            response.put("updatedAt",
                    user.getUpdatedAt());


            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(response);

        }
        catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());

        }
        catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");

        }

    }

}