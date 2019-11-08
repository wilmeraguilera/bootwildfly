package bootwildfly.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import bootwildfly.app.config.PropertiesValues;

@RestController
public class HelloWorldController {
	
	@Autowired
	PropertiesValues configProps;
    
    /**
     * http://localhost:8080/hello
     * @return
     */
    @GetMapping("/hello")
    public String hello(){
    	System.out.println("Enviroment: "+configProps.getEnviroment());
        return ("Hello World! - Enviroment: "+configProps.getEnviroment());
    }
}