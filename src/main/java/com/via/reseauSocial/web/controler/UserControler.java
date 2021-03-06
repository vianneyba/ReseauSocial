package com.via.reseauSocial.web.controler;

import java.net.URI;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.via.reseauSocial.beans.User;
import com.via.reseauSocial.ctrl.UserCtrl;
import com.via.reseauSocial.dao.UserDao;

@RestController
public class UserControler {

	@Autowired
	private UserDao userDao;
	
	@GetMapping(value = "/users")
    public List<User> listUsers() {
       return userDao.findAll();
    }
	
	@GetMapping(value = "/users/{id}")
	public User viewUser(@PathVariable int id) {
	    return userDao.findById(id);
	}
	
	@GetMapping(value= "/users/sign-out")
	public ResponseEntity<String> signOut(HttpServletRequest request) {
		request.getSession().invalidate();
		System.out.println("deconnection OK!");
		return ResponseEntity
			.status(HttpStatus.OK)
			.body("Déconnexion Ok");
	}
	
	@PostMapping(value= "/users/sign-in")
	public User signIn(@RequestBody User user, HttpServletRequest request) {
		System.out.println(user);
		User newUser= userDao.findByEmailAndPassword(user.getEmail(), user.getPassword());
		if(newUser != null) {
			request.getSession().setAttribute("user", newUser);		
		} 
		return newUser;
	}
	
	@PostMapping(value = "/users/sign-up")
    public ResponseEntity<Void> signUp(@RequestBody User user, HttpServletRequest request) {
		ResponseEntity<Void> result= null;
		UserCtrl userCtrl= new UserCtrl();
		
		userCtrl.signUpCtrl(user, userDao);
		if(!userCtrl.isError()) {
			
			User newUser= userDao.save(user);
			request.getSession().setAttribute("user", newUser);
	
			final HttpHeaders headers = new HttpHeaders();
    		headers.add("id", String.valueOf(newUser.getId()));
            result= new ResponseEntity<Void>(headers, HttpStatus.CREATED);
		}else {
    		result= new ResponseEntity<Void>(HttpStatus.SERVICE_UNAVAILABLE);
    	}
		return result;
	}
	
	@PostMapping(value = "/users")
    public ResponseEntity<Void> addUser(@RequestBody User user) {
    	User newUser= userDao.save(user);
    	
    	if(newUser == null) {
    		return ResponseEntity.noContent().build();
    	}
    	
    	URI location= ServletUriComponentsBuilder
    			.fromCurrentRequest()
    			.path("/{id}")
    			.buildAndExpand(newUser.getId())
    			.toUri();
    	
    	return ResponseEntity.created(location).build();
    }
}
