package bootwildfly.app.service;

import org.springframework.stereotype.Service;

@Service
public class CalculadoraService {
	
	/**
	 * Metodo encargado de sumar 2 valores
	 * @param sumando1
	 * @param sumando2
	 * @return
	 */
	public Integer sumar(Integer sumando1, Integer sumando2) {
		Integer resultado = sumando1 + sumando2;
		
		if(resultado.intValue()==6) {
			System.out.println("El resultado es 6");
		}
		return resultado;
	}
	
	
		
}
