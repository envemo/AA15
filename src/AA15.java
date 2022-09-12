import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

public class AA15 {
	
	public static void main(String args[]) {
		
		Scanner sc = new Scanner(System.in);
		
		System.out.println("ID: ");
		
		String id = sc.next();
		
		System.out.println("Nombre: ");
		
		String nombre = sc.next();
		
		System.out.println("Apellido: ");
		
		String apellido = sc.next();
		
		sc.close();
		
		Usuario usuario = new Usuario(id, nombre, apellido);
		
		Login login = new Login(usuario, LocalDate.now());
		
		Programa programa = new Programa(login, extraerCapitales());
		
		programa.imprimirLineas();
		
		programa.generarArchivo();
		
		programa.generarJenkins();
		
	}

	private static List<Capital> extraerCapitales() {
		
		String api = "https://public.opendatasoft.com/api/records/1.0/search/?dataset=provincias-espanolas&q=&rows=0&sort=-provincia&facet=provincia";
		
		URL url;
		try {
			url = new URL (api);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
		
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.connect();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		int tiempoRespuesta;
		try {
			tiempoRespuesta = conn.getResponseCode();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		if(tiempoRespuesta != 200)
		{
			throw new RuntimeException("HttpResponse" + tiempoRespuesta);
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			Scanner sc;
			try {
				sc = new Scanner(url.openStream());
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			while(sc.hasNext())
			{
				sb.append(sc.nextLine());
			}
			sc.close();
			JSONObject jsonObject = new JSONObject(sb.toString());
			JSONArray jsonArray = jsonObject.getJSONArray("facet_groups");
			//jsonArray = jsonObject.getJSONArray("facets");
			
			for(Object object : jsonArray) {
				jsonObject = (JSONObject) object;
				jsonArray = jsonObject.getJSONArray("facets");
			}
			
			List<Capital> capitales = new ArrayList<Capital>();
			
			for(Object object : jsonArray) {
				jsonObject = (JSONObject) object;
				capitales.add(new Capital(jsonObject.getString("name"), new Provincia(jsonObject.getString("path"))));
			}
			
			return capitales;
			
		}
		
	}

}

class Provincia {
	
	private String nombre;

	public Provincia(String nombre) {
		super();
		this.nombre = nombre;
	}

	public String getNombre() {
		return nombre;
	}

}

class Capital {
	
	private String nombre;
	
	private Provincia provincia;

	public Capital(String nombre, Provincia provincia) {
		super();
		this.nombre = nombre;
		this.provincia = provincia;
	}

	public String getNombre() {
		return nombre;
	}

	public Provincia getProvincia() {
		return provincia;
	}
	
	@Override
	public String toString() {
		return "- Capital: " + nombre + ", Provincia: " + provincia.getNombre();
	}
	
}

class Usuario {
	
	private String id;
	
	private String nombre;
	
	private String apellido;

	public Usuario(String id, String nombre, String apellido) {
		super();
		this.id = id;
		this.nombre = nombre;
		this.apellido = apellido;
	}

	public String getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public String getApellido() {
		return apellido;
	}
	
}

class Login {
	
	private Usuario usuario;
	
	private LocalDate fecha;

	public Login(Usuario usuario, LocalDate fecha) {
		super();
		this.usuario = usuario;
		this.fecha = fecha;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public LocalDate getFecha() {
		return fecha;
	}
	
	@Override
	public String toString() {
		return "ID_USUARIO: " + usuario.getId() + " Nombre usuario: " + usuario.getApellido() + ", " + usuario.getNombre() + " Fecha: " + fecha.toString();
	}
	
}

class Programa implements generar {
	
	private Login login;
	
	private List<Capital> capitales;

	public Programa(Login login, List<Capital> capitales) {
		super();
		this.login = login;
		this.capitales = capitales;
	}
	
	private List<String> lineas(){
		
		List<String> lineas = new ArrayList<String>();
		
		lineas.add(login.toString());
		
		for (Capital capital : capitales) {
			
			lineas.add(capital.toString());
			
		}
		
		return lineas;
		
	}
	
	public void imprimirLineas() {
		
		for (String linea : lineas()) {
			System.out.println(linea);
		}
		
	}

	@Override
	public void generarArchivo() {
		
		String nombreArchivo = "capitalesProvincias.txt";

		try
		{
			Path file = Paths.get(nombreArchivo);
			Files.write(file, lineas(), StandardCharsets.UTF_8);
		}
		catch (IOException e)
		{
			System.out.println("Ocurrió un error");
			e.printStackTrace();
		}
		
	}

	@Override
	public void generarJenkins() {

		List<String> lineas = new ArrayList<String>();
		lineas.add("pipeline {");
		lineas.add("	agent any");
		lineas.add("	stages {");
		lineas.add("		stage ('HolaMundo') {");
		lineas.add("			steps {");
		for (String linea : lineas()) {
			lineas.add("				echo \"" + linea + "\"");
		}
		lineas.add("			}");
		lineas.add("		}");
		lineas.add("	}");
		lineas.add("}");
		
		String nombreArchivo = "Jenkinsfile";

		try
		{
			Path file = Paths.get(nombreArchivo);
			Files.write(file, lineas, StandardCharsets.UTF_8);
		}
		catch (IOException e)
		{
			System.out.println("Ocurrió un error");
			e.printStackTrace();
		}
		
	}
	
}

interface generar {
	
	void generarArchivo();
	void generarJenkins();
	
}