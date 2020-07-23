package com.example.demo;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.websocket.server.PathParam;

@RestController
public class UserController {
	
	UserRepository database;
	
	public UserController(UserRepository database) {
		this.database=database;
	}
	
	@GetMapping("/users")
	public Iterable<User> _1(){
		Iterable<User> data = database.findAll();
		data.forEach((item)->{item.stripPassword();}); //Hide passwords.
		return data;
	}
	@GetMapping("/users/{id}")
	public User _1(@PathVariable Long id){
		return database.findById(id).orElse(new User()).stripPassword();
	}
	@PatchMapping("/users/{id}")
	public ResponseEntity<User> _1(@PathVariable Long id,
			@RequestBody User user){
		Optional<User> u = database.findById(id);
		if (u.isPresent()) {
			User u2 = u.get();
			u2.patchProperties(user);
			u2.id=id; //We're not allowed to patch the Id. Set it back.
			return ResponseEntity.ok(database.save(u2).stripPassword());
		} else {
			return ResponseEntity.badRequest().body(null);
		}
	}
	@DeleteMapping("/users/{id}")
	public UserCount _2(@PathVariable Long id){
		if (database.existsById(id)) {
			database.deleteById(id);
		}
		return new UserCount().set(database.count());
	}
	@PostMapping("/users/authenticate")
	public AuthenticationStatus _2(@RequestBody User user){
		List<User> u = database.findByPasswordAndEmail(user.password, user.email);
		return new AuthenticationStatus((u.size()>0),(u.size()>0)?u.get(0):null).filter();
	}
			
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@RequestMapping
	static class AuthenticationStatus{
		boolean authenticated;
		User user;
		
		AuthenticationStatus(boolean auth,User u) {
			this.authenticated=auth;
			this.user=u;
		}
		
		public User getUser() {
			return user;
		}
		public void setUser(User user) {
			this.user = user;
		}
		public AuthenticationStatus filter() {
			if (authenticated) {
				user.stripPassword();
				return this;
			} else {
				user=null; //Strip the user object entirely. Not authenticated.
				return this;
			}
		}

		public boolean isAuthenticated() {
			return authenticated;
		}

		public void setAuthenticated(boolean authenticated) {
			this.authenticated = authenticated;
		}
	}
	
	@RequestMapping
	static class UserCount{
		Long count;

		public Long getCount() {
			return count;
		}

		public void setCount(Long count) {
			this.count = count;
		}
		
		public UserCount set(Long count) {
			setCount(count);
			return this;
		}
	}
	
	@PostMapping("/users")
	public ResponseEntity<User> _1(@RequestBody User user){
		User u = user;
		if (u.email!=null && u.password!=null && u.id==null/*Cannot specify an id. This is POST'ing only.*/) {
			database.save(u);
			return ResponseEntity.ok(u.stripPassword());
		} else {
			return ResponseEntity.badRequest().body(null);
		}
	}
	
	public static void downloadFileFromUrl(String url, String file) throws IOException{
		  File filer = new File(file);
		  filer.createNewFile();
		  
		  URL website = new URL(url);
		  HttpURLConnection connection = (HttpURLConnection) website.openConnection();
		    /*for (String s : connection.getHeaderFields().keySet()) {
		    	System.out.println(s+": "+connection.getHeaderFields().get(s));
		    }*/
		    connection.setRequestMethod("GET");
		    //connection.setRequestProperty("Content-Type", "application/json");
		    try {
			  ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
			  FileOutputStream fos = new FileOutputStream(file);
			  fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			  fos.close();
		    } catch (ConnectException e) {
		    	System.out.println("Failed to connect, moving on...");
		    }
	  }
	
	public double CalculateArea(double width,double height) {
		return width*height;
	}
	
	public double CalculateArea(double radius) {
		return Math.PI*Math.pow(radius, 2);
	}

	@PostMapping("/math/area")
	public String areaDisplay(
			@RequestParam Map<String,String> map) {
		if (map.containsKey("type")) {
			if (map.get("type").equalsIgnoreCase("circle")) {
				if (map.containsKey("radius")) {
					return new StringBuilder("Area of a circle with a radius of ")
						.append(map.get("radius"))
						.append(" is "+CalculateArea(Double.parseDouble(map.get("radius"))))
						.toString();
				}
			}else if(map.get("type").equalsIgnoreCase("rectangle")) {
				if (map.containsKey("width") && map.containsKey("height")) {
					return new StringBuilder("Area of a ")
							.append(map.get("width")).append("x")
							.append(map.get("height"))
							.append(" rectangle is "+CalculateArea(Double.parseDouble(map.get("width")),Double.parseDouble(map.get("height"))))
							.toString();
				}
			}
		}
		return "Invalid";
	}
	
	@GetMapping("/math/volume/{l}/{w}/{h}")
	public String volumeDisplay(
			@PathVariable(value="l") String length,
			@PathVariable(value="w") String width,
			@PathVariable(value="h") String height) {
		return new StringBuilder("The volume of a ")
				.append(length).append("x")
				.append(width).append("x")
				.append(height)
				.append(" rectangle is ")
				.append(Integer.parseInt(length)*Integer.parseInt(width)*Integer.parseInt(height))
				.toString();
	}
	
	@GetMapping("/math/calculate")
	public String piDisplay(@RequestParam(value="operation",required=false) String operation,
			@RequestParam(value="x") String x,
			@RequestParam(value="y") String y) {
		int val1=Integer.parseInt(x);
		int val2=Integer.parseInt(y);
		switch (operation) {
			case "subtract":{
				return Integer.toString(val1-val2);
			}
			case "multiply":{
				return Integer.toString(val1*val2);
			}
			case "divide":{
				return Integer.toString(val1/val2);
			}
			default:{
				return Integer.toString(val1+val2);
			}
		}
	}
	
	@PostMapping("/math/sum")
	public String sumDisplay(@RequestParam Map<String,String> keys) {
		int sum = 0;
		for (String i : keys.keySet()) {
			sum += Integer.parseInt(keys.get(i));
		}
		return Integer.toString(sum);
	}
	
	@GetMapping("/math/pi")
	public String piDisplay() {
		return Double.toString(Math.PI);
	}

    @GetMapping("/image")
    public HashMap<String,String> helloWorld(@RequestParam("url") String url){
		try {
			downloadFileFromUrl("http://pbs.twimg.com/media/EdKE8xzVcCEf1qd.jpg","temp");
			BufferedImage img = ImageIO.read(new File("temp"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
    }
}