// Admin Controller for Modify User functionality
package com.kodnest.learn.admincontrollers;
import com.kodnest.learn.entity.User;
import com.kodnest.learn.adminservices.AdminUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
    @CrossOrigin(
    origins = {
        "http://localhost:5173",
        "https://zippy-parfait-f89cac.netlify.app"
    },
    allowCredentials = "true"
)
@RequestMapping("/admin/user")
public class AdminUserController {
    private final AdminUserService adminUserService;
    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @PutMapping("/modify")
    public ResponseEntity<?> modifyUser(@RequestBody Map<String, Object> userRequest) {
    	 try {
             Integer userId = (Integer) userRequest.get("userId");
             String username = (String) userRequest.get("username");
             String email = (String) userRequest.get("email");
             String role = (String) userRequest.get("role");
             User updatedUser = adminUserService.modifyUser(userId, username, email, role);
             Map<String, Object> response = new HashMap<>();
             response.put("userId", updatedUser.getUserId());
             response.put("username", updatedUser.getUsername());
             response.put("email", updatedUser.getEmail());
             response.put("role", updatedUser.getRole().name());
             response.put("createdAt", updatedUser.getCreatedAt());
             response.put("updatedAt", updatedUser.getUpdatedAt());
             return ResponseEntity.status(HttpStatus.OK).body(response);
         } catch (IllegalArgumentException e) {
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
         } catch (Exception e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
         }
    }

    @GetMapping("/getbyid")
    public ResponseEntity<?> getUserById(@RequestBody Map<String, Integer> userRequest) {
    	 try {
             Integer userId = userRequest.get("userId");
             User user = adminUserService.getUserById(userId);
             return ResponseEntity.status(HttpStatus.OK).body(user);
         } catch (IllegalArgumentException e) {
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
         } catch (Exception e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
         }
     }
}
