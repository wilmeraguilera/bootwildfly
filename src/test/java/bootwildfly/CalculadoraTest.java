package bootwildfly;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import bootwildfly.app.Application;
import bootwildfly.app.controller.CalculadoraController;
import bootwildfly.app.service.CalculadoraService;



@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("classpath:application.properties")
@ContextConfiguration(classes = {Application.class})
public class CalculadoraTest {
	
	@Autowired
	CalculadoraService calculadoraService;
	
	@Autowired
	CalculadoraController calculadoraController;
	
	@Before
	public void setUp() {
		
		
    }
	
	@Test
	public void sumarPositivos() {
		Integer resul =0;
		resul = calculadoraService.sumar(2, 4);
		
		assertEquals(new Integer(6), resul);
	}
	
	@Test
	public void sumarNegativos() {
		Integer resul =0;
		resul = calculadoraService.sumar(-2, -3);
		
		assertEquals(new Integer(-5), resul);
	}
	
	@Test
	public void sumarNegativoYPositivo() {
		Integer resul =0;
		resul = calculadoraService.sumar(-2, 3);
		
		assertEquals(new Integer(1), resul);
	}

	
	

}
