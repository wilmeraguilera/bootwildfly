package bootwildfly.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration

@PropertySource( value = {"classpath:application.properties"},  ignoreResourceNotFound = true)
@PropertySource( value = {"file:/deployments/config/application.properties"},  ignoreResourceNotFound = true)
public class ConfigProperties {
	
	@Autowired
	Environment env;

	@Bean
	public PropertiesValues getPropertiesValues() {
		PropertiesValues propertiesValues = new PropertiesValues();
		propertiesValues.setEnviroment(env.getProperty("enviroment"));
		return propertiesValues;
		
	}
	

}
