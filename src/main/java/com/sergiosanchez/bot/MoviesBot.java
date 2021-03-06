package com.sergiosanchez.bot;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import com.sergiosanchez.configuration.Config;
import com.sergiosanchez.connections.Downloader;
import com.sergiosanchez.connections.Library;
import com.sergiosanchez.connections.MoviesAPI;
import com.sergiosanchez.movies.Cast;
import com.sergiosanchez.movies.Movie;
import com.sergiosanchez.utils.Util;
import com.vdurmont.emoji.EmojiParser;

/**
 * Se encarga de gestionar las distintas peticiones del chat así como de
 * devolver los distintos tipos de respuesta usando el resto de clases del
 * paquete connections
 * 
 * @author Sergio Sanchez
 *
 */
public class MoviesBot extends TelegramLongPollingBot {

	// Variables que almacenan el estado de las busquedas
	public static ArrayList<Movie> movies = new ArrayList<Movie>();
	public static Movie movieSeleccionada;
	public static ArrayList<String> listaOpciones = new ArrayList<String>();
	public static String busqueda;
	String contacto = "";
	public static String[] UsuariosLista = Config.getUSERSLIST();

	@Override
	public String getBotUsername() {
		return Config.getBOTNAME();
	}

	@Override
	public void onUpdateReceived(Update update) {

		// Creacion del mensaje
		SendMessage message = new SendMessage();

		// Borra los teclados de optiones en caso de que estén abiertos
		ReplyKeyboardRemove keyboardremove = new ReplyKeyboardRemove();
		keyboardremove.setSelective(true);
		message.setReplyMarkup(keyboardremove);

		// Recoge el Id del chat
		message.setChatId(update.getMessage().getChatId());

		// Formato de texto para poder poner negrita
		message.enableMarkdown(true);
		message.setParseMode("Markdown");

		//Guarda el contacto si lo ha enviado el usuario
		try {
			System.out.println("Contacto: " + update.getMessage().getContact().getPhoneNumber());
			contacto = update.getMessage().getContact().getPhoneNumber();
		} catch (Exception e) {
		}

		System.out.println("Variable contacto: " + contacto);

		//Si hay mensaje
		if (update.hasMessage() && update.getMessage().hasText()) {

			//Autoriza al contacto
			for (String usuario : UsuariosLista) {
				
				if (contacto.equals(usuario)) {

					// Obtiene una lista de películas del año corriente (en pruebas)
					if (update.getMessage().getText().startsWith("Recomiendame")) {

						String mensaje = ":first_place_medal: Te recomiendo estas últimas películas :movie_camera::\n\n";

						int currentYear = Calendar.getInstance().get(Calendar.YEAR);

						for (Movie movie : MoviesAPI.getMovies("https://api.themoviedb.org/3/discover/movie?api_key="
								+ Config.getAPIKEY() + "&language=es-ES&primary_release_year=" + currentYear)) {
							mensaje = mensaje + " - " + movie.getName() + "\n";
						}

						message.setText(EmojiParser.parseToUnicode(mensaje));

						// Busca y obtiene una lista de peliculas
					} else if (update.getMessage().getText().contains("Busca ")
							|| update.getMessage().getText().equals("Enseñame la lista otra vez")) {

						movies = new ArrayList<Movie>();
						movieSeleccionada = null;
						listaOpciones = new ArrayList<String>();
						ArrayList<String> keyboardButtons = new ArrayList<String>();

						if (update.getMessage().getText().equals("Enseñame la lista otra vez")) {
							// No es necesario buscar en el String otra vez porque
							// ya
							// tenemos la busqueda almacenada
						} else {
							// Buscamos en el mensaje la película
							busqueda = update.getMessage().getText().substring(6, update.getMessage().getText().length());
						}

						try {
							movies = Downloader.searchMovie(busqueda, Config.getDOMAIN());
							int contador = 1;

							if (movies.size() != 0) {
								ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();

								for (Movie movie : movies) {

									keyboardButtons
											.add(contador + ". " + movie.getName() + " (" + movie.getQuality() + ")");
									listaOpciones.add(contador + ". " + movie.getName() + " (" + movie.getQuality() + ")");
									contador++;
								}

								List<KeyboardRow> list = Util.generateKeyboard(keyboardButtons, true);

								keyboard.setKeyboard(list);
								message.setReplyMarkup(keyboard);

								String smile_emoji = EmojiParser.parseToUnicode("Aqui tienes los resultados "
										+ update.getMessage().getChat().getFirstName() + " :smiley:");
								message.setText(smile_emoji);
							} else {
								message.setText("No se han encontrado resultados");
							}

						} catch (MalformedURLException e) {
							message.setText(
									"La página de la que obtenemos la información de las películas no se encuentra disponible en este momento, intentalo más tarde.");
						}

						// Añade la pelicula a la libreria
					} else if (update.getMessage().getText().equals("Añadir a mi biblioteca")) {

						String mensaje;

						try {
							// Obtiene la url de la pelicula seleccionada
							String urlMovie = Downloader.getMovie(Config.getIPADDRESS(), movieSeleccionada.getUrl(),
									Config.getDOMAIN());
							// Añade la película a la librería en base a su URL
							Library.addFile(Config.getIPADDRESS(), "http://" + Config.getDOMAIN() + "" + urlMovie);

							mensaje = "La película " + movieSeleccionada.getName()
									+ " se ha enviado a tu biblioteca :file_folder:";

						} catch (Exception e) {
							mensaje = "Ha habido un error al intentar añadir " + movieSeleccionada.getName()
									+ " a la biblioteca";
						}

						message.setText(mensaje);

						// Elimina la información de las variables
						Util.deleteData();

					} else if (update.getMessage().getText().equals("Ver Sinopsis")) {

						ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();

						// Coge la descripcion del primer resultado de la búsqueda
						message.setText("*Sinopsis*\n\n" + movieSeleccionada.getDescription());

						// Genera un teclado de opciones
						ArrayList<String> optionsKeyboard = new ArrayList<String>();
						optionsKeyboard.add("Añadir a mi biblioteca");
						optionsKeyboard.add("Ver Sinopsis");
						optionsKeyboard.add("Ver Casting");
						optionsKeyboard.add("Enseñame la lista otra vez");
						optionsKeyboard.add("Cancelar");
						keyboard.setKeyboard(Util.generateKeyboard(optionsKeyboard, false));
						message.setReplyMarkup(keyboard);

					} else if (update.getMessage().getText().equals("Ver Casting")) {

						ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
						String mensaje = "*Casting*:\n\n";

						for (Cast cast : movieSeleccionada.getCasting()) {
							mensaje = mensaje + " - " + cast.getActor() + " como " + cast.getCharacter() + "\n";
						}

						// Coge la descripcion del primer resultado de la búsqueda
						message.setText(mensaje);

						// Genera un teclado de opciones
						ArrayList<String> optionsKeyboard = new ArrayList<String>();
						optionsKeyboard.add("Añadir a mi biblioteca");
						optionsKeyboard.add("Ver Sinopsis");
						optionsKeyboard.add("Ver Casting");
						optionsKeyboard.add("Enseñame la lista otra vez");
						optionsKeyboard.add("Cancelar");
						keyboard.setKeyboard(Util.generateKeyboard(optionsKeyboard, false));
						message.setReplyMarkup(keyboard);

					} else if (update.getMessage().getText().equals("Cancelar")) {

						message.setText("Vale " + update.getMessage().getChat().getFirstName() + "!");

						// Elimina la información de las variables
						Util.deleteData();

					} else if (update.getMessage().getText().equals("Dime el estado")) {

						String estado = null;
						int init = 0;
						String mensaje = "";
						String estadoTorrent = "";

						JSONObject jObject;
						try {
							jObject = new JSONObject(Library.getInfo(Config.getIPADDRESS()));
							JSONArray torrents = jObject.getJSONArray("torrents");
							mensaje = "Aquí tienes el estado de tu biblioteca:\n\n";

							if (torrents.length() != 0) {
								for (int i = 0; i < torrents.length(); i++) {
									JSONArray resultado = torrents.getJSONArray(i);

									String jsonString = resultado.toString();
									String responseArray[] = jsonString.split(",");

									String nombre = responseArray[2].replace("\"", "");
									nombre = nombre.substring(0, nombre.indexOf("[") - 1);
									estado = responseArray[21];

									if (estado.contains("Paused")) {
										init = estado.indexOf("Paused") + 6;
										estadoTorrent = "pausada";
									} else if (estado.contains("Seeding")) {
										init = estado.indexOf("Seeding") + 7;
										estadoTorrent = "completada";
									} else if (estado.contains("Downloading")) {
										init = estado.indexOf("Downloading") + 11;
										estadoTorrent = "en proceso";
									} else if (estado.contains("Stopped")) {
										init = estado.indexOf("Stopped") + 7;
										estadoTorrent = "parada";
									}

									String porcentaje = estado.substring(init, estado.length());
									porcentaje = porcentaje.replace(",", "");
									porcentaje = porcentaje.replace("\"", "");

									mensaje = mensaje + " - *" + nombre + "* está " + estadoTorrent + " con un "
											+ porcentaje.trim() + "\n\n";

								}
							} else {
								mensaje = "No hay novedades";
							}

						} catch (JSONException e) {
							mensaje = "No hay novedades";
						}

						message.setText(mensaje);

						// Elimina la información de las variables
						Util.deleteData();

					} else {

						if (listaOpciones != null) {

							for (String Opcion : listaOpciones) {

								if (update.getMessage().getText().equals(Opcion)) {

									int numero;

									String numeroString = update.getMessage().getText().substring(0, 3);
									numeroString = numeroString.replace(".", "");
									numero = Integer.parseInt(numeroString.trim()) - 1;

									try {
										movieSeleccionada = Downloader.getMovieInfo(movies.get(numero).getUrl(),
												Config.getDOMAIN());

										movieSeleccionada.setName(movies.get(numero).getName());
										movieSeleccionada.setQuality(movies.get(numero).getQuality());
										movieSeleccionada.setUrl(movies.get(numero).getUrl());

										// Pasa la busqueda a formato URL
										String busquedaURL = movieSeleccionada.getName();

										if (busquedaURL.contains("Version")) {
											busquedaURL = busquedaURL.substring(0, busquedaURL.indexOf("Version"));
										}
										if (busquedaURL.contains("version")) {
											busquedaURL = busquedaURL.substring(0, busquedaURL.indexOf("version"));
										}
										if (busquedaURL.contains("(") || busquedaURL.contains(")")) {
											busquedaURL = busquedaURL.substring(0, busquedaURL.indexOf("("));
										}

										busquedaURL = busquedaURL.replace(" ", "%20");

										// Variable que controla la conexión de
										// datos con la API
										boolean APIconnection = true;

										try {
											// Hace busqueda de información en la
											// API
											System.out.println("URL API: https://api.themoviedb.org/3/search/movie?api_key="
													+ Config.getAPIKEY() + "&language=es-ES&query=" + busquedaURL
													+ "&page=1&include_adult=false&region=Spain&year="
													+ movieSeleccionada.getDate());
											ArrayList<Movie> moviesApi = MoviesAPI
													.getMovies("https://api.themoviedb.org/3/search/movie?api_key="
															+ Config.getAPIKEY() + "&language=es-ES&query=" + busquedaURL
															+ "&page=1&include_adult=false&region=Spain&year="
															+ movieSeleccionada.getDate());

											movieSeleccionada.setDescription(moviesApi.get(0).getDescription());
											movieSeleccionada.setId(moviesApi.get(0).getId());
											movieSeleccionada.setName(moviesApi.get(0).getName());
											movieSeleccionada.setVoteAverage(moviesApi.get(0).getVoteAverage());
											movieSeleccionada.setImg(moviesApi.get(0).getImg());
											movieSeleccionada.setDate(moviesApi.get(0).getDate());		
											movieSeleccionada.setCasting(MoviesAPI.getCastList(movieSeleccionada.getId()));

											// Buscamos el trailer
											String trailer = MoviesAPI.getTrailer(movieSeleccionada.getId());
											movieSeleccionada.setTrailer(trailer);
										} catch (Exception e) {
											System.err.println("No se ha podido obtener la información de la API");
											APIconnection = false;
										}

										String mensaje;

										// Si ha encontrado datos en la API
										if (APIconnection) {
											mensaje = ":movie_camera: *" + movieSeleccionada.getName() + "* :popcorn:\n\n"
													+ " - *Calificación*: " + movieSeleccionada.getVoteAverage()
													+ " :thumbsup:\n" + " - *Fecha*: " + movieSeleccionada.getDate()
													+ " :date:\n" + " - *Calidad*: " + movieSeleccionada.getQuality()
													+ " :thumbsup:\n" + " - *Tamaño*: " + movieSeleccionada.getSize()
													+ " :dvd:\n";

											if (movieSeleccionada.getTrailer() != ""
													&& movieSeleccionada.getTrailer() != null) {
												mensaje = mensaje + " - *Trailer*: " + movieSeleccionada.getTrailer();
											}

											ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();

											// Genera un teclado de opciones
											ArrayList<String> optionsKeyboard = new ArrayList<String>();
											optionsKeyboard.add("Añadir a mi biblioteca");
											optionsKeyboard.add("Ver Sinopsis");
											optionsKeyboard.add("Ver Casting");
											optionsKeyboard.add("Enseñame la lista otra vez");
											optionsKeyboard.add("Cancelar");
											keyboard.setKeyboard(Util.generateKeyboard(optionsKeyboard, false));
											message.setReplyMarkup(keyboard);

											// Si NO ha encontrado datos en la API
										} else {
											mensaje = ":movie_camera: *" + movieSeleccionada.getName() + "* :popcorn:\n\n"
													+ " - *Fecha*: " + movieSeleccionada.getDate() + " :date:\n"
													+ " - *Calidad*: " + movieSeleccionada.getQuality() + " :thumbsup:\n"
													+ " - *Tamaño*: " + movieSeleccionada.getSize() + " :dvd:\n";

											ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();

											// Genera un teclado de opciones
											ArrayList<String> optionsKeyboard = new ArrayList<String>();
											optionsKeyboard.add("Añadir a mi biblioteca");
											optionsKeyboard.add("Enseñame la lista otra vez");
											optionsKeyboard.add("Cancelar");
											keyboard.setKeyboard(Util.generateKeyboard(optionsKeyboard, false));
											message.setReplyMarkup(keyboard);
										}

										System.out.println("IMAGEN: " + movieSeleccionada.getImg());

										try {
											SendPhoto sendPhoto = new SendPhoto();
											sendPhoto.setChatId(update.getMessage().getChatId());
											sendPhoto.setPhoto(movieSeleccionada.getImg());
											sendPhoto(sendPhoto);
										} catch (TelegramApiException e) {
											e.printStackTrace();
										}

										message.setText(EmojiParser.parseToUnicode(mensaje));

									} catch (MalformedURLException e) {
										e.printStackTrace();
									}
								} else {

								}
							}

						} else {

						}

					}

					//Bienvenida e inicio de sesion
				} else if (update.getMessage().getText().equals("/start")) {

					message.setText("¡Hola " + update.getMessage().getChat().getFirstName() + "! "
							+ EmojiParser.parseToUnicode(":smile:") + " Necesito tu número de teléfono para "
							+ "que pueda verificar que estás autorizado para usar este Bot. (Toca el botón \"Enviar número de "
							+ "teléfono\" que aparece en el teclado)");

					ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
					List<KeyboardRow> list = new ArrayList<KeyboardRow>();

					KeyboardRow row1 = new KeyboardRow();
					KeyboardRow row2 = new KeyboardRow();

					KeyboardButton button1 = new KeyboardButton();
					KeyboardButton button2 = new KeyboardButton();

					button1.setRequestContact(true);
					button1.setText("Enviar número de teléfono");
					button2.setText("Cancelar");

					row1.add(button1);
					row2.add(button2);

					list.add(row1);
					list.add(row2);

					keyboard.setKeyboard(list);
					message.setReplyMarkup(keyboard);

				}
			}

			//Si no hay mensaje, valida el usuario
		} else {
			
			String mensaje = "No estás autorizado para usar este Bot";
			for (String usuario : UsuariosLista) {
				if(contacto.equals(usuario)){
					mensaje = "Usuario autorizado. Ya puedes realizar peticiones.";
				}
			}
			message.setText(mensaje);	
		}
		
		try {
			sendMessage(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getBotToken() {
		return Config.getBOTAPIKEY();
	}

}
