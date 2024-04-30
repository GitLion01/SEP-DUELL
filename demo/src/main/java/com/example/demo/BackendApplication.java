package com.example.demo;

import org.jsoup.Jsoup;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.text.Document;
import java.io.File;
import java.io.IOException;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {

		SpringApplication.run(BackendApplication.class, args);
		try {
			// Pfad zur HTML-Datei angeben
			File input = new File("templates/login.html");

			// HTML-Datei mit Jsoup parsen
			Document doc = (Document) Jsoup.parse(input, "UTF-8", "");

			// Den Inhalt der HTML-Datei ausgeben
			System.out.println(doc);
		} catch (IOException e) {
			System.out.println("error");
		}
	}

}
