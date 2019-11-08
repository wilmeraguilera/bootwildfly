package bootwildfly.app.controller;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bootwildfly.app.config.PropertiesValues;
import bootwildfly.app.service.CalculadoraService;

@RestController
public class CalculadoraController {

	@Autowired
	PropertiesValues configProps;
	
	AtomicInteger executeVal = new AtomicInteger(0);
	
	@Autowired
	CalculadoraService calculadoraService;
	
	/**
	 * http://localhost:8080/sumar?sumando1=1&sumando2=3
	 * @param sumando1
	 * @param sumando2
	 * @return
	 */
    @RequestMapping("sumar")
    public String sumar(@RequestParam Integer sumando1, @RequestParam Integer sumando2){
    	Integer resultado = 0;
    	Integer nroExecute = executeVal.incrementAndGet();
    	resultado = calculadoraService.sumar(sumando1, sumando2);
    	
        return ("Execución Nro: "+nroExecute+ "Resultado Suma: "+resultado);
    }
    
   
}